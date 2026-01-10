import { Request, Response } from "express";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import Profile from "../models/profile.model";
import User from "../models/user.model";

type MentorApplicationStatus = "pending" | "approved" | "rejected";

function mapStatus(user: any): MentorApplicationStatus {
  if (user.mentorApplicationStatus) return user.mentorApplicationStatus;
  if (user.status === "pending-mentor") return "pending";
  if (user.role === "mentor" && user.status === "active") return "approved";
  return "rejected";
}

function mapApplication(user: any, profile?: any) {
  const status = mapStatus(user);
  const submittedAt =
    user.mentorApplicationSubmittedAt || profile?.createdAt || user.createdAt;

  return {
    id: String(user._id),
    mentorId: String(user._id),
    fullName: profile?.fullName || user.name || user.userName || "",
    jobTitle: profile?.jobTitle || "",
    category: profile?.category || "",
    status,
    submittedAt: submittedAt ? new Date(submittedAt).toISOString() : null,
    note: user.mentorApplicationNote || "",
  };
}

export const getMentorApplications = asyncHandler(async (req: Request, res: Response) => {
  const {
    filter = "{}",
    range = "[0,9]",
    sort = '["createdAt","DESC"]',
  } = req.query;

  const filterObj = JSON.parse(filter as string);
  const [start, end] = JSON.parse(range as string);
  const [sortField, sortOrder] = JSON.parse(sort as string);

  const conditions: any[] = [];

  const statusFilter = String(filterObj.status || "").trim();
  if (statusFilter === "pending") {
    conditions.push({
      role: "mentor",
      $or: [
        { mentorApplicationStatus: "pending" },
        { status: "pending-mentor" },
      ],
    });
  } else if (statusFilter === "approved") {
    conditions.push({
      role: "mentor",
      mentorApplicationStatus: "approved",
    });
  } else if (statusFilter === "rejected") {
    conditions.push({ mentorApplicationStatus: "rejected" });
  } else {
    conditions.push({
      role: "mentor",
      $or: [
        { mentorApplicationStatus: "pending" },
        { status: "pending-mentor" },
      ],
    });
  }

  if (filterObj.q) {
    const q = String(filterObj.q).trim();
    if (q) {
      const profileHits = await Profile.find({
        fullName: { $regex: q, $options: "i" },
      })
        .select("user")
        .lean();

      const profileUserIds = profileHits.map((p) => p.user);
      conditions.push({
        $or: [
          { email: { $regex: q, $options: "i" } },
          { userName: { $regex: q, $options: "i" } },
          { name: { $regex: q, $options: "i" } },
          { _id: { $in: profileUserIds } },
        ],
      });
    }
  }

  const query = conditions.length > 0 ? { $and: conditions } : {};

  const total = await User.countDocuments(query);
  const users = await User.find(query)
    .sort({ [sortField]: sortOrder === "DESC" ? -1 : 1 })
    .skip(start)
    .limit(end - start + 1)
    .lean();

  const userIds = users.map((u) => u._id);
  const profiles = await Profile.find({ user: { $in: userIds } })
    .select("user fullName jobTitle category createdAt")
    .lean();
  const profileMap = new Map(
    profiles.map((p) => [String(p.user), p])
  );

  res.set("Content-Range", `mentor-applications ${start}-${end}/${total}`);
  res.set("Access-Control-Expose-Headers", "Content-Range");

  const data = users.map((user) =>
    mapApplication(user, profileMap.get(String(user._id)))
  );

  return res.json(data);
});

export const getMentorApplicationById = asyncHandler(async (req: Request, res: Response) => {
  const user = await User.findById(req.params.id).lean();
  if (!user) return responseHandler.notFound(res, null, "User not found");

  const profile = await Profile.findOne({ user: user._id })
    .select("user fullName jobTitle category createdAt")
    .lean();

  return res.json(mapApplication(user, profile));
});

export const updateMentorApplication = asyncHandler(async (req: Request, res: Response) => {
  const { status, note } = req.body ?? {};
  const user = await User.findById(req.params.id);
  if (!user) return responseHandler.notFound(res, null, "User not found");

  const updates: any = {};
  if (note !== undefined) updates.mentorApplicationNote = String(note);

  if (status) {
    if (status === "pending") {
      updates.role = "mentor";
      updates.status = "pending-mentor";
      updates.mentorApplicationStatus = "pending";
      updates.mentorApplicationReviewedAt = undefined;
      if (!user.mentorApplicationSubmittedAt) {
        updates.mentorApplicationSubmittedAt = new Date();
      }
    } else if (status === "approved") {
      updates.role = "mentor";
      updates.status = "active";
      updates.mentorApplicationStatus = "approved";
      updates.mentorApplicationReviewedAt = new Date();
    } else if (status === "rejected") {
      updates.role = "mentee";
      updates.status = "active";
      updates.mentorApplicationStatus = "rejected";
      updates.mentorApplicationReviewedAt = new Date();
    }
  }

  const updated = await User.findByIdAndUpdate(req.params.id, updates, {
    new: true,
  }).lean();

  if (!updated) return responseHandler.notFound(res, null, "User not found");

  const profile = await Profile.findOne({ user: updated._id })
    .select("user fullName jobTitle category createdAt")
    .lean();

  return res.json(mapApplication(updated, profile));
});

export default {
  getMentorApplications,
  getMentorApplicationById,
  updateMentorApplication,
};

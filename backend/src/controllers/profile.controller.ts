import userController from "./user.controller";
import { Request, Response } from "express";
import responseHandler from "../handlers/response.handler";
import User from "../models/user.model";
import Profile from "../models/profile.model";
import cloudinary from "../utils/cloudinary";
import { asyncHandler } from "../handlers/async.handler";

function isHttpUrl(introVideo: string) {
  try {
    const url = new URL(introVideo);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
}

export const createRequiredProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user.id;
    console.log("User ID:", userId);
    if (!userId) return responseHandler.unauthorized(res, null, "Unauthorized");

    const user = await User.findById(userId).lean();
    if (!user) return responseHandler.notFound(res, null, "User not found");

    const role = (user as any).role as "mentee" | "mentor" | "admin";

    const {
      jobTitle,
      location,
      category,
      bio,
      skills,
      experience,
      headline,
      mentorReason,
      greatestAchievement,
      introVideo,
      description,
      goal,
      education,
      languages,
      links,
    } = req.body ?? {};

    const normStr = (v: any) => (typeof v === "string" ? v.trim() : "");
    const toArray = (v: any): string[] =>
      Array.isArray(v) ? v : typeof v === "string" ? v.split(/[,;\n]/) : [];

    const skillsArr: string[] = [
      ...new Set(
        toArray(skills)
          .map((s) => String(s || "").trim())
          .filter(Boolean)
      ),
    ];

    const languagesArr: string[] = [
      ...new Set(
        toArray(languages)
          .map((s) =>
            String(s || "")
              .trim()
              .toLowerCase()
          )
          .filter(Boolean)
      ),
    ];

    const file = (req as any)?.file as Express.Multer.File | undefined;
    const avatarUrlInput = normStr(req.body?.avatarUrl || req.body?.avatar);

    let avatarUrl: string | undefined;
    let avatarPublicId: string | undefined;

    if (file) {
      if (!file.mimetype?.startsWith("image/")) {
        return responseHandler.badRequest(res, null, "avatar must be an image");
      }
      const up = await cloudinary.uploadImage(file.buffer, {
        folder: "mentorme/avatars",
        resource_type: "image",
        overwrite: false,
        unique_filename: true,
        transformation: [
          { width: 512, height: 512, crop: "fill", gravity: "face:auto" },
        ],
      });
      avatarUrl = up.secure_url;
      avatarPublicId = up.public_id;
    } else if (avatarUrlInput) {
      if (!isHttpUrl(avatarUrlInput)) {
        return responseHandler.badRequest(
          res,
          null,
          "avatarUrl must be a valid http(s) URL"
        );
      }
      const up = await cloudinary.uploadImage(avatarUrlInput, {
        folder: "mentorme/avatars",
        resource_type: "image",
        overwrite: false,
        unique_filename: true,
        transformation: [
          { width: 512, height: 512, crop: "fill", gravity: "face:auto" },
        ],
      });
      avatarUrl = up.secure_url;
      avatarPublicId = up.public_id;
    }

    if (normStr(introVideo) && !isHttpUrl(introVideo)) {
      return responseHandler.badRequest(
        res,
        null,
        "introVideo must be a valid URL"
      );
    }

    const commonMissing: string[] = [];
    if (!normStr(location)) commonMissing.push("location");
    if (!normStr(category)) commonMissing.push("category");
    if (!avatarUrl) commonMissing.push("avatar (file hoặc avatarUrl)");
    if (languagesArr.length === 0) commonMissing.push("languages");

    const mentorMissing: string[] = [];
    const menteeMissing: string[] = [];

    if (role === "mentor") {
      if (!normStr(jobTitle)) mentorMissing.push("jobTitle");
      if (!normStr(experience)) mentorMissing.push("experience");
      if (!normStr(headline)) mentorMissing.push("headline");
      if (!normStr(mentorReason)) mentorMissing.push("mentorReason");
      if (!normStr(greatestAchievement))
        mentorMissing.push("greatestAchievement");
      if (skillsArr.length === 0) mentorMissing.push("skills");
    } else if (role === "mentee") {
      if (!normStr(description)) menteeMissing.push("description");
      if (!normStr(goal)) menteeMissing.push("goal");
      if (!normStr(education)) menteeMissing.push("education");
      if (skillsArr.length === 0) menteeMissing.push("skills");
    }

    const allMissing = [...commonMissing, ...mentorMissing, ...menteeMissing];
    if (allMissing.length > 0) {
      const who = role === "mentor" ? "mentor" : "mentee";
      return responseHandler.badRequest(
        res,
        { missing: allMissing },
        `Missing required fields for ${who}: ${allMissing.join(", ")}`
      );
    }

    const existed = await Profile.findOne({ user: userId }).lean();
    if (existed)
      return responseHandler.conflict(res, null, "Profile already exists");

    const cleanLinks: any = {};
    if (links && typeof links === "object") {
      const keys = [
        "website",
        "twitter",
        "linkedin",
        "github",
        "youtube",
        "facebook",
      ] as const;
      for (const k of keys) {
        const v = normStr((links as any)[k]);
        if (v && isHttpUrl(v)) cleanLinks[k] = v;
      }
    }

    const payload: any = {
      user: userId,
      location: normStr(location),
      category: normStr(category),
      avatarUrl,
      avatarPublicId,
      languages: languagesArr,
      links: cleanLinks,
      profileCompleted: true,
    };

    if (role === "mentor") {
      Object.assign(payload, {
        jobTitle: normStr(jobTitle),
        experience: normStr(experience),
        headline: normStr(headline),
        mentorReason: normStr(mentorReason),
        greatestAchievement: normStr(greatestAchievement),
        bio: normStr(bio || ""),
        skills: skillsArr,
      });
      if (normStr(introVideo)) payload.introVideo = normStr(introVideo);
    } else if (role === "mentee") {
      Object.assign(payload, {
        description: normStr(description),
        goal: normStr(goal),
        education: normStr(education),
        skills: skillsArr,
      });
    }

    const profile = await Profile.findOneAndUpdate({ user: userId }, payload, {
      new: true,
      upsert: true,
      setDefaultsOnInsert: true,
    });

    const newStatus = role === "mentor" ? "pending-mentor" : "active";
    await User.findByIdAndUpdate(
      userId,
      { $set: { status: newStatus } },
      { new: false }
    );

    const next = role === "mentor" ? "/onboarding/review" : "/home";
    const msg =
      role === "mentor"
        ? "Mentor profile created. Your mentor application is pending review."
        : "Mentee profile created. Your account is active.";

    responseHandler.created(
      res,
      {
        profile,
        updatedStatus: newStatus,
        next,
      },
      msg
    );
  }
);

export default {
  createRequiredProfile,
};

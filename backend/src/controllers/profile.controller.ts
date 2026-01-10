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

export const getMyProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user?.id;
    if (!userId) {
      return responseHandler.unauthorized(res, null, "Unauthorized");
    }

    const profile = await Profile.findOneAndUpdate(
      { user: userId },
      { $setOnInsert: { user: userId } },
      { new: true, upsert: true, setDefaultsOnInsert: true }
    )
      .populate("user", "email userName role status")
      .lean();

    return responseHandler.ok(res, { profile }, "Profile fetched successfully");
  }
);

export const updateMyProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user?.id;
    if (!userId) {
      return responseHandler.unauthorized(res, null, "Unauthorized");
    }

    // ✅ DEBUG: Log incoming request
    console.log("📝 UPDATE PROFILE REQUEST:");
    console.log("  User ID:", userId);
    console.log("  Body:", JSON.stringify(req.body, null, 2));
    console.log("  File:", req.file ? "Present" : "None");

    const {
      fullName,
      phone,
      location,
      bio,
      languages,
      skills,
      jobTitle,
      experience,
      headline,
      description,
      goal,
      education,
      category,
      hourlyRateVnd,
      website,
      twitter,
      linkedin,
      github,
      youtube,
      facebook,
    } = req.body;

    let profile = await Profile.findOne({ user: userId });

    if (!profile) {
      return responseHandler.notFound(
        res,
        null,
        "Profile not found.  Please complete onboarding first."
      );
    }

    if (fullName !== undefined) profile.fullName = fullName;
    if (phone !== undefined) profile.phone = phone;
    if (location !== undefined) profile.location = location;
    if (bio !== undefined) profile.bio = bio;
    if (jobTitle !== undefined) profile.jobTitle = jobTitle;
    if (experience !== undefined) profile.experience = experience;
    if (headline !== undefined) profile.headline = headline;
    if (description !== undefined) profile.description = description;
    if (goal !== undefined) profile.goal = goal;
    if (education !== undefined) profile.education = education;

    // ✅ NEW: Mentor-specific fields
    if (category !== undefined) profile.category = category;
    if (hourlyRateVnd !== undefined) {
      const parsedRate = Number(hourlyRateVnd);
      if (Number.isFinite(parsedRate) && parsedRate >= 0) {
        profile.hourlyRateVnd = parsedRate;
      }
    }

    if (website !== undefined) profile.links.website = website;
    if (twitter !== undefined) profile.links.twitter = twitter;
    if (linkedin !== undefined) profile.links.linkedin = linkedin;
    if (github !== undefined) profile.links.github = github;
    if (youtube !== undefined) profile.links.youtube = youtube;
    if (facebook !== undefined) profile.links.facebook = facebook;

    if (languages !== undefined) {
      profile.languages = Array.isArray(languages)
        ? languages
        : languages
            .split(",")
            .map((s: string) => s.trim())
            .filter(Boolean);
    }

    if (skills !== undefined) {
      profile.skills = Array.isArray(skills)
        ? skills
        : skills
            .split(",")
            .map((s: string) => s.trim())
            .filter(Boolean);
    }

    if (req.file) {
      try {
        if (profile.avatarPublicId) {
          try {
            await cloudinary.deleteAsset(profile.avatarPublicId, "image", true);
            console.log(`Deleted old avatar:  ${profile.avatarPublicId}`);
          } catch (deleteError) {
            console.warn("Failed to delete old avatar:", deleteError);
          }
        }

        const uploadResult = await cloudinary.uploadImage(req.file.buffer, {
          folder: "mentor-me-mobile-app/avatars",
          transformation: [
            { width: 400, height: 400, crop: "fill", gravity: "face" },
            { quality: "auto", fetch_format: "auto" },
          ],
        });

        profile.avatarUrl = uploadResult.secure_url;
        profile.avatarPublicId = uploadResult.public_id;

        console.log(`Uploaded new avatar: ${uploadResult.public_id}`);
      } catch (uploadError: any) {
        console.error("Avatar upload failed:", uploadError);
        return responseHandler.internalServerError(
          res,
          null,
          `Failed to upload avatar: ${uploadError.message || "Unknown error"}`
        );
      }
    }

    await profile.save();

    // ✅ DEBUG: Log what was saved
    console.log("✅ PROFILE UPDATED:");
    console.log("  fullName:", profile.fullName);
    console.log("  jobTitle:", profile.jobTitle);
    console.log("  category:", profile.category);
    console.log("  hourlyRateVnd:", profile.hourlyRateVnd);
    console.log("  skills:", profile.skills);

    return responseHandler.ok(res, { profile }, "Profile updated successfully");
  }
);

export const getPublicProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = req.params.id || req.params.userId;
    if (!userId) {
      return responseHandler.badRequest(res, null, "Missing user id");
    }

    const profile = await Profile.findOne({ user: userId })
      .populate("user", "email userName role")
      .lean();

    if (!profile) {
      return responseHandler.notFound(res, null, "Profile not found");
    }

    const publicProfile = {
      fullName: profile.fullName,
      avatarUrl: profile.avatarUrl,
      bio: profile.bio,
      headline: profile.headline,
      location: profile.location,
      jobTitle: profile.jobTitle,
      experience: profile.experience,
      education: profile.education,
      skills: profile.skills,
      languages: profile.languages,
      links: profile.links,
      phone: profile.phone, // ✅ FIXED: Thêm phone
      category: profile.category, // ✅ FIXED: Thêm category (lĩnh vực quan tâm)
      hourlyRateVnd: profile.hourlyRateVnd, // ✅ FIXED: Thêm hourlyRateVnd (cho mentor)
    };

    //  FIXED: Trả về publicProfile trực tiếp, không wrap thêm { profile: ... }
    return responseHandler.ok(res, publicProfile, "Public profile fetched");
  }
);

export const getAllProfiles = asyncHandler(
  async (req: Request, res: Response) => {
    const {
      filter = "{}",
      range = "[0,9]",
      sort = '["createdAt","DESC"]',
    } = req.query;

    const filterObj = JSON.parse(filter as string);
    const [start, end] = JSON.parse(range as string);
    const [sortField, sortOrder] = JSON.parse(sort as string);

    const query: any = {};

    if (filterObj.q) {
      query.$or = [
        { fullName: { $regex: filterObj.q, $options: "i" } },
        { bio: { $regex: filterObj.q, $options: "i" } },
        { headline: { $regex: filterObj.q, $options: "i" } },
      ];
    }

    if (filterObj.category) query.category = filterObj.category;
    if (filterObj.profileCompleted !== undefined) {
      query.profileCompleted = filterObj.profileCompleted === "true";
    }

    const total = await Profile.countDocuments(query);
    const profiles = await Profile.find(query)
      .populate("user", "email userName role status")
      .sort({ [sortField]: sortOrder === "DESC" ? -1 : 1 })
      .skip(start)
      .limit(end - start + 1)
      .lean();

    res.set("Content-Range", `profiles ${start}-${end}/${total}`);
    res.set("Access-Control-Expose-Headers", "Content-Range");

    return res.json(
      profiles.map((p) => ({
        ...p,
        id: (p as any)._id,
        userId: (p as any).user?._id,
      }))
    );
  }
);

export const deleteProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const { id } = req.params;

    const profile = await Profile.findById(id);
    if (!profile) {
      return responseHandler.notFound(res, null, "Profile not found");
    }

    if (profile.avatarPublicId) {
      try {
        await cloudinary.deleteAsset(profile.avatarPublicId, "image", true);
        console.log(`🗑️ Deleted avatar: ${profile.avatarPublicId}`);
      } catch (error) {
        console.error("Failed to delete avatar from cloudinary:", error);
      }
    }

    await Profile.findByIdAndDelete(id);

    return responseHandler.ok(res, { id }, "Profile deleted successfully");
  }
);

export const createRequiredProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user.id;
    console.log("User ID:", userId);
    if (!userId) return responseHandler.unauthorized(res, null, "Unauthorized");

    const user = await User.findById(userId).lean();
    if (!user) return responseHandler.notFound(res, null, "User not found");

    const role = (user as any).role as "mentee" | "mentor" | "admin";

    const {
      fullName,
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

    if (normStr(introVideo) && !isHttpUrl(normStr(introVideo))) {
      return responseHandler.badRequest(
        res,
        null,
        "introVideo must be a valid URL"
      );
    }

    const commonMissing: string[] = [];
    if (!normStr(fullName)) commonMissing.push("fullName");
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

    const existed = await Profile.findOne({ user: userId })
      .select("profileCompleted")
      .lean();
    if (existed?.profileCompleted) {
      return responseHandler.conflict(res, null, "Profile already exists");
    }

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
      fullName: normStr(fullName),
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
  getMyProfile,
  updateMyProfile,
  getPublicProfile,
  getAllProfiles,
  deleteProfile,
};

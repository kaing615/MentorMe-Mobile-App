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

export const getProfileByUserId = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = req.params.userId;
    if (!userId)
      return responseHandler.badRequest(res, null, "userId is required");

    const profile = await Profile.findOne({ user: userId })
      .populate("user", "userName email role status")
      .lean();
    if (!profile)
      return responseHandler.notFound(res, null, "Profile not found");

    return responseHandler.ok(res, profile, "Profile fetched");
  }
);

export const getProfileByUserName = asyncHandler(
  async (req: Request, res: Response) => {
    const userName = req.params.userName;
    if (!userName)
      return responseHandler.badRequest(res, null, "userName is required");

    const user = await User.findOne({
      userName: new RegExp(`^${userName}$`, "i"),
    })
      .select("email userName role status createdAt updatedAt")
      .lean();
    if (!user) return responseHandler.notFound(res, null, "User not found");
    const profile = await Profile.findOne({ user: user._id })
      .populate("user", "userName email role status")
      .lean();
    if (!profile)
      return responseHandler.notFound(res, null, "Profile not found");

    return responseHandler.ok(res, profile, "Profile fetched");
  }
);

export const updateUserProfile = asyncHandler(
  async (req: Request, res: Response) => {
    const userId = (req as any).user.id;
    if (!userId) return responseHandler.unauthorized(res, null, "Unauthorized");

    const profile = await Profile.findOne({ user: userId });
    if (!profile)
      return responseHandler.notFound(res, null, "Profile not found");

    const updatableFields = [
      "jobTitle",
      "location",
      "category",
      "bio",
      "skills",
      "experience",
      "headline",
      "mentorReason",
      "greatestAchievement",
      "introVideo",
      "description",
      "goal",
      "education",
      "languages",
      "links",
      "price",
    ];

    for (const field of updatableFields) {
      if (req.body[field] !== undefined) {
        if (field === "price") {
          const v = Number(req.body.price);
          if (!Number.isFinite(v) || v < 0) {
            return responseHandler.badRequest(
              res,
              null,
              "price must be a non-negative number"
            );
          }
          (profile as any).price = v;
          continue;
        }
        if (field === "skills" || field === "languages") {
          const raw = Array.isArray(req.body[field])
            ? req.body[field]
            : typeof req.body[field] === "string"
            ? req.body[field].split(/[,;\n]/)
            : [];
          profile[field] = [
            ...new Set(
              raw
                .map((s: any) => String(s).trim())
                .filter(Boolean)
                .map((s: string) =>
                  field === "languages" ? s.toLowerCase() : s
                )
            ),
          ];
        } else if (field === "links" && typeof req.body[field] === "object") {
          const cleanLinks: any = {};
          const keys = [
            "website",
            "twitter",
            "linkedin",
            "github",
            "youtube",
            "facebook",
          ] as const;
          for (const k of keys) {
            const v =
              typeof req.body[field][k] === "string"
                ? req.body[field][k].trim()
                : "";
            if (v && isHttpUrl(v)) cleanLinks[k] = v;
          }
          profile.links = cleanLinks;
        } else if (
          field === "introVideo" &&
          req.body[field] &&
          !isHttpUrl(req.body[field])
        ) {
          return responseHandler.badRequest(
            res,
            null,
            "introVideo must be a valid URL"
          );
        } else {
          (profile as any)[field] =
            typeof req.body[field] === "string"
              ? req.body[field].trim()
              : req.body[field];
        }
      }
    }

    const file = (req as any)?.file as Express.Multer.File | undefined;
    const avatarUrlInput =
      typeof req.body?.avatarUrl === "string" ? req.body?.avatarUrl.trim() : "";

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
      if (profile.avatarPublicId) {
        await cloudinary.deleteAsset(profile.avatarPublicId, "image", true);
      }
      profile.avatarUrl = up.secure_url;
      profile.avatarPublicId = up.public_id;
    } else if (avatarUrlInput) {
      if (!isHttpUrl(avatarUrlInput)) {
        return responseHandler.badRequest(
          res,
          null,
          "avatarUrl must be a valid http(s) URL"
        );
      }
      try {
        const up = await cloudinary.uploadImage(avatarUrlInput, {
          folder: "mentorme/avatars",
          resource_type: "image",
          overwrite: false,
          unique_filename: true,
          transformation: [
            { width: 512, height: 512, crop: "fill", gravity: "face:auto" },
          ],
        });
        if (profile.avatarPublicId)
          await cloudinary.deleteAsset(profile.avatarPublicId, "image", true);

        profile.avatarUrl = up.secure_url;
        profile.avatarPublicId = up.public_id;
      } catch (e) {
        console.error("Cloudinary fetch error:", e);
        return responseHandler.badRequest(
          res,
          null,
          "avatarUrl must point to a direct image URL (jpg/png/webp)"
        );
      }
    }

    await profile.save();
    return responseHandler.ok(res, profile, "Profile updated");
  }
);

export const getAllMentors = asyncHandler(
  async (req: Request, res: Response) => {
    const {
      page = "1",
      limit = "10",
      search = "",
      sort = "profile.rating.average:desc",
      minPrice,
      maxPrice,
      minRating,
      category,
      skills,
      languages,
      location,
    } = req.query as Record<string, string>;

    const pageNum = Math.max(1, parseInt(page, 10) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 10));
    const skip = (pageNum - 1) * limitNum;

    const toArr = (v?: string) =>
      !v
        ? []
        : Array.isArray(v)
        ? (v as unknown as string[])
        : String(v)
            .split(/[,;\n]/)
            .map((s) => s.trim())
            .filter(Boolean);

    const categoriesArr = toArr(category);
    const skillsArr = toArr(skills);
    const languagesArr = toArr(languages).map((s) => s.toLowerCase());

    const ALLOW_SORT = new Set([
      "createdAt",
      "updatedAt",
      "userName",
      "profile.rating.average",
      "profile.rating.count",
      "profile.price",
      "profile.category",
    ]);
    const parseSort = (s?: string) => {
      if (!s) return { "profile.rating.average": -1 as 1 | -1 };
      const obj: Record<string, 1 | -1> = {};
      for (const pair of s.split(",")) {
        const [field, order] = pair.split(":");
        if (field && ALLOW_SORT.has(field)) {
          obj[field] = order === "asc" ? 1 : -1;
        }
      }
      return Object.keys(obj).length
        ? obj
        : { "profile.rating.average": -1 as 1 | -1 };
    };
    const sortOption = parseSort(sort);

    const userMatch: any = { role: "mentor", status: "active" };
    if (search) {
      userMatch.$or = [
        { userName: { $regex: search, $options: "i" } },
        { email: { $regex: search, $options: "i" } },
      ];
    }

    const profileMatch: any = {};

    if (search) {
      const rx = { $regex: search, $options: "i" };
      profileMatch.$or = [
        { headline: rx },
        { bio: rx },
        { category: rx },
        { skills: rx },
      ];
    }

    if (location) {
      profileMatch.location = { $regex: String(location), $options: "i" };
    }
    if (categoriesArr.length) {
      profileMatch.category = { $in: categoriesArr };
    }
    if (skillsArr.length) {
      profileMatch.skills = { $in: skillsArr };
    }
    if (languagesArr.length) {
      profileMatch.languages = { $in: languagesArr };
    }
    if (minPrice || maxPrice) {
      profileMatch.price = {};
      if (minPrice) profileMatch.price.$gte = Number(minPrice);
      if (maxPrice) profileMatch.price.$lte = Number(maxPrice);
    }
    if (minRating) {
      profileMatch["rating.average"] = { $gte: Number(minRating) };
    }

    const pipeline: any[] = [
      { $match: userMatch },
      {
        $lookup: {
          from: "profiles",
          localField: "_id",
          foreignField: "user",
          as: "profile",
        },
      },
      { $unwind: { path: "$profile", preserveNullAndEmptyArrays: false } },
      Object.keys(profileMatch).length
        ? {
            $match: {
              profile: { $exists: true },
              ...Object.entries(profileMatch).reduce((acc, [k, v]) => {
                acc[`profile.${k}`] = v;
                return acc;
              }, {} as any),
            },
          }
        : { $match: { profile: { $exists: true } } },
      {
        $project: {
          _id: 1,
          email: 1,
          userName: 1,
          role: 1,
          status: 1,
          createdAt: 1,
          updatedAt: 1,
          profile: {
            user: 1,
            jobTitle: 1,
            location: 1,
            category: 1,
            bio: 1,
            skills: 1,
            experience: 1,
            headline: 1,
            languages: 1,
            price: 1,
            rating: 1,
            avatarUrl: 1,
          },
        },
      },
      {
        $facet: {
          rows: [{ $sort: sortOption }, { $skip: skip }, { $limit: limitNum }],
          totalCount: [{ $count: "count" }],
        },
      },
      {
        $addFields: {
          total: { $ifNull: [{ $arrayElemAt: ["$totalCount.count", 0] }, 0] },
        },
      },
      {
        $project: { total: 1, rows: 1 },
      },
    ];

    const agg = User.aggregate(pipeline)
      .collation({ locale: "en", strength: 2 })
      .option({ allowDiskUse: true });

    const [result] = await agg;
    const total: number = result?.total ?? 0;
    const mentors = result?.rows ?? [];
    const totalPages = Math.ceil(total / limitNum);

    return responseHandler.ok(
      res,
      {
        mentors,
        pagination: { total, page: pageNum, limit: limitNum, totalPages },
      },
      "Mentors fetched"
    );
  }
);

export const getMe = asyncHandler(async (req: Request, res: Response) => {
  const userId = (req as any).user.id;
  if (!userId) return responseHandler.unauthorized(res, null, "Unauthorized");

  const user = await User.findById(userId)
    .select("userName email role status createdAt updatedAt")
    .lean();
  if (!user) return responseHandler.notFound(res, null, "User not found");

  const profile = await Profile.findOne({ user: userId }).lean();

  return responseHandler.ok(res, { user, profile }, "Me fetched");
});

export default {
  createRequiredProfile,
  getProfileByUserId,
  getProfileByUserName,
  updateUserProfile,
  getAllMentors,
  getMe,
};

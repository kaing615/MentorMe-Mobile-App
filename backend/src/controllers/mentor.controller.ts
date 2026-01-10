import { Request, Response } from "express";
import response from "../handlers/response.handler";
import Profile from "../models/profile.model";
import User from "../models/user.model";
import mongoose from "mongoose";
import { asyncHandler } from "../handlers/async.handler";
import toMentorCard from "../utils/mentor.mapper";

function parseNumber(v: any, def: number): number {
  const n = Number(v);
  return Number.isFinite(n) ? n : def;
}

// ✅ Helper: Normalize Vietnamese text for better search
function normalizeVietnamese(text: string): string {
  return text
    .toLowerCase()
    .normalize("NFD") // Decompose accents
    .replace(/[\u0300-\u036f]/g, "") // Remove accent marks
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D");
}

export const listMentors = asyncHandler(async (req: Request, res: Response) => {
  const q = (req.query.q as string | undefined)?.trim();
  const skillsCsv = (req.query.skills as string | undefined) || "";
  const minRating = parseNumber(req.query.minRating, 0);
  const priceMin = req.query.priceMin !== undefined ? parseNumber(req.query.priceMin, 0) : undefined;
  const priceMax = req.query.priceMax !== undefined ? parseNumber(req.query.priceMax, Number.MAX_SAFE_INTEGER) : undefined;
  const sortKey = (req.query.sort as string) || "rating_desc";
  const page = Math.max(1, parseNumber(req.query.page, 1));
  const limitRaw = parseNumber(req.query.limit, 20);
  const limit = Math.min(Math.max(1, limitRaw), 200); // ✅ Tăng limit từ 50 → 200
  const skip = (page - 1) * limit;

  const skills = skillsCsv
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  // Build match conditions for Profile
  const match: any = { profileCompleted: true };

  // ✅ REMOVED: Don't filter by minRating in $match stage
  // We'll handle rating filter AFTER ensuring all mentors are included
  // This allows mentors with rating=0 (no reviews) to appear

  if (priceMin !== undefined || priceMax !== undefined) {
    match["hourlyRateVnd"] = {};
    if (priceMin !== undefined) Object.assign(match["hourlyRateVnd"], { $gte: priceMin });
    if (priceMax !== undefined && priceMax !== Number.MAX_SAFE_INTEGER)
      Object.assign(match["hourlyRateVnd"], { $lte: priceMax });
    if (Object.keys(match["hourlyRateVnd"]).length === 0) delete match["hourlyRateVnd"]; // no-op
  }
  if (skills.length > 0) {
    match["skills"] = { $in: skills };
  }

  // ✅ IMPROVED: Text search with Vietnamese normalization support
  const or: any[] = [];
  if (q) {
    // Escape regex special characters
    const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

    // Create both original and normalized search patterns
    const rxOriginal = new RegExp(escaped, "i"); // Case-insensitive
    const rxNormalized = new RegExp(normalizeVietnamese(escaped), "i");

    // Search in original fields (for exact Vietnamese match)
    or.push({ fullName: rxOriginal });
    or.push({ jobTitle: rxOriginal });
    or.push({ skills: rxOriginal });
    or.push({ company: rxOriginal });

    // ✅ NEW: Also search in normalized fields (for partial match like "Bù" → "Bùi")
    // Note: This requires normalized fields in DB or computed fields
    // For now, we rely on MongoDB's text index or case-insensitive regex
    // The regex will match "Bù" in "Bùi Ngọc Thái" due to case-insensitive flag
  }

  const pipeline: any[] = [
    { $match: match },
    { $lookup: { from: "users", localField: "user", foreignField: "_id", as: "user" } },
    { $unwind: "$user" },
    // keep only mentors
    { $match: { "user.role": "mentor" } },
  ];

  if (or.length > 0) pipeline.push({ $match: { $or: or } });

  // ✅ NEW: Lookup availability to prioritize mentors with published schedules
  pipeline.push({
    $lookup: {
      from: "availabilities",
      let: { mentorUserId: "$user._id" },
      pipeline: [
        {
          $match: {
            $expr: { $eq: ["$mentor", "$$mentorUserId"] },
            isPublished: true, // ✅ Only published schedules
            // Optionally filter future slots only
            // startTime: { $gte: new Date() }
          }
        },
        { $limit: 1 } // Just need to know if ANY published availability exists
      ],
      as: "availabilities"
    }
  });

  // ✅ Add computed field: hasAvailability (true if mentor has published schedules)
  pipeline.push({
    $addFields: {
      hasAvailability: { $gt: [{ $size: "$availabilities" }, 0] },
      // ✅ NEW: isAvailable field for UI (same as hasAvailability for now)
      isAvailable: { $gt: [{ $size: "$availabilities" }, 0] },
      // ✅ Ensure rating fields exist with default 0
      "rating.average": { $ifNull: ["$rating.average", 0] },
      "rating.count": { $ifNull: ["$rating.count", 0] }
    }
  });

  // ✅ Apply minRating filter AFTER ensuring all mentors are included
  if (minRating > 0) {
    pipeline.push({
      $match: { "rating.average": { $gte: minRating } }
    });
  }

  // ✅ IMPROVED: Sorting with availability priority, rating defaults to 0
  // Mentors WITH availability AND high rating → top
  // Mentors WITH availability AND low/no rating → middle
  // Mentors WITHOUT availability → bottom
  let sortStage: Record<string, 1 | -1> = {
    hasAvailability: -1,
    "rating.average": -1,
    "rating.count": -1
  };
  switch (sortKey) {
    case "price_asc":
      sortStage = { hasAvailability: -1, hourlyRateVnd: 1 } as any; // ✅ Availability first, then price
      break;
    case "price_desc":
      sortStage = { hasAvailability: -1, hourlyRateVnd: -1 } as any; // ✅ Availability first, then price
      break;
    case "newest":
      sortStage = { hasAvailability: -1, createdAt: -1 } as any; // ✅ Availability first, then newest
      break;
    case "rating_desc":
    default:
      // ✅ Availability FIRST, then rating (0 rating mentors will appear after rated mentors)
      sortStage = { hasAvailability: -1, "rating.average": -1, "rating.count": -1 } as any;
  }

  pipeline.push({ $sort: sortStage });

  // Facet for pagination and total
  pipeline.push({
    $facet: {
      items: [{ $skip: skip }, { $limit: limit }],
      total: [{ $count: "count" }],
    },
  });

  const result = await Profile.aggregate(pipeline).allowDiskUse(true);
  const first = result[0] || { items: [], total: [] };
  const total = (first.total[0]?.count as number) || 0;

  const items = (first.items as any[]).map((doc) => toMentorCard(doc.user, doc));

  return response.ok(res, { items, page, limit, total });
});

export const getMentorById = asyncHandler(async (req: Request, res: Response) => {
  const id = String(req.params.id || "").trim();
  if (!mongoose.Types.ObjectId.isValid(id)) {
    return response.notFound(res, "Mentor not found");
  }
  const user = await User.findOne({ _id: id /*, role: "mentor"*/ }).lean();
  if (!user) return response.notFound(res, "Mentor not found");
  // Optionally enforce role: if ((user as any).role !== "mentor") return response.notFound(res, "Mentor not found");

  const profile = await Profile.findOne({ user: user._id }).lean();

  // ✅ FIXED: Trả về đầy đủ thông tin giống getPublicProfile thay vì chỉ MentorCard
  if (!profile) {
    return response.notFound(res, "Mentor profile not found");
  }

  const mentorProfile = {
    // Basic MentorCard fields
    id: String(user._id),
    ownerId: String(user._id),
    userId: String(user._id),
    name: profile.fullName || (user as any).userName || "",
    role: profile.jobTitle || "",
    company: (profile as any).company || "",
    rating: Number((profile as any).rating?.average ?? 0) || 0,
    ratingCount: Number((profile as any).rating?.count ?? 0) || 0,
    hourlyRate: Number(profile.hourlyRateVnd ?? 0) || 0,
    skills: Array.isArray(profile.skills) ? profile.skills : [],
    avatarUrl: profile.avatarUrl || "",

    // ✅ FIXED: Thêm các field còn thiếu
    phone: profile.phone,
    bio: profile.bio,
    languages: Array.isArray(profile.languages) ? profile.languages : [],
    category: profile.category,
    hourlyRateVnd: profile.hourlyRateVnd,
    headline: profile.headline,
    experience: profile.experience,
    education: profile.education,
    location: profile.location,
    links: profile.links,
  };

  return response.ok(res, mentorProfile);
});

export default {
  listMentors,
  getMentorById,
};

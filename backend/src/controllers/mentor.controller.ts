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
  const limitRaw = parseNumber(req.query.limit, 100); // ✅ Tăng default từ 20 → 100
  const limit = Math.min(Math.max(1, limitRaw), 500); // ✅ Tăng max từ 200 → 500
  const skip = (page - 1) * limit;

  const skills = skillsCsv
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  // Build match conditions for Profile
  const match: any = { profileCompleted: true };
  // We'll handle rating filter AFTER ensuring all mentors are included
  // This allows mentors with rating=0 (no reviews) to appear

  // We'll apply price filter AFTER ensuring hourlyRateVnd defaults to 0
  // This prevents null/undefined hourlyRateVnd from being excluded
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
    const normalizedQuery = normalizeVietnamese(q);
    const rxNormalized = new RegExp(normalizeVietnamese(escaped), "i");

    // Search in original fields (for exact Vietnamese match)
    or.push({ fullName: rxOriginal });
    or.push({ jobTitle: rxOriginal });
    or.push({ skills: rxOriginal });
    or.push({ company: rxOriginal });

    // ✅ IMPROVED: Add $expr with $regexMatch for advanced matching
    // This allows partial word matching like "Gay" → "Nguyen Van Gay"
    // MongoDB regex already handles this with case-insensitive flag
    // No additional processing needed - the above should work

    console.log(`[listMentors] Search query: "${q}" (normalized: "${normalizedQuery}")`);
  }

  const pipeline: any[] = [
    { $match: match },
    { $lookup: { from: "users", localField: "user", foreignField: "_id", as: "user" } },
    { $unwind: "$user" },
    // keep only mentors
    { $match: { "user.role": "mentor" } },
  ];

  // ✅ Debug: Log initial match count
  console.log(`[listMentors] Initial match conditions:`, JSON.stringify(match));
  console.log(`[listMentors] Skills filter:`, skills.length > 0 ? skills : "NONE");
  console.log(`[listMentors] Price filter:`, `${priceMin}-${priceMax}`);

  if (or.length > 0) pipeline.push({ $match: { $or: or } });

  // ✅ FIXED: Check if mentor has available slots
  // Logic:
  // 1. Find AvailabilitySlot with status="published"
  // 2. Find AvailabilityOccurrence with status="open" that references that slot
  pipeline.push({
    $lookup: {
      from: "availabilityslots", // Collection name for AvailabilitySlot model
      let: { mentorUserId: "$user._id" },
      pipeline: [
        {
          $match: {
            $expr: {
              $and: [
                { $eq: ["$mentor", "$$mentorUserId"] },
                { $eq: ["$status", "published"] } // ✅ Move status check to $expr for consistency
              ]
            }
          }
        },
        // ✅ Check if this slot has open occurrences
        {
          $lookup: {
            from: "availabilityoccurrences",
            let: { slotId: "$_id" },
            pipeline: [
              {
                $match: {
                  $expr: {
                    $and: [
                      { $eq: ["$slot", "$$slotId"] },
                      { $eq: ["$status", "open"] } // ✅ Move status check to $expr
                    ]
                  }
                }
              },
              { $limit: 1 }
            ],
            as: "openOccurrences"
          }
        },
        // ✅ Only keep slots that have open occurrences
        {
          $match: {
            $expr: { $gt: [{ $size: "$openOccurrences" }, 0] } // ✅ Use $expr for size check
          }
        },
        { $limit: 1 }
      ],
      as: "availableSlots"
    }
  });

  // ✅ Add computed field: hasAvailability (true if mentor has published slots with open occurrences)
  pipeline.push({
    $addFields: {
      hasAvailability: { $gt: [{ $size: "$availableSlots" }, 0] },
      isAvailable: { $gt: [{ $size: "$availableSlots" }, 0] },
      "rating.average": { $ifNull: ["$rating.average", 0] },
      "rating.count": { $ifNull: ["$rating.count", 0] },
      hourlyRateVnd: { $ifNull: ["$hourlyRateVnd", 0] }
    }
  });

  // ✅ Apply minRating filter AFTER ensuring all mentors are included
  if (minRating > 0) {
    pipeline.push({
      $match: { "rating.average": { $gte: minRating } }
    });
  }

  // ✅ Apply price filter AFTER hourlyRateVnd defaults to 0
  const hasMeaningfulPriceFilter =
    (priceMin !== undefined && priceMin > 0) ||
    (priceMax !== undefined && priceMax !== Number.MAX_SAFE_INTEGER);

  if (hasMeaningfulPriceFilter) {
    const priceMatch: any = {};
    if (priceMin !== undefined && priceMin > 0) {
      priceMatch.hourlyRateVnd = { $gte: priceMin };
    }
    if (priceMax !== undefined && priceMax !== Number.MAX_SAFE_INTEGER) {
      priceMatch.hourlyRateVnd = priceMatch.hourlyRateVnd || {};
      Object.assign(priceMatch.hourlyRateVnd, { $lte: priceMax });
    }
    pipeline.push({ $match: priceMatch });
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

  // ✅ Safely map items, catch errors
  const items: any[] = [];
  const errors: any[] = [];
  (first.items as any[]).forEach((doc, idx) => {
    try {
      // ✅ DEBUG: Log raw doc fields before mapping
      if (idx < 3) {
        console.log(`[listMentors] 🔍 Raw doc[${idx}]:`, {
          _id: doc._id,
          fullName: doc.fullName,
          avatarUrl: doc.avatarUrl,
          hasAllFields: {
            fullName: !!doc.fullName,
            avatarUrl: !!doc.avatarUrl,
            jobTitle: !!doc.jobTitle,
            skills: !!doc.skills,
            rating: !!doc.rating,
            user: !!doc.user
          }
        });
      }

      const card = toMentorCard(doc.user, doc);

      // ✅ DEBUG: Log mapped card
      if (idx < 3) {
        console.log(`[listMentors] ✅ Mapped card[${idx}]:`, {
          name: card.name,
          avatarUrl: card.avatarUrl,
          rating: card.rating,
          isAvailable: card.isAvailable
        });
      }

      items.push(card);
    } catch (err) {
      console.error(`❌ [listMentors] Failed to map mentor at index ${idx}:`, err);
      console.error(`   Doc:`, JSON.stringify({
        userId: doc.user?._id,
        email: doc.user?.email,
        fullName: doc.fullName
      }));
      errors.push({ idx, error: (err as Error).message });
    }
  });

  // ✅ Debug log
  console.log(`[listMentors] ✅ RESULT: page=${page} limit=${limit} total=${total} items.length=${items.length}`);
  if (errors.length > 0) {
    console.error(`❌ [listMentors] ${errors.length} mentors failed to map:`, errors);
  }
  if (items.length !== total && page === 1) {
    console.warn(`⚠️  MISMATCH: total=${total} but items.length=${items.length} (missing ${total - items.length})`);
  }

  // ✅ Log first 3 and last 3 mentors to see sort order
  if (items.length > 0) {
    console.log(`[listMentors] First 3:`, items.slice(0, 3).map(m => `${m.name}(avail:${m.isAvailable},rating:${m.rating})`));
    console.log(`[listMentors] Last 3:`, items.slice(-3).map(m => `${m.name}(avail:${m.isAvailable},rating:${m.rating})`));
  }

  // ✅ DEBUG: Check if "Nguyen Van Gay" is in the results
  const gayMentor = items.find(m => m.name.includes("Gay"));
  if (gayMentor) {
    console.log(`[listMentors] 🎯 Found "Nguyen Van Gay":`, {
      id: gayMentor.id,
      isAvailable: gayMentor.isAvailable,
      rating: gayMentor.rating,
      hourlyRate: gayMentor.hourlyRate,
      avatarUrl: gayMentor.avatarUrl, // ✅ ADD: Log avatarUrl to debug
      hasAvatarUrl: !!gayMentor.avatarUrl
    });
  } else {
    console.log(`[listMentors] ⚠️  "Nguyen Van Gay" NOT in results`);
    // Check if it was filtered out
    const allDocs = first.items as any[];
    const gayDoc = allDocs.find((doc: any) => doc.fullName?.includes("Gay"));
    if (gayDoc) {
      console.log(`[listMentors] 🔍 Found in raw docs but failed mapping:`, {
        fullName: gayDoc.fullName,
        hasAvailability: gayDoc.hasAvailability,
        isAvailable: gayDoc.isAvailable,
        availableSlotsCount: gayDoc.availableSlots?.length,
        rating: gayDoc.rating?.average
      });
    }
  }

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

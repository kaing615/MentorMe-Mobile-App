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

export const listMentors = asyncHandler(async (req: Request, res: Response) => {
  const q = (req.query.q as string | undefined)?.trim();
  const skillsCsv = (req.query.skills as string | undefined) || "";
  const minRating = parseNumber(req.query.minRating, 0);
  const priceMin = req.query.priceMin !== undefined ? parseNumber(req.query.priceMin, 0) : undefined;
  const priceMax = req.query.priceMax !== undefined ? parseNumber(req.query.priceMax, Number.MAX_SAFE_INTEGER) : undefined;
  const sortKey = (req.query.sort as string) || "rating_desc";
  const page = Math.max(1, parseNumber(req.query.page, 1));
  const limitRaw = parseNumber(req.query.limit, 20);
  const limit = Math.min(Math.max(1, limitRaw), 50);
  const skip = (page - 1) * limit;

  const skills = skillsCsv
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  // Build match conditions for Profile
  const match: any = { profileCompleted: true };

  if (minRating > 0) {
    match["rating.average"] = { $gte: minRating };
  }
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

  // Text-like search across multiple fields using regex (case-insensitive)
  const or: any[] = [];
  if (q) {
    const rx = new RegExp(q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "i");
    or.push({ fullName: rx });
    or.push({ jobTitle: rx });
    or.push({ skills: rx });
    // company is not persisted; if added later on profile, this will start matching
    or.push({ company: rx });
  }

  const pipeline: any[] = [
    { $match: match },
    { $lookup: { from: "users", localField: "user", foreignField: "_id", as: "user" } },
    { $unwind: "$user" },
    // keep only mentors
    { $match: { "user.role": "mentor" } },
  ];

  if (or.length > 0) pipeline.push({ $match: { $or: or } });

  // Sorting
  let sortStage: Record<string, 1 | -1> = { "rating.average": -1, "rating.count": -1 };
  switch (sortKey) {
    case "price_asc":
      sortStage = { hourlyRateVnd: 1 } as any;
      break;
    case "price_desc":
      sortStage = { hourlyRateVnd: -1 } as any;
      break;
    case "newest":
      sortStage = { createdAt: -1 } as any;
      break;
    case "rating_desc":
    default:
      sortStage = { "rating.average": -1, "rating.count": -1 } as any;
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
  const card = toMentorCard(user, profile);
  return response.ok(res, card);
});

export default {
  listMentors,
  getMentorById,
};

export interface MentorCard {
  id: string;
  ownerId: string;
  userId: string;
  name: string;
  role: string;
  company: string;
  rating: number;
  ratingCount: number;
  hourlyRate: number;
  skills: string[];
  avatarUrl: string;
  isAvailable?: boolean; // ✅ NEW: Mentor has published availability
}

/**
 * Map User + optional Profile to compact MentorCard used by FE and calendar.
 * Rules:
 * - userId = String(docUser._id)
 * - id = ownerId ?? userId ?? (docProfile?._id) ?? String(docUser._id)
 * - Acceptance: if userId exists, id must equal userId (unified calendar ID)
 * - Only include required fields in the returned object
 */
export function toMentorCard(docUser: any, docProfile?: any): MentorCard {
  const userId: string = docUser && docUser._id ? String(docUser._id) : "";
  const ownerId: string = userId || "";
  // Ensure id equals userId when present, otherwise fallbacks
  const id: string = userId || (docProfile && docProfile._id ? String(docProfile._id) : "");

  // Prefer profile fields; fallback to user
  const name: string =
    (docProfile?.fullName ? String(docProfile.fullName).trim() : "") ||
    (docUser?.userName ? String(docUser.userName).trim() : "");

  const role: string = docProfile?.jobTitle ? String(docProfile.jobTitle).trim() : "";

  // Company not modeled in current schema; support if present on profile, else empty
  const company: string =
    (docProfile?.company ? String(docProfile.company).trim() : "") || "";

  const rating: number = Number(docProfile?.rating?.average ?? 0) || 0;
  const ratingCount: number = Number(docProfile?.rating?.count ?? 0) || 0;

  // Prefer hourlyRateVnd if present; otherwise 0
  const hourlyRate: number =
    Number(docProfile?.hourlyRateVnd ?? docProfile?.hourlyRate ?? 0) || 0;

  const skills: string[] = Array.isArray(docProfile?.skills)
    ? docProfile.skills
        .filter((s: any) => typeof s === "string" && s.trim())
        .map((s: string) => s.trim())
    : [];

  const avatarUrl: string = docProfile?.avatarUrl ? String(docProfile.avatarUrl) : "";

  // ✅ NEW: isAvailable from aggregation pipeline
  const isAvailable: boolean = docProfile?.isAvailable === true;

  // Return only the required fields
  return {
    id,
    ownerId,
    userId,
    name,
    role,
    company,
    rating,
    ratingCount,
    hourlyRate,
    skills,
    avatarUrl,
    isAvailable, // ✅ Add to response
  };
}

export default toMentorCard;

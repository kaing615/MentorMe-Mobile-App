import mongoose, { Document, Schema, Model } from "mongoose";

export interface IProfile extends Document {
  _id: mongoose.Types.ObjectId;
  user: mongoose.Types.ObjectId;
  jobTitle?: string;
  location?: string;
  category?: string;
  bio?: string;
  skills: string[];
  experience: string;
  headline?: string;
  mentorReason?: string;
  greatestAchievement?: string;
  introVideo?: string;
  description?: string;
  goal?: string;
  education?: string;
  languages: string[];
  reviews: mongoose.Types.ObjectId[];
  rating?: { average: number; count: number };
  links: {
    website: string;
    twitter: string;
    linkedin: string;
    github: string;
    youtube: string;
    facebook: string;
  };
  profileCompleted: boolean;
  avatarUrl?: string;
  avatarPublicId?: string;
}

const urlMatch = /^https?:\/\/.+/i;

const ProfileSchema: Schema<IProfile> = new Schema(
  {
    user: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
    },
    jobTitle: { type: String, trim: true, default: "" },
    location: { type: String, trim: true, default: "" },
    category: { type: String, trim: true, default: "" },
    bio: { type: String, trim: true, default: "" },
    skills: {
      type: [String],
      default: [],
      set: (v: string[]) =>
        Array.isArray(v)
          ? [...new Set(v.map((s) => String(s).trim()).filter(Boolean))]
          : [],
    },
    experience: { type: String, trim: true, default: "" },
    headline: { type: String, trim: true, default: "" },
    mentorReason: { type: String, trim: true, default: "" },
    greatestAchievement: { type: String, trim: true, default: "" },
    introVideo: {
      type: String,
      trim: true,
      default: "",
      match: urlMatch,
    },
    description: { type: String, trim: true, default: "" },
    goal: { type: String, trim: true, default: "" },
    education: { type: String, trim: true, default: "" },
    languages: {
      type: [String],
      default: [],
      set: (v: string[]) =>
        Array.isArray(v)
          ? [
              ...new Set(
                v.map((s) => String(s).trim().toLowerCase()).filter(Boolean)
              ),
            ]
          : [],
    },
    reviews: [{ type: Schema.Types.ObjectId, ref: "Review" }],
    rating: {
      average: { type: Number, min: 0, max: 5, default: 0 },
      count: { type: Number, min: 0, default: 0 },
    },
    links: {
      website: { type: String, trim: true, default: "", match: urlMatch },
      twitter: { type: String, trim: true, default: "", match: urlMatch },
      linkedin: { type: String, trim: true, default: "", match: urlMatch },
      github: { type: String, trim: true, default: "", match: urlMatch },
      youtube: { type: String, trim: true, default: "", match: urlMatch },
      facebook: { type: String, trim: true, default: "", match: urlMatch },
    },
    profileCompleted: { type: Boolean, default: false },
    avatarUrl: { type: String, trim: true, default: "", match: urlMatch },
    avatarPublicId: { type: String, trim: true, default: "" },
  },
  {
    timestamps: true,
    toJSON: {
      virtuals: true,
      versionKey: false,
    },
    toObject: { virtuals: true, versionKey: false },
  }
);

ProfileSchema.index({ user: 1 }, { unique: true });
ProfileSchema.index({
  headline: "text",
  bio: "text",
  category: "text",
  skills: "text",
});

const Profile: Model<IProfile> = mongoose.model<IProfile>(
  "Profile",
  ProfileSchema
);
export default Profile;

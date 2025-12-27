import User from "../models/user.model";
import Profile from "../models/profile.model";

export type UserInfo = {
  email: string;
  name: string;
};

export async function getUserInfo(userId: string): Promise<UserInfo> {
  const [user, profile] = await Promise.all([
    User.findById(userId).select("email userName").lean(),
    Profile.findOne({ user: userId }).select("fullName").lean(),
  ]);

  return {
    email: user?.email ?? "",
    name: profile?.fullName ?? user?.userName ?? "User",
  };
}

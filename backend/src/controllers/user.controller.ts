import { Request, Response } from "express";
import bcrypt from "bcryptjs";
import sgMail from "../utils/sendGrid";
import redis from "../utils/redis";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import User from "../models/user.model";
import { randomInt, randomBytes, createHash, randomUUID } from "crypto";
import jwt, { SignOptions } from "jsonwebtoken";
import getTokenFromReq from "../middlewares/auth.middleware";
import Profile from "../models/profile.model";

const OTP_TTL_SEC = 10 * 60;
const OTP_MAX_ATTEMPTS = 5;

const genNumericOtp = (len = 6) =>
  randomInt(0, 10 ** len)
    .toString()
    .padStart(len, "0");

const sha256 = (s: string) => createHash("sha256").update(s).digest("hex");
const hashToken = (t: string) => sha256(t);

const jwtOpts = (): SignOptions => ({
  expiresIn: 60 * 60 * 24 * 7,
  jwtid: randomUUID(),
  issuer: "mentorme",
  audience: "mentorme-mobile",
});

async function sendOtpEmail(
  to: string,
  userName: string | undefined,
  code: string,
  minutes: number
) {
  const codeSpaced = code.split("").join(" ");
  const from = "kainguyen615@gmail.com";
  await sgMail.send({
    to,
    from,
    subject: "MentorMe • OTP Verification",
    text:
      `Hi ${userName || "there"},\n\n` +
      `Your one-time verification code is: ${code}\n` +
      `It expires in ${minutes} minutes.\n\n` +
      `If you didn’t request this, please ignore this email.\n\n` +
      `— MentorMe Team`,
    html: `
  <div style="background:#f6f8fb;padding:32px 12px;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#111;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="560" style="max-width:560px;background:#ffffff;border-radius:16px;box-shadow:0 2px 8px rgba(0,0,0,0.05);overflow:hidden;">
      <tr>
        <td style="padding:28px 28px 0;">
          <div style="font-size:18px;font-weight:700;color:#111">MentorMe</div>
          <div style="font-size:12px;color:#6b7280;margin-top:4px;">Email verification</div>
        </td>
      </tr>
      <tr>
        <td style="padding:20px 28px 0;">
          <p style="margin:0 0 8px;font-size:16px;">Hi ${
            userName || "there"
          },</p>
          <p style="margin:0 0 16px;line-height:1.6;color:#374151">
            Use the code below to verify your email. This code expires in <strong>${minutes} minutes</strong>.
          </p>
        </td>
      </tr>
      <tr>
        <td style="padding:12px 28px 0;">
          <div style="
            display:inline-block;
            padding:14px 18px;
            border:1px solid #e5e7eb;
            background:#f9fafb;
            border-radius:12px;
            font-size:28px;
            letter-spacing:8px;
            font-weight:700;
            font-family:SFMono-Regular,Menlo,Consolas,Monaco,monospace;
            color:#111;
            text-align:center;">
            ${codeSpaced}
          </div>
        </td>
      </tr>
      <tr>
        <td style="padding:18px 28px 0;">
          <p style="margin:0;color:#6b7280;font-size:13px;line-height:1.6">
            If you didn’t request this, you can safely ignore this email.
          </p>
        </td>
      </tr>
      <tr>
        <td style="padding:24px 28px 28px;">
          <hr style="border:none;border-top:1px solid #eef2f7;margin:0 0 12px;">
          <div style="font-size:12px;color:#9ca3af;">
            © ${new Date().getFullYear()} MentorMe. All rights reserved.
          </div>
        </td>
      </tr>
    </table>
  </div>
  `,
    categories: ["auth", "otp"],
  });
}

export const signUpMentee = asyncHandler(
  async (req: Request, res: Response) => {
    const rawEmail = String(req.body?.email ?? "").trim();
    const email = rawEmail.toLowerCase();
    const userName = String(req.body?.userName ?? "").trim();
    const password = String(req.body?.password ?? "").trim();

    console.log("signup mentee:", { ct: req.headers["content-type"], email });

    if (!email || !userName || !password) {
      return responseHandler.badRequest(
        res,
        null,
        "Email, userName, and password are required"
      );
    }

    const existing = await User.findOne({ email }).lean();
    if (existing && existing.status === "active") {
      return responseHandler.conflict(res, null, "Email is already registered");
    }

    let userId: string | null = null;
    if (existing) {
      userId = String(existing._id);
    } else {
      const hashed = await bcrypt.hash(password, 12);
      try {
        const doc = await User.create({
          email,
          userName,
          passwordHash: hashed,
          role: "mentee",
          status: "pending",
        });
        userId = doc.id;
      } catch (e: any) {
        if (e?.code === 11000) {
          return responseHandler.conflict(
            res,
            null,
            "Email is already registered"
          );
        } else {
          return responseHandler.internalServerError(
            res,
            null,
            "Failed to create user"
          );
        }
      }
    }

    const code = genNumericOtp(6);
    const verificationId = randomBytes(16).toString("hex");
    const minutes = Math.max(1, Math.floor(OTP_TTL_SEC / 60));

    await redis.setEx(
      `otp:verify:${verificationId}`,
      OTP_TTL_SEC,
      JSON.stringify({
        userId,
        verificationId,
        email,
        codeHash: sha256(code),
        attempts: 0,
      })
    );

    try {
      await sendOtpEmail(email, userName, code, minutes);
    } catch (e: any) {
      return responseHandler.internalServerError(
        res,
        null,
        "Failed to send OTP email. Please try again later."
      );
    }

    return responseHandler.created(
      res,
      {
        userId,
        email,
        userName,
        status: "pending",
        verificationId,
        expiresIn: OTP_TTL_SEC,
      },
      "User pending. OTP sent to email."
    );
  }
);

export const verifyEmailOtp = asyncHandler(
  async (req: Request, res: Response) => {
    const verificationId = (req.body.verificationId ?? "").toString().trim();
    const input = (req.body.code ?? "").toString().trim();

    if (!verificationId || !input) {
      return responseHandler.badRequest(
        res,
        null,
        "verificationId and code are required"
      );
    }

    if (!/^\d{6}$/.test(input)) {
      return responseHandler.badRequest(res, null, "Invalid OTP format");
    }

    const key = `otp:verify:${verificationId}`;
    const raw = await redis.get(key);
    if (!raw) {
      return responseHandler.badRequest(res, null, "OTP expired or not found");
    }

    const data = JSON.parse(raw) as {
      codeHash: string;
      attempts: number;
      userId: string;
      email: string;
    };

    if (data.attempts >= OTP_MAX_ATTEMPTS) {
      await redis.del(key);
      return responseHandler.badRequest(
        res,
        null,
        "Too many attempts. Request a new OTP."
      );
    }

    if (sha256(input) !== data.codeHash) {
      const ttlLeft = await redis.ttl(key);
      const safeTtl = ttlLeft > 0 ? ttlLeft : 1;
      await redis.setEx(
        key,
        safeTtl,
        JSON.stringify({ ...data, attempts: data.attempts + 1 })
      );
      return responseHandler.badRequest(res, null, "Invalid OTP");
    }

    const user = await User.findById(data.userId).lean();
    if (!user) {
      await redis.del(key);
      return responseHandler.notFound(res, null, "User not found");
    }

    if (user.status === "active") {
      await redis.del(key);
      return responseHandler.ok(
        res,
        { email: user.email, userId: data.userId, status: "active" },
        "Email already verified"
      );
    }

    const newStatus = user.role === "mentor" ? "pending-mentor" : "active";

    const updated = await User.findByIdAndUpdate(
      data.userId,
      { $set: { status: newStatus, emailVerifiedAt: new Date() } },
      { new: true }
    ).lean();

    if (!updated) {
      await redis.del(key);
      return responseHandler.internalServerError(
        res,
        null,
        "Failed to activate account"
      );
    }

    await redis.del(key);

    let token: string | undefined;
    if (process.env.JWT_SECRET && updated.status === "active") {
      token = jwt.sign(
        {
          id: data.userId,
          email: user.email,
          role: (user as any).role ?? "mentee",
        },
        process.env.JWT_SECRET,
        jwtOpts()
      );
    }

    return responseHandler.ok(
      res,
      {
        email: updated.email,
        userId: String(updated._id),
        status: updated.status,
        token,
      },
      "Email verified"
    );
  }
);

export const signUpAsMentor = asyncHandler(
  async (req: Request, res: Response) => {
    const rawEmail = (req.body.email ?? "").toString().trim();
    const email = rawEmail.toLowerCase();
    const userName = (req.body.userName ?? "").toString().trim();
    const password = (req.body.password ?? "").toString();

    if (!email || !userName || !password) {
      return responseHandler.badRequest(
        res,
        null,
        "Email, userName, and password are required"
      );
    }

    const existing = await User.findOne({ email }).lean();
    if (existing && existing.status === "active") {
      return responseHandler.conflict(res, null, "Email is already registered");
    }

    let userId: string | null = null;
    if (existing) {
      userId = String(existing._id);
    } else {
      const hashed = await bcrypt.hash(password, 12);
      try {
        const doc = await User.create({
          email,
          userName,
          passwordHash: hashed,
          role: "mentor",
          status: "pending-mentor",
        });
        userId = doc.id;
      } catch (e: any) {
        if (e.code === 11000) {
          return responseHandler.conflict(
            res,
            null,
            "Email is already registered"
          );
        }
        return responseHandler.internalServerError(
          res,
          null,
          "Failed to create user"
        );
      }
    }

    const code = genNumericOtp(6);
    const verificationId = randomBytes(16).toString("hex");
    const minutes = Math.max(1, Math.floor(OTP_TTL_SEC / 60));

    await redis.setEx(
      `otp:verify:${verificationId}`,
      OTP_TTL_SEC,
      JSON.stringify({
        userId,
        verificationId,
        email,
        codeHash: sha256(code),
        attempts: 0,
      })
    );

    try {
      await sendOtpEmail(email, userName, code, minutes);
    } catch (e: any) {
      return responseHandler.internalServerError(
        res,
        null,
        "Failed to send OTP email. Please try again later."
      );
    }

    return responseHandler.created(
      res,
      {
        userId,
        email,
        userName,
        status: "pending-mentor",
        role: "mentor",
        verificationId,
        expiresIn: OTP_TTL_SEC,
      },
      "Your mentor application is received and pending review. OTP sent to email."
    );
  }
);

export const signIn = asyncHandler(async (req: Request, res: Response) => {
  const rawEmail = (req.body.email ?? "").toString().trim();
  const email = rawEmail.toLowerCase();
  const password = (req.body.password ?? "").toString();

  if (!email || !password) {
    return responseHandler.badRequest(
      res,
      null,
      "Email and password are required"
    );
  }

  const user = await User.findOne({ email }).lean();
  if (!user || user.status !== "active") {
    return responseHandler.unauthorized(res, null, "Invalid email or password");
  }

  const match = await bcrypt.compare(password, (user as any).passwordHash);
  if (!match) {
    return responseHandler.unauthorized(res, null, "Invalid email or password");
  }

  let token: string | undefined;
  if (process.env.JWT_SECRET) {
    token = jwt.sign(
      {
        id: String(user._id),
        email: user.email,
        role: (user as any).role ?? "mentee",
      },
      process.env.JWT_SECRET,
      jwtOpts()
    );
  }

  const profileDoc = await Profile.findOne({ user: user._id }).lean();
  const profileCompleted = !!profileDoc?.profileCompleted;
  const requiresOnboarding = !profileCompleted;

  const next = requiresOnboarding ? "/onboarding" : "/home";

  return responseHandler.ok(
    res,
    {
      email: user.email,
      role: (user as any).role ?? "mentee",
      userId: String(user._id),
      status: "active",
      token,
      profileCompleted,
      requiresOnboarding,
      next,
      profile: profileDoc
        ? { id: String(profileDoc._id), avatarUrl: profileDoc.avatarUrl || "" }
        : null,
    },
    "Sign-in successful"
  );
});

export const signOut = asyncHandler(async (req: Request, res: Response) => {
  const token = getTokenFromReq(req);

  if (token && process.env.JWT_SECRET) {
    try {
      const payload = jwt.verify(token, process.env.JWT_SECRET) as {
        exp?: number;
        jti?: string;
      };

      const now = Math.floor(Date.now() / 1000);
      const exp = typeof payload.exp === "number" ? payload.exp : 0;
      const ttl = Math.max(1, exp - now);

      if (ttl > 1) {
        if (payload.jti) {
          await redis.setEx(`bl:jwt:jti:${payload.jti}`, ttl, "1");
        }
        await redis.setEx(`bl:jwt:${hashToken(token)}`, ttl, "1");
      }
    } catch {}
  }

  return responseHandler.ok(res, null, "Sign-out successful");
});

export const getCurrentUser = asyncHandler(
  async (req: Request, res: Response) => {
    const authUser = (req as any).user;
    if (!authUser) {
      return responseHandler.unauthorized(res, null, "Unauthorized");
    }

    const [dbUser, profile] = await Promise.all([
      User.findById(authUser.id)
        .select("email userName role status createdAt updatedAt")
        .lean(),
      Profile.findOne({ user: authUser.id })
        .select("profileCompleted avatarUrl avatarPublicId")
        .lean(),
    ]);

    if (!dbUser) return responseHandler.notFound(res, null, "User not found");

    const profileCompleted = !!profile?.profileCompleted;

    const next = !profileCompleted
      ? "/onboarding"
      : dbUser.status !== "active"
      ? "/onboarding/review"
      : "/home";

    const requiresOnboarding = next === "/onboarding";

    return responseHandler.ok(
      res,
      {
        userId: String(dbUser._id),
        email: dbUser.email,
        userName: dbUser.userName,
        role: (dbUser as any).role ?? "mentee",
        createdAt: dbUser.createdAt,
        updatedAt: dbUser.updatedAt,
        profile: profile
          ? {
              profileCompleted,
              avatarUrl: profile.avatarUrl || "",
              avatarPublicId: profile.avatarPublicId || "",
            }
          : { profileCompleted: false, avatarUrl: "" },
        profileCompleted,
        requiresOnboarding,
        next,
      },
      "Current user fetched"
    );
  }
);

export default {
  signUpMentee,
  verifyEmailOtp,
  signUpAsMentor,
  signIn,
  signOut,
  getCurrentUser,
};

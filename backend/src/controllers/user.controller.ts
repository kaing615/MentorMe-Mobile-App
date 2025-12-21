import bcrypt from "bcryptjs";
import transporter from "../utils/emailService";
import nodemailer from "nodemailer";
import redis from "../utils/redis";
import { createHash, randomBytes, randomInt, randomUUID } from "crypto";
import { Request, Response } from "express";
import jwt, { SignOptions } from "jsonwebtoken";
import nodemailer from "nodemailer";
import { asyncHandler } from "../handlers/async.handler";
import responseHandler from "../handlers/response.handler";
import getTokenFromReq from "../middlewares/auth.middleware";
import Profile from "../models/profile.model";
import User, { IUser } from "../models/user.model";
import transporter from "../utils/emailService";
import redis from "../utils/redis";

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
  try {
    const codeSpaced = code.split("").join(" ");
    const from = `"MentorMe" <${process.env.SMTP_USER}>`;
    const info = await transporter.sendMail({
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
    });
    console.log("OTP email sent:", info.messageId);
    console.log("MessageId:", info.messageId);
    console.log("Preview URL:", nodemailer.getTestMessageUrl(info));
    return info;
  } catch (error) {
    console.error("Failed to send OTP email:", error);
    throw new Error("Failed to send verification email");
  }
}

async function issueEmailOtpAndSend(
  userId: string,
  email: string,
  userName: string | undefined
) {
  const cdKey = `otp:last:${userId}`;
  const cd = await redis.ttl(cdKey);
  if (cd > 0) {
    throw new Error(`OTP was recently sent. Try again in ${cd}s`);
  }

  const code = genNumericOtp(6);
  let verificationId = randomBytes(16).toString("hex");
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

  await sendOtpEmail(email, userName, code, minutes);
  await redis.setEx(cdKey, 30, "1");

  return { verificationId, expiresIn: OTP_TTL_SEC };
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
          status: "verifying",
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

    let verificationId: string;
    let expiresIn: number;
    try {
      const issued = await issueEmailOtpAndSend(userId!, email, userName);
      verificationId = issued.verificationId;
      expiresIn = issued.expiresIn;
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
        status: "verifying",
        verificationId,
        expiresIn,
        next: "/verify-otp",
      },
      "Verification required. OTP sent to email."
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

    const newStatus: IUser["status"] = "onboarding";

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
    if (process.env.JWT_SECRET) {
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
        next: "/onboarding",
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
          status: "verifying",
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

    let verificationId: string;
    let expiresIn: number;
    try {
      const issued = await issueEmailOtpAndSend(userId!, email, userName);
      verificationId = issued.verificationId;
      expiresIn = issued.expiresIn;
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
        status: "verifying",
        role: "mentor",
        verificationId,
        expiresIn,
        next: "/verify-otp",
      },
      "Your mentor application is received and pending review. OTP sent to email."
    );
  }
);

export const signIn = asyncHandler(async (req: Request, res: Response) => {
  const rawEmail = (req.body.email ?? "").toString().trim();
  const email = rawEmail.toLowerCase();
  const password = (req.body.password ?? "").toString();

  console.log("🔐 SignIn attempt:", { email, passwordLength: password.length });

  if (!email || !password) {
    return responseHandler.badRequest(
      res,
      null,
      "Email and password are required"
    );
  }

  const user = await User.findOne({ email }).lean();
  if (!user) {
    console.log("❌ User not found:", email);
    return responseHandler.unauthorized(res, null, "Invalid email or password");
  }

  console.log("👤 User found:", {
    email: user.email,
    role: (user as any).role,
    status: (user as any).status,
    hasPassword: !!(user as any).passwordHash,
  });

  // Kiểm tra password trước để đảm bảo user nhập đúng thông tin
  const match = await bcrypt.compare(password, (user as any).passwordHash);
  console.log("🔑 Password match:", match);

  if (!match) {
    console.log("❌ Password mismatch for:", email);
    return responseHandler.unauthorized(res, null, "Invalid email or password");
  }

  // Kiểm tra tài khoản có bị block không
  if ((user as any).isBlocked) {
    console.log("🚫 Account is blocked:", email);
    return responseHandler.forbidden(res, null, "Your account has been blocked. Please contact support.");
  }

  if (user.status === "verifying") {
    console.log("⏳ Account verifying (resend OTP):", email);
    try {
      const { verificationId, expiresIn } = await issueEmailOtpAndSend(
        String(user._id),
        email,
        (user as any).userName
      );

      return responseHandler.ok(
        res,
        {
          userId: String(user._id),
          email: user.email,
          status: "verifying",
          verificationId,
          expiresIn,
          next: "/verify-otp",
          resent: true,
        },
        "Account verifying. OTP sent to email."
      );
    } catch (e: any) {
      const msg = e?.message || "Failed to resend OTP email";
      const m = msg.match(/OTP was recently sent\. Try again in (\d+)s/);
      if (m) {
        const retryIn = Number(m[1]);
        return responseHandler.ok(
          res,
          {
            userId: String(user._id),
            email: user.email,
            status: "verifying",
            next: "/verify-otp",
            resent: false,
            retryIn,
          },
          "OTP was recently sent. Please wait before requesting again."
        );
      }
      return responseHandler.internalServerError(
        res,
        null,
        "Failed to send OTP email. Please try again later."
      );
    }
  }

  if (user.status === "onboarding" || user.status === "pending-mentor") {
    console.log("⏳ Account pending approval:", { email, status: user.status });
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
    return responseHandler.ok(
      res,
      {
        userId: String(user._id),
        email: user.email,
        status: user.status,
        next:
          user.status === "onboarding" ? "/onboarding" : "/onboarding/review",
        requiresOnboarding: user.status === "onboarding",
        requiresApproval: user.status === "pending-mentor",
        token,
      },
      user.status === "onboarding"
        ? "Account requires onboarding."
        : "Account pending mentor approval."
    );
  }

  if (user.status !== "active") {
    console.log("❌ Account not active:", { email, status: user.status });
    return responseHandler.unauthorized(res, null, "Invalid email or password");
  }

  console.log("✅ Login successful for:", email);

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

    let next = "/home";
    if (dbUser.status === "verifying") next = "/verify-otp";
    else if (dbUser.status === "onboarding" || !profileCompleted)
      next = "/onboarding";
    else if (dbUser.status !== "active") next = "/onboarding/review";

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
        next,
      },
      "Current user fetched"
    );
  }
);


export const adminLogin = asyncHandler(async (req: Request, res: Response) => {
  const { username, password } = req.body;
  
  const user = await User.findOne({ 
    email: username, // hoặc userName: username
    role: { $in: ['admin', 'root'] } 
  });
  
  if (!user || !(await bcrypt.compare(password, (user as any).passwordHash))) {
    return responseHandler.unauthorized(res, null, "Invalid credentials");
  }

  // Kiểm tra tài khoản có bị block không
  if ((user as any).isBlocked) {
    return responseHandler.forbidden(res, null, "Your account has been blocked. Please contact administrator.");
  }
  
  const token = jwt.sign(
    { id: String(user._id), email: user.email, role: user.role },
    process.env.JWT_SECRET!,
    jwtOpts()
  );
  
  return responseHandler.ok(res, { 
    accessToken: token, 
    role: user.role,
    userId: String(user._id),
    email: user.email
  }, "Login successful");
});

export const getAllUsers = asyncHandler(async (req: Request, res: Response) => {
  const { filter = '{}', range = '[0,9]', sort = '["createdAt","DESC"]' } = req.query;
  
  const filterObj = JSON.parse(filter as string);
  const [start, end] = JSON.parse(range as string);
  const [sortField, sortOrder] = JSON.parse(sort as string);
  
  const query: any = {};
  if (filterObj.q) {
    query.$or = [
      { name: { $regex: filterObj.q, $options: 'i' } },
      { email: { $regex: filterObj.q, $options: 'i' } },
      { userName: { $regex: filterObj.q, $options: 'i' } }
    ];
  }
  if (filterObj.role) query.role = filterObj.role;
  if (filterObj.status) query.status = filterObj.status;
  if (filterObj.isBlocked !== undefined) query.isBlocked = filterObj.isBlocked === 'true';
  
  const total = await User.countDocuments(query);
  const users = await User.find(query)
    .select('-passwordHash')
    .sort({ [sortField]: sortOrder === 'DESC' ? -1 : 1 })
    .skip(start)
    .limit(end - start + 1);
  
  res.set('Content-Range', `users ${start}-${end}/${total}`);
  res.set('Access-Control-Expose-Headers', 'Content-Range');
  return res.json(users.map(u => ({ ...u.toObject(), id: u._id })));
});

export const getUserById = asyncHandler(async (req: Request, res: Response) => {
  const user = await User.findById(req.params.id).select('-passwordHash');
  if (!user) return responseHandler.notFound(res, null, "User not found");
  return res.json({ ...user.toObject(), id: user._id });
});

export const createUser = asyncHandler(async (req: Request, res: Response) => {
  const { email, userName, name, role, password, status } = req.body;
  const currentUser = (req as any).user;
  
  if (!email || !userName) {
    return responseHandler.badRequest(res, null, "Email and userName are required");
  }
  
  // Chỉ root mới được tạo admin/root
  if (['admin', 'root'].includes(role) && currentUser?.role !== 'root') {
    return responseHandler.forbidden(res, null, "Only root user can create admin accounts");
  }
  
  // Kiểm tra email đã tồn tại chưa
  const existing = await User.findOne({ email });
  if (existing) {
    return responseHandler.conflict(res, null, "Email already exists");
  }
  
  // Tạo password: nếu không có thì random
  const finalPassword = password || `Pass${randomInt(100000, 999999)}!`;
  const passwordHash = await bcrypt.hash(finalPassword, 12);
  
  const user = await User.create({
    email,
    userName,
    name: name || userName,
    passwordHash,
    role: role || 'mentee',
    status: status || 'active', // Admin tạo thì active luôn
    isBlocked: false,
  });
  
  // Gửi email thông báo tài khoản mới với password
  try {
    const from = `"MentorMe" <${process.env.SMTP_USER}>`;
    await transporter.sendMail({
      to: email,
      from,
      subject: "Welcome to MentorMe - Your Account Details",
      text:
        `Hi ${name || userName},\n\n` +
        `Your MentorMe account has been created by an administrator.\n\n` +
        `Login Details:\n` +
        `Username: ${userName}\n` +
        `Email: ${email}\n` +
        `Password: ${finalPassword}\n\n` +
        `Please change your password after your first login.\n\n` +
        `Login at: ${process.env.API_BASE_URL || 'http://localhost:4000'}\n\n` +
        `— MentorMe Team`,
      html: `
  <div style="background:#f6f8fb;padding:32px 12px;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#111;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="560" style="max-width:560px;background:#ffffff;border-radius:16px;box-shadow:0 2px 8px rgba(0,0,0,0.05);overflow:hidden;">
      <tr>
        <td style="padding:28px 28px 0;">
          <div style="font-size:18px;font-weight:700;color:#111">MentorMe</div>
          <div style="font-size:12px;color:#6b7280;margin-top:4px;">Welcome to MentorMe</div>
        </td>
      </tr>
      <tr>
        <td style="padding:20px 28px 0;">
          <p style="margin:0 0 8px;font-size:16px;">Hi ${name || userName},</p>
          <p style="margin:0 0 16px;line-height:1.6;color:#374151">
            Your MentorMe account has been created by an administrator. Here are your login details:
          </p>
        </td>
      </tr>
      <tr>
        <td style="padding:12px 28px;">
          <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:16px;">
            <div style="margin-bottom:8px;">
              <strong style="color:#111;">Username:</strong> 
              <span style="color:#374151;">${userName}</span>
            </div>
            <div style="margin-bottom:8px;">
              <strong style="color:#111;">Email:</strong> 
              <span style="color:#374151;">${email}</span>
            </div>
            <div>
              <strong style="color:#111;">Password:</strong> 
              <code style="background:#fff;padding:4px 8px;border-radius:4px;font-family:monospace;color:#dc2626;">${finalPassword}</code>
            </div>
          </div>
        </td>
      </tr>
      <tr>
        <td style="padding:0 28px 20px;">
          <p style="margin:0;color:#6b7280;font-size:13px;line-height:1.6">
            ⚠️ Please change your password after your first login for security.
          </p>
        </td>
      </tr>
      <tr>
        <td style="padding:0 28px 28px;">
          <hr style="border:none;border-top:1px solid #eef2f7;margin:0 0 12px;">
          <div style="font-size:12px;color:#9ca3af;">
            © ${new Date().getFullYear()} MentorMe. All rights reserved.
          </div>
        </td>
      </tr>
    </table>
  </div>
      `,
    });
    console.log(`✉️ Welcome email sent to ${email}`);
  } catch (emailError: any) {
    console.error("Failed to send welcome email:", emailError);
    // Không fail request nếu email lỗi, user vẫn được tạo
  }
  
  return res.status(201).json({ ...user.toObject(), id: user._id });
});

export const updateUser = asyncHandler(async (req: Request, res: Response) => {
  const { id } = req.params;
  const updates = req.body;
  const currentUser = (req as any).user;
  
  delete updates.id;
  delete updates.passwordHash;
  
  // Lấy thông tin user đang được sửa
  const targetUser = await User.findById(id);
  if (!targetUser) return responseHandler.notFound(res, null, "User not found");
  
  // Admin không được thay đổi role của admin/root khác
  if (currentUser?.role === 'admin') {
    if (['admin', 'root'].includes(targetUser.role as string)) {
      return responseHandler.forbidden(res, null, "You cannot modify admin accounts");
    }
    // Ngăn admin thay đổi role thành admin/root
    if (updates.role && ['admin', 'root'].includes(updates.role)) {
      return responseHandler.forbidden(res, null, "Only root user can change roles to admin");
    }
  }
  
  // Chỉ root mới được thay đổi role thành admin/root
  if (updates.role && ['admin', 'root'].includes(updates.role) && currentUser?.role !== 'root') {
    return responseHandler.forbidden(res, null, "Only root user can assign admin roles");
  }
  
  const user = await User.findByIdAndUpdate(id, updates, { new: true }).select('-passwordHash');
  if (!user) return responseHandler.notFound(res, null, "User not found");
  return res.json({ ...user.toObject(), id: user._id });
});

export const deleteUser = asyncHandler(async (req: Request, res: Response) => {
  const { id } = req.params;
  const currentUser = (req as any).user;
  
  const user = await User.findById(id);
  if (!user) return responseHandler.notFound(res, null, "User not found");
  
  // Admin không được xóa admin/root khác
  if (currentUser?.role === 'admin') {
    if (['admin', 'root'].includes((user as any).role)) {
      return responseHandler.forbidden(res, null, "You cannot delete admin accounts");
    }
  }
  
  // Không cho xóa root user
  if ((user as any).role === 'root') {
    return responseHandler.forbidden(res, null, "Root user cannot be deleted");
  }
  
  await User.findByIdAndDelete(id);
  
  // Có thể thêm logic xóa dữ liệu liên quan (profile, bookings, etc.)
  await Profile.deleteOne({ user: id });
  
  return res.json({ ...user.toObject(), id: user._id });
});

export const changeUserPassword = asyncHandler(async (req: Request, res: Response) => {
  const { id } = req.params;
  const { password } = req.body;
  const currentUser = (req as any).user;
  
  if (!password || password.length < 6) {
    return responseHandler.badRequest(res, null, "Password must be at least 6 characters");
  }
  
  const user = await User.findById(id);
  if (!user) return responseHandler.notFound(res, null, "User not found");
  
  // Admin không được đổi password của admin/root khác (trừ khi là root)
  if (currentUser?.role === 'admin') {
    if (['admin', 'root'].includes((user as any).role)) {
      return responseHandler.forbidden(res, null, "You cannot change password of admin accounts");
    }
  }
  
  // Hash password mới
  const passwordHash = await bcrypt.hash(password, 12);
  await User.findByIdAndUpdate(id, { passwordHash });
  
  return responseHandler.ok(res, { id }, "Password changed successfully");
});

export const changeMyPassword = asyncHandler(async (req: Request, res: Response) => {
  const { currentPassword, newPassword } = req.body;
  const currentUser = (req as any).user;
  
  if (!currentPassword || !newPassword) {
    return responseHandler.badRequest(res, null, "Current password and new password are required");
  }
  
  if (newPassword.length < 6) {
    return responseHandler.badRequest(res, null, "New password must be at least 6 characters");
  }
  
  const user = await User.findById(currentUser.id);
  if (!user) return responseHandler.notFound(res, null, "User not found");
  
  // Verify current password
  const match = await bcrypt.compare(currentPassword, (user as any).passwordHash);
  if (!match) {
    return responseHandler.unauthorized(res, null, "Current password is incorrect");
  }
  
  // Hash and update new password
  const passwordHash = await bcrypt.hash(newPassword, 12);
  await User.findByIdAndUpdate(currentUser.id, { passwordHash });
  
  return responseHandler.ok(res, null, "Your password has been changed successfully");
});

export const approveMentor = asyncHandler(async (req: Request, res: Response) => {
  const { id } = req.params;
  const currentUser = (req as any).user;
  
  const user = await User.findById(id);
  if (!user) return responseHandler.notFound(res, null, "User not found");
  
  if ((user as any).role !== 'mentor') {
    return responseHandler.badRequest(res, null, "User is not a mentor");
  }
  
  if ((user as any).status !== 'pending-mentor') {
    return responseHandler.badRequest(res, null, "Mentor is not in pending status");
  }
  
  await User.findByIdAndUpdate(id, { status: 'active' });
  
  // Gửi email thông báo được duyệt
  try {
    await transporter.sendMail({
      to: user.email,
      from: `"MentorMe" <${process.env.SMTP_USER}>`,
      subject: "MentorMe • Mentor Application Approved ✅",
      text: `Hi ${user.name || user.userName},\n\nCongratulations! Your mentor application has been approved.\nYou can now login and start mentoring.\n\n— MentorMe Team`,
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f7fafc;">
          <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f7fafc; padding: 40px 20px;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.07);">
                  <!-- Header -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%); padding: 48px 40px; text-align: center;">
                      <table width="100%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td style="text-align: center;">
                            <div style="background-color: rgba(255,255,255,0.2); width: 64px; height: 64px; border-radius: 16px; margin: 0 auto 16px; display: inline-flex; align-items: center; justify-content: center; backdrop-filter: blur(10px);">
                              <span style="color: #ffffff; font-size: 32px; font-weight: 700;">M</span>
                            </div>
                            <h1 style="color: #ffffff; margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">MentorMe</h1>
                            <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 14px; font-weight: 500;">Empowering Growth Through Mentorship</p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  
                  <!-- Success Icon -->
                  <tr>
                    <td style="padding: 48px 40px 24px; text-align: center;">
                      <div style="width: 96px; height: 96px; background: linear-gradient(135deg, #10b981 0%, #059669 100%); border-radius: 50%; margin: 0 auto; display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 8px 16px rgba(16, 185, 129, 0.3);">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M20 6L9 17L4 12" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </div>
                    </td>
                  </tr>
                  
                  <!-- Content -->
                  <tr>
                    <td style="padding: 0 40px 40px; text-align: center;">
                      <h2 style="color: #111827; margin: 0 0 16px; font-size: 28px; font-weight: 700;">Congratulations! 🎉</h2>
                      <p style="color: #6b7280; margin: 0 0 12px; font-size: 16px; line-height: 1.6;">
                        Hi <strong style="color: #111827;">${user.name || user.userName}</strong>,
                      </p>
                      <p style="color: #6b7280; margin: 0 0 32px; font-size: 16px; line-height: 1.7;">
                        Your mentor application has been <strong style="color: #10b981;">approved</strong>! You're now part of our amazing community of mentors. Log in to your dashboard and start making a difference.
                      </p>
                      <table width="100%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td align="center">
                            <a href="${process.env.CLIENT_URL || 'https://mentorme.com'}/signin" style="display: inline-block; background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4); transition: all 0.3s;">Get Started →</a>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  
                  <!-- Divider -->
                  <tr>
                    <td style="padding: 0 40px;">
                      <div style="border-top: 1px solid #e5e7eb;"></div>
                    </td>
                  </tr>
                  
                  <!-- Footer -->
                  <tr>
                    <td style="padding: 32px 40px; text-align: center;">
                      <p style="color: #9ca3af; margin: 0 0 8px; font-size: 14px; line-height: 1.6;">
                        Questions? Reach out to us at <a href="mailto:support@mentorme.com" style="color: #6366f1; text-decoration: none;">support@mentorme.com</a>
                      </p>
                      <p style="color: #9ca3af; margin: 0; font-size: 14px;">
                        © 2025 MentorMe. All rights reserved.
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
      `,
    });
  } catch (emailError) {
    console.error("Failed to send approval email:", emailError);
  }
  
  return responseHandler.ok(res, { id }, "Mentor approved successfully");
});

export const rejectMentor = asyncHandler(async (req: Request, res: Response) => {
  const { id } = req.params;
  const { reason } = req.body;
  
  const user = await User.findById(id);
  if (!user) return responseHandler.notFound(res, null, "User not found");
  
  if ((user as any).role !== 'mentor') {
    return responseHandler.badRequest(res, null, "User is not a mentor");
  }
  
  if ((user as any).status !== 'pending-mentor') {
    return responseHandler.badRequest(res, null, "Mentor is not in pending status");
  }
  
  // Chuyển về mentee hoặc xóa tùy yêu cầu - ở đây chuyển về mentee
  await User.findByIdAndUpdate(id, { role: 'mentee', status: 'active' });
  
  // Gửi email thông báo bị từ chối
  try {
    await transporter.sendMail({
      to: user.email,
      from: `"MentorMe" <${process.env.SMTP_USER}>`,
      subject: "MentorMe • Mentor Application Update",
      text: `Hi ${user.name || user.userName},\n\nThank you for your interest in becoming a mentor.\nUnfortunately, your application was not approved at this time.\n${reason ? `\nReason: ${reason}` : ''}\n\nYou can continue using MentorMe as a mentee.\n\n— MentorMe Team`,
      html: `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f7fafc;">
          <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f7fafc; padding: 40px 20px;">
            <tr>
              <td align="center">
                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.07);">
                  <!-- Header -->
                  <tr>
                    <td style="background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%); padding: 48px 40px; text-align: center;">
                      <table width="100%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td style="text-align: center;">
                            <div style="background-color: rgba(255,255,255,0.2); width: 64px; height: 64px; border-radius: 16px; margin: 0 auto 16px; display: inline-flex; align-items: center; justify-content: center; backdrop-filter: blur(10px);">
                              <span style="color: #ffffff; font-size: 32px; font-weight: 700;">M</span>
                            </div>
                            <h1 style="color: #ffffff; margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">MentorMe</h1>
                            <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 14px; font-weight: 500;">Empowering Growth Through Mentorship</p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  
                  <!-- Info Icon -->
                  <tr>
                    <td style="padding: 48px 40px 24px; text-align: center;">
                      <div style="width: 96px; height: 96px; background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); border-radius: 50%; margin: 0 auto; display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 8px 16px rgba(245, 158, 11, 0.3);">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <circle cx="12" cy="12" r="10" stroke="white" stroke-width="2"/>
                          <path d="M12 8V12" stroke="white" stroke-width="2" stroke-linecap="round"/>
                          <circle cx="12" cy="16" r="1" fill="white"/>
                        </svg>
                      </div>
                    </td>
                  </tr>
                  
                  <!-- Content -->
                  <tr>
                    <td style="padding: 0 40px 40px; text-align: center;">
                      <h2 style="color: #111827; margin: 0 0 16px; font-size: 28px; font-weight: 700;">Application Update</h2>
                      <p style="color: #6b7280; margin: 0 0 12px; font-size: 16px; line-height: 1.6;">
                        Hi <strong style="color: #111827;">${user.name || user.userName}</strong>,
                      </p>
                      <p style="color: #6b7280; margin: 0 0 24px; font-size: 16px; line-height: 1.7;">
                        Thank you for your interest in becoming a mentor on MentorMe. After careful review, we regret to inform you that your application was not approved at this time.
                      </p>
                      ${reason ? `
                        <div style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); border-left: 4px solid #f59e0b; padding: 20px; margin: 0 0 24px; text-align: left; border-radius: 8px; box-shadow: 0 2px 4px rgba(245, 158, 11, 0.1);">
                          <p style="color: #92400e; margin: 0 0 8px; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Reason</p>
                          <p style="color: #78350f; margin: 0; font-size: 15px; line-height: 1.6;">
                            ${reason}
                          </p>
                        </div>
                      ` : ''}
                      <p style="color: #6b7280; margin: 0 0 32px; font-size: 16px; line-height: 1.7;">
                        You can continue using MentorMe as a mentee and reapply in the future.
                      </p>
                      <table width="100%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td align="center">
                            <a href="${process.env.CLIENT_URL || 'https://mentorme.com'}/signin" style="display: inline-block; background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);">Continue as Mentee →</a>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                  
                  <!-- Divider -->
                  <tr>
                    <td style="padding: 0 40px;">
                      <div style="border-top: 1px solid #e5e7eb;"></div>
                    </td>
                  </tr>
                  
                  <!-- Footer -->
                  <tr>
                    <td style="padding: 32px 40px; text-align: center;">
                      <p style="color: #9ca3af; margin: 0 0 8px; font-size: 14px; line-height: 1.6;">
                        Questions? Reach out to us at <a href="mailto:support@mentorme.com" style="color: #6366f1; text-decoration: none;">support@mentorme.com</a>
                      </p>
                      <p style="color: #9ca3af; margin: 0; font-size: 14px;">
                        © 2025 MentorMe. All rights reserved.
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
      `,
    });
  } catch (emailError) {
    console.error("Failed to send rejection email:", emailError);
  }
  
  return responseHandler.ok(res, { id }, "Mentor application rejected");
});

export const getPendingMentorsCount = asyncHandler(async (req: Request, res: Response) => {
  const count = await User.countDocuments({ 
    role: 'mentor', 
    status: 'pending-mentor' 
  });
  
  return responseHandler.ok(res, { count }, "Pending mentors count retrieved");
});

export default {
  signUpMentee,
  verifyEmailOtp,
  signUpAsMentor,
  signIn,
  signOut,
  getCurrentUser,
  adminLogin,
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  changeUserPassword,
  changeMyPassword,
  approveMentor,
  rejectMentor,
  getPendingMentorsCount,
};



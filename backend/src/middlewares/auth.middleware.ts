import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import redis from "../utils/redis";
import { createHash } from "crypto";
import responseHandler from "../handlers/response.handler";

const hashToken = (t: string) => createHash("sha256").update(t).digest("hex");

export default function getTokenFromReq(req: Request): string | null {
  const authz = req.headers.authorization || "";
  if (authz.startsWith("Bearer ")) return authz.slice(7);
  return null;
}

export function auth(req: Request, res: Response, next: NextFunction) {
  const token = getTokenFromReq(req);
  if (!token) return responseHandler.unauthorized(res, "Unauthorized");

  (async () => {
    try {
      const payload = jwt.verify(token, process.env.JWT_SECRET!, {
        issuer: "mentorme",
        audience: "mentorme-mobile",
        algorithms: ["HS256"],
        clockTolerance: 5,
      }) as { jti?: string; id: string; email: string; role: string };

      const jtiKey = payload.jti ? `bl:jwt:jti:${payload.jti}` : null;
      const hashKey = `bl:jwt:${hashToken(token)}`;
      const [jtiHit, hashHit] = await Promise.all([
        jtiKey ? redis.get(jtiKey) : null,
        redis.get(hashKey),
      ]);

      if (jtiHit || hashHit) {
        return responseHandler.unauthorized(res, "Token revoked");
      }
      (req as any).user = {
        id: payload.id,
        email: payload.email,
        role: payload.role,
      };
      return next();
    } catch {
      return responseHandler.unauthorized(res, "Unauthorized");
    }
  })();
}

export function requireRoles(...roles: string[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    const user = (req as any).user;
    if (!user) return responseHandler.unauthorized(res, "Unauthorized");
    if (!roles.includes(user.role)) {
      return responseHandler.forbidden(res, "Forbidden");
    }
    next();
  };
}

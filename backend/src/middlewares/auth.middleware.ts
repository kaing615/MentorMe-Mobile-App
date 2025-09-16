import { Request, Response, NextFunction } from "express";
import responseHandler from "../handlers/response.handler";
import jwt from "jsonwebtoken";

export interface AuthUser {
  id: string;
  email: string;
  roles?: string[];
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthUser;
    }
  }
}

function getTokenFromReq(req: Request): string | null {
  const auth = req.headers.authorization;
  if (auth?.startsWith("Bearer ")) return auth.slice(7).trim();
  const cookieToken = (req as any).cookies?.access_token;
  return cookieToken ?? null;
}

export const auth = (options?: { required?: boolean }) => {
  const required = options?.required ?? true;
  return (req: Request, res: Response, next: NextFunction) => {
    try {
      const token = getTokenFromReq(req);
      if (!token) {
        if (!required) return next();
        return responseHandler.unauthorized(res, null, "Unauthorized");
      }

      const secret = process.env.JWT_SECRET;
      if (!secret) {
        return responseHandler.internalServerError(
          res,
          null,
          "JWT secret is not configured"
        );
      }

      const payload = jwt.verify(token, secret) as AuthUser & {
        iat: number;
        exp: number;
      };
      req.user = {
        id: payload.id,
        email: payload.email,
        roles: payload.roles ?? [],
      };
      next();
    } catch {
      return responseHandler.unauthorized(
        res,
        null,
        "Invalid or expired token"
      );
    }
  };
};

export const requireRoles = (...roles: string[]) => {
  return (req: Request, res: Response, next: NextFunction) => {
    const user = req.user;
    if (!user) return responseHandler.unauthorized(res, null, "Unauthorized");
    const userRoles = new Set(user.roles ?? []);
    const ok = roles.some((r) => userRoles.has(r));
    if (!ok) return responseHandler.forbidden(res, null, "Forbidden");
    next();
  };
};

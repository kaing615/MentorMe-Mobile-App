import { Request, Response, NextFunction } from "express";
import { validationResult } from "express-validator";
import responseHandler from "./response.handler";

export const validate = (req: Request, res: Response, next: NextFunction) => {
  const errors = validationResult(req);

  if (!errors.isEmpty()) {
    return responseHandler.badRequest(
      res,
      {
        errors: errors.array().map((e) => ({
          field: (e as any).path ?? (e as any).param ?? "unknown",
          message: e.msg,
        })),
      },
      "Validation error"
    );
  }

  next();
};

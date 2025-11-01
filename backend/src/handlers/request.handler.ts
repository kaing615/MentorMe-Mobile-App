import { Request, Response, NextFunction } from "express";
import { validationResult } from "express-validator";
import responseHandler from "./response.handler";

export const validate = (req: Request, res: Response, next: NextFunction) => {
  const errors = validationResult(req);

  if (!errors.isEmpty()) {
    // Return only the first error message, per requirement
    const first = errors.array({ onlyFirstError: true })[0];
    const message = first?.msg || "Validation error";
    return responseHandler.badRequest(res, message);
  }

  next();
};

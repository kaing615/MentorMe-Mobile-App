// Kiểu tổng quát hơn dựa trên RequestHandler của Express
import { RequestHandler } from "express";

export const asyncHandler =
  <T extends RequestHandler>(fn: T): RequestHandler =>
  (req, res, next) =>
    Promise.resolve(fn(req, res, next)).catch(next);

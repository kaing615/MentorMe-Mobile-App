import { Response } from "express";

export function ok<T>(res: Response, data: T, message = "OK") {
  return res.status(200).json({ success: true, message, data });
}

export function created<T>(res: Response, data: T, message = "Created") {
  return res.status(201).json({ success: true, message, data });
}

export function notFound<T>(res: Response, data: T, message = "Not Found") {
  return res.status(404).json({ success: false, message, data });
}

export function badRequest<T>(res: Response, data: T, message = "Bad Request") {
  return res.status(400).json({ success: false, message, data });
}

export function unauthorized<T>(
  res: Response,
  data: T,
  message = "Unauthorized"
) {
  return res.status(401).json({ success: false, message, data });
}

export function forbidden<T>(res: Response, data: T, message = "Forbidden") {
  return res.status(403).json({ success: false, message, data });
}

export function conflict<T>(res: Response, data: T, message = "Conflict") {
  return res.status(409).json({ success: false, message, data });
}

export function internalServerError<T>(
  res: Response,
  data: T,
  message = "Internal Server Error"
) {
  return res.status(500).json({ success: false, message, data });
}

export default {
  ok,
  created,
  notFound,
  badRequest,
  unauthorized,
  forbidden,
  conflict,
  internalServerError,
};

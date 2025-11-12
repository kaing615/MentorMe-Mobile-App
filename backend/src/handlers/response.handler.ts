import { Response } from "express";

export function ok<T>(res: Response, data: T, message = "OK") {
  return res.status(200).json({ success: true, message, data });
}

export function created<T>(res: Response, data: T, message = "Created") {
  return res.status(201).json({ success: true, message, data });
}

// Support both: notFound(res, payload, message) and notFound(res, message)
export function notFound<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Not Found"
) {
  if (typeof dataOrMessage === "string" && message === "Not Found") {
    return res.status(404).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(404)
    .json({ success: false, message, data: dataOrMessage as T });
}

// Support both: badRequest(res, payload, message) and badRequest(res, message)
export function badRequest<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Bad Request"
) {
  if (typeof dataOrMessage === "string" && message === "Bad Request") {
    return res.status(400).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(400)
    .json({ success: false, message, data: dataOrMessage as T });
}

// Support both: unauthorized(res, payload, message) and unauthorized(res, message)
export function unauthorized<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Unauthorized"
) {
  if (typeof dataOrMessage === "string" && message === "Unauthorized") {
    return res.status(401).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(401)
    .json({ success: false, message, data: dataOrMessage as T });
}

// Support both: forbidden(res, payload, message) and forbidden(res, message)
export function forbidden<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Forbidden"
) {
  if (typeof dataOrMessage === "string" && message === "Forbidden") {
    return res.status(403).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(403)
    .json({ success: false, message, data: dataOrMessage as T });
}

// Support both: conflict(res, payload, message) and conflict(res, message)
export function conflict<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Conflict"
) {
  if (typeof dataOrMessage === "string" && message === "Conflict") {
    return res.status(409).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(409)
    .json({ success: false, message, data: dataOrMessage as T });
}

// Support both: internalServerError(res, payload, message) and internalServerError(res, message)
export function internalServerError<T>(
  res: Response,
  dataOrMessage: T | string,
  message = "Internal Server Error"
) {
  if (
    typeof dataOrMessage === "string" &&
    message === "Internal Server Error"
  ) {
    return res.status(500).json({ success: false, message: dataOrMessage });
  }
  return res
    .status(500)
    .json({ success: false, message, data: dataOrMessage as T });
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

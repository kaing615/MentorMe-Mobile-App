import { body, param, query } from "express-validator";

export const mentorApplicationIdRules = [
  param("id").isMongoId().withMessage("id must be a valid user ID"),
];

export const mentorApplicationListRules = [
  query("status")
    .optional()
    .isIn(["pending", "approved", "rejected"])
    .withMessage("invalid status"),
  query("q").optional().isString(),
];

export const mentorApplicationUpdateRules = [
  body("status")
    .optional()
    .isIn(["pending", "approved", "rejected"])
    .withMessage("invalid status"),
  body("note")
    .optional()
    .isString()
    .trim()
    .isLength({ max: 1000 })
    .withMessage("note must be at most 1000 characters"),
];

export default {
  mentorApplicationIdRules,
  mentorApplicationListRules,
  mentorApplicationUpdateRules,
};

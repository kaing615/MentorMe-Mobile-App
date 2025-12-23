import { body } from "express-validator";
import { RequestHandler } from "express";

export const createPayoutRequestValidator: RequestHandler[] = [
  body("amount")
    .exists().withMessage("amount is required")
    .isNumeric().withMessage("amount must be a number")
    .custom((value) => Number(value) > 0)
    .withMessage("amount must be greater than 0"),
  body("currency")
    .optional()
    .isIn(["VND", "USD"])
    .withMessage("currency must be VND or USD"),
  body("clientRequestId")
    .exists().withMessage("clientRequestId is required")
    .isString().withMessage("clientRequestId must be a string")
    .notEmpty().withMessage("clientRequestId cannot be empty"),
];

export const mockPayoutWebhookValidator: RequestHandler[] = [
  body("externalId")
    .exists().withMessage("externalId is required")
    .isString().withMessage("externalId must be a string")
    .notEmpty().withMessage("externalId cannot be empty"),
  body("status")
    .exists().withMessage("status is required")
    .isIn(["PAID", "FAILED"])
    .withMessage("status must be PAID or FAILED"),
];

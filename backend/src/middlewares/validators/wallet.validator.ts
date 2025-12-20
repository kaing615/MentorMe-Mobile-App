import { body, query } from "express-validator";
import { RequestHandler } from "express";

export const mockTopupValidator: RequestHandler[] = [
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

export const mockDebitValidator: RequestHandler[] = [
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

export const listTransactionsValidator: RequestHandler[] = [
  query("limit")
    .optional()
    .isInt({ min: 1, max: 50 })
    .withMessage("limit must be between 1 and 50"),

  query("cursor")
    .optional()
    .isString()
    .withMessage("cursor must be a string"),

  query("type")
    .optional()
    .isIn(["CREDIT", "DEBIT", "REFUND"])
    .withMessage("invalid transaction type"),

  query("source")
    .optional()
    .isIn([
      "MANUAL_TOPUP",
      "MANUAL_WITHDRAW",
      "BOOKING_PAYMENT",
      "BOOKING_REFUND",
    ])
    .withMessage("invalid transaction source"),
];

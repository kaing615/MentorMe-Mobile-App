import { Request, Response, NextFunction } from "express";
import { body, ValidationChain, validationResult } from "express-validator";

const emailRule = body("email")
  .exists({ checkFalsy: true })
  .withMessage("Email is required")
  .bail()
  .isEmail()
  .withMessage("Invalid email address")
  .bail()
  .normalizeEmail();

const passwordRule = body("password")
  .exists({ checkFalsy: true })
  .withMessage("Password is required")
  .bail()
  .isLength({ min: 8, max: 72 })
  .withMessage("Password must be 8–72 characters")
  .matches(/^(?=.*[A-Za-z])(?=.*\d).+$/)
  .withMessage("Password must contain at least one letter and one number");

const userNameRule = body("userName")
  .exists({ checkFalsy: true })
  .withMessage("User Name is required")
  .bail()
  .isString()
  .trim()
  .isLength({ min: 3, max: 50 })
  .withMessage("User Name must be 3–50 characters")
  .bail()
  .matches(/^[\p{L}\p{N}\s._-]+$/u)
  .withMessage("User Name contains invalid characters");

export const signUpMenteeValidator: ValidationChain[] = [
  emailRule,
  passwordRule,
  userNameRule,
];

export const signUpMentorValidator: ValidationChain[] = [
  emailRule,
  passwordRule,
  userNameRule,
];

export const signInValidator: ValidationChain[] = [emailRule, passwordRule];

export const verifyOtpValidator: ValidationChain[] = [
  body("verificationId")
    .exists({ checkFalsy: true })
    .withMessage("verificationId is required")
    .bail()
    .isHexadecimal()
    .withMessage("verificationId must be hexadecimal")
    .bail()
    .isLength({ min: 32, max: 32 })
    .withMessage("verificationId must be 32 hex characters"),
  body("code")
    .exists({ checkFalsy: true })
    .withMessage("OTP code is required")
    .bail()
    .matches(/^\d{6}$/)
    .withMessage("OTP code must be 6 digits"),
];

export default {
  signUpMenteeValidator,
  signUpMentorValidator,
  signInValidator,
  verifyOtpValidator,
};

// path: src/middlewares/validators/notification.validator.ts
import { body } from 'express-validator';

const platformValues = ['android', 'ios', 'web'];

const isPlainObject = (value: unknown) =>
  value !== null && typeof value === 'object' && !Array.isArray(value);

export const registerDeviceTokenRules = [
  body('token').isString().trim().notEmpty().withMessage('token is required'),
  body('platform')
    .optional()
    .isString()
    .trim()
    .toLowerCase()
    .isIn(platformValues)
    .withMessage('platform must be android, ios, or web'),
  body('deviceId')
    .optional()
    .isString()
    .trim()
    .isLength({ max: 128 })
    .withMessage('deviceId must be at most 128 characters'),
];

export const unregisterDeviceTokenRules = [
  body('token')
    .optional()
    .isString()
    .trim()
    .notEmpty()
    .withMessage('token must be a non-empty string'),
];

export const sendTestPushRules = [
  body('title')
    .optional()
    .isString()
    .trim()
    .isLength({ max: 200 })
    .withMessage('title must be at most 200 characters'),
  body('body')
    .optional()
    .isString()
    .trim()
    .isLength({ max: 1000 })
    .withMessage('body must be at most 1000 characters'),
  body('data')
    .optional()
    .custom((value) => isPlainObject(value))
    .withMessage('data must be an object'),
];

export const sendPushToUserIdRules = [
  body('userId').isMongoId().withMessage('userId must be a valid ID'),
  body('title')
    .isString()
    .trim()
    .notEmpty()
    .withMessage('title is required')
    .isLength({ max: 200 })
    .withMessage('title must be at most 200 characters'),
  body('body')
    .isString()
    .trim()
    .notEmpty()
    .withMessage('body is required')
    .isLength({ max: 1000 })
    .withMessage('body must be at most 1000 characters'),
  body('data')
    .optional()
    .custom((value) => isPlainObject(value))
    .withMessage('data must be an object'),
];

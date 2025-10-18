// path: src/middlewares/validators/slot.validator.ts
import { body, param, query } from 'express-validator';

const isISO = (v?: string) => !v || /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z$/.test(v);
const rruleBasic = /FREQ=(SECONDLY|MINUTELY|HOURLY|DAILY|WEEKLY|MONTHLY|YEARLY)/i;

export const createSlotRules = [
  body('timezone').isString().notEmpty().withMessage('timezone is required'),
  body('title').optional().isString(),
  body('description').optional().isString(),
  body('rrule').optional().isString().custom((v) => {
    if (v && !rruleBasic.test(v)) throw new Error('rrule must contain a valid FREQ');
    return true;
  }),
  body('exdates').optional().isArray(),
  body('exdates.*').optional().isISO8601(),
  body('bufferBeforeMin').optional().isInt({ min: 0, max: 120 }),
  body('bufferAfterMin').optional().isInt({ min: 0, max: 120 }),
  body('publishHorizonDays').optional().isInt({ min: 1, max: 365 }).withMessage('publishHorizonDays must be 1-365'),
  body('visibility').optional().isIn(['public', 'private']),
  // Luôn yêu cầu start & end làm mốc base (kể cả khi có rrule) để biết duration & anchor
  body('start').custom((value) => {
    if (!value) throw new Error('start is required');
    if (!isISO(value)) throw new Error('start must be ISO UTC');
    return true;
  }),
  body('end').custom((value) => {
    if (!value) throw new Error('end is required');
    if (!isISO(value)) throw new Error('end must be ISO UTC');
    return true;
  }),
  body('end').custom((end, { req }) => {
    if (req.body.start && end) {
      const s = new Date(req.body.start).getTime();
      const e = new Date(end).getTime();
      if (!(e > s)) throw new Error('end must be greater than start');
    }
    return true;
  })
];

export const publishSlotRules = [ param('id').isMongoId() ];

export const calendarQueryRules = [
  param('mentorId').isMongoId(),
  query('from').isISO8601(),
  query('to').isISO8601()
];

import { Router } from 'express';
import multer from 'multer';
import { 
  getChatRestrictionInfo, 
  getMessages, 
  getMessagesByPeer, 
  sendMessage,
  uploadMessageFile  // Import function mới
} from '../controllers/message.controller';
import { validate } from '../handlers/request.handler';
import { auth } from '../middlewares/auth.middleware';
import { messageBookingIdRules, sendMessageRules } from '../middlewares/validators/message.validator';

const router = Router();

// Setup multer for file upload
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { 
    fileSize: 10 * 1024 * 1024, // 10MB max
  },
  fileFilter: (req, file, cb) => {
    // Allow images and common document types
    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/webp',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'text/plain',
    ];
    
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('File type not allowed. Only images and documents are supported.'));
    }
  },
});

// Upload file endpoint - MUST be before other routes
router.post('/upload', auth, upload.single('file'), uploadMessageFile);

// Get all messages with a peer across all bookings (must be before /:bookingId)
router.get('/peer/:peerId', auth, getMessagesByPeer);

// Get chat restriction info for a booking (must be before /:bookingId)
router.get('/:bookingId/restriction-info', auth, messageBookingIdRules, validate, getChatRestrictionInfo);

// List messages for a booking
router.get('/:bookingId', auth, messageBookingIdRules, validate, getMessages);

// Send a message
router.post('/', auth, sendMessageRules, validate, sendMessage);

export default router;

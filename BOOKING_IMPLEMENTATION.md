# Booking System Implementation

## Overview
Complete booking lifecycle implementation for MentorMe mobile app with:
- Payment flow management
- Slot locking and concurrency control
- Email notifications with ICS calendar files
- State machine for booking status transitions

## Architecture

### Backend Components

1. **Model** (`models/booking.model.ts`)
   - MongoDB schema with indexes
   - Status: PaymentPending, Confirmed, Failed, Cancelled, Completed
   - Auto-expiration support

2. **Controller** (`controllers/booking.controller.ts`)
   - Transaction-based slot locking
   - State validation
   - Email notification triggers

3. **Routes** (`routes/booking.route.ts`)
   - RESTful API endpoints
   - Authentication middleware

4. **Utilities**
   - `utils/ics.ts`: ICS calendar file generation
   - `utils/email.ts`: Email notification templates

### Android Components

1. **Models** (`data/model/Models.kt`)
   - Updated BookingStatus enum

2. **API** (`data/remote/MentorMeApi.kt`)
   - Cancel booking endpoint
   - Resend ICS endpoint

3. **Use Cases**
   - `CancelBookingUseCase.kt`
   - `ResendIcsUseCase.kt`

4. **UI**
   - Status display for all booking states
   - Updated status chips and filters

## Features

### 1. Booking Creation
- Creates booking with PaymentPending status
- Immediately locks the slot using MongoDB transactions
- Auto-expires after 15 minutes if payment not completed

### 2. Slot Locking
- MongoDB transactions for atomicity
- Conflict detection for overlapping bookings
- Updates AvailabilityOccurrence status

### 3. Payment Flow
- Webhook handler for payment provider integration
- On success: Confirms booking, sends emails with ICS
- On failure: Marks as Failed, releases slot

### 4. Notifications
Emails sent for:
- Booking confirmed (both parties + ICS)
- Booking failed (mentee only)
- Booking cancelled (both parties)

### 5. ICS Calendar
- Generated for confirmed bookings
- Attached to confirmation emails
- Can be resent via API (idempotent)

### 6. State Machine
Valid transitions:
- PaymentPending → Confirmed, Failed, Cancelled
- Confirmed → Completed, Cancelled
- Failed/Cancelled/Completed are terminal states

## API Endpoints

- `POST /api/v1/bookings` - Create booking
- `GET /api/v1/bookings` - List bookings
- `GET /api/v1/bookings/:id` - Get booking
- `POST /api/v1/bookings/:id/cancel` - Cancel booking
- `POST /api/v1/bookings/:id/resend-ics` - Resend ICS
- `POST /api/v1/payments/webhook` - Payment webhook
- `POST /api/v1/bookings/release-expired` - Release expired bookings

See `backend/docs/BOOKING_API.md` for full API documentation.

## Setup

### Backend

1. Install dependencies:
   ```bash
   cd backend
   npm install
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   # Edit .env with your credentials
   ```

3. Build and start:
   ```bash
   npm run build
   npm start
   ```

### Android

1. Sync Gradle dependencies
2. Build project
3. Run on device/emulator

## Testing

### Manual Testing Flow

1. **Create Booking:**
   ```bash
   curl -X POST http://localhost:4000/api/v1/bookings \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "mentorId": "MENTOR_ID",
       "scheduledAt": "2025-12-25T10:00:00Z",
       "duration": 60,
       "topic": "Career guidance"
     }'
   ```

2. **Confirm Payment:**
   ```bash
   curl -X POST http://localhost:4000/api/v1/payments/webhook \
     -H "Content-Type: application/json" \
     -d '{
       "bookingId": "BOOKING_ID",
       "status": "success",
       "transactionId": "TXN_123"
     }'
   ```

3. **Cancel Booking:**
   ```bash
   curl -X POST http://localhost:4000/api/v1/bookings/BOOKING_ID/cancel \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

### Test Cases

1. ✅ Concurrent booking prevention
2. ✅ Payment success flow
3. ✅ Payment failure flow
4. ✅ Booking cancellation
5. ✅ Slot release on failure/cancel
6. ✅ Email notifications
7. ✅ ICS file generation
8. ✅ Expired booking auto-release
9. ✅ State transition validation
10. ✅ ICS resending (idempotent)

## Configuration

### Email Settings
- Provider: SendGrid
- Configure `SENDGRID_API_KEY` and `SENDGRID_FROM_EMAIL` in .env

### Expiry Time
- Default: 15 minutes
- Modify `BOOKING_EXPIRY_MINUTES` in `booking.controller.ts`

### Cron Job
Set up a cron job to periodically release expired bookings:
```bash
*/5 * * * * curl -X POST http://localhost:4000/api/v1/bookings/release-expired
```

## Edge Cases Handled

1. **Concurrent Bookings**: Transaction + conflict detection prevents double booking
2. **Payment Timeout**: Auto-expires after 15 minutes
3. **Invalid State Transitions**: Validated before applying
4. **Slot Release**: Atomic with status change via transactions
5. **Email Failures**: Logged but don't fail the booking operation
6. **ICS Resend**: Idempotent, safe to call multiple times

## Security Considerations

- ✅ Authentication required for all booking operations
- ✅ Authorization check (user must be mentee or mentor)
- ⚠️ Payment webhook should verify signature (TODO in production)
- ⚠️ Release-expired endpoint should be admin-only (TODO in production)
- ⚠️ **Rate Limiting**: Booking endpoints lack rate limiting (CodeQL finding)
  - Add rate limiting middleware to prevent abuse
  - Recommended: 10 requests per minute per user for booking creation
- ⚠️ **CSRF Protection**: Cookie middleware lacks CSRF protection (CodeQL finding)
  - Not critical for mobile API with Bearer token authentication
  - Consider adding CSRF tokens for web admin panel
- ⚠️ **ICS Injection**: User input in ICS files should be sanitized
  - Escape special characters in mentor/mentee names, topic, and notes

## Future Enhancements

- Add payment provider integration (Stripe, PayPal)
- Implement webhook signature verification
- Add admin dashboard for managing bookings
- Support recurring bookings
- Add booking reminders (24h, 1h before)
- Support rescheduling (cancel + create new)
- Add booking analytics and reporting

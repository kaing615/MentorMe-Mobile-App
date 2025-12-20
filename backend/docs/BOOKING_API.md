# Booking API Documentation

## Overview
The booking system provides a complete lifecycle for managing mentor-mentee booking sessions, including payment processing, slot management, and notifications.

## Booking Lifecycle

```
1. Create Booking (PaymentPending)
   ↓
2. Payment Processing
   ↓
3a. Payment Success → Confirmed
   ↓
4. Session Complete → Completed

OR

3b. Payment Failure → Failed (slot released)

OR

3c. Cancellation → Cancelled (slot released)

OR

3d. Payment Timeout (15 min) → Failed (slot released)
```

## Endpoints

### POST /api/v1/bookings
Create a new booking with PaymentPending status and lock the time slot.

**Authentication:** Required (Bearer token)

**Request Body:**
```json
{
  "mentorId": "string",
  "scheduledAt": "2025-12-20T10:00:00Z",
  "duration": 60,
  "topic": "Career guidance",
  "notes": "Optional notes"
}
```

**Response:** 201 Created
```json
{
  "id": "booking_id",
  "menteeId": "mentee_id",
  "mentorId": "mentor_id",
  "date": "2025-12-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "status": "PaymentPending",
  "price": 50.0,
  "notes": "Optional notes",
  "createdAt": "2025-12-20T09:00:00Z"
}
```

**Features:**
- Uses MongoDB transaction for atomic slot locking
- Prevents double booking with conflict detection
- Auto-expires after 15 minutes if payment not completed
- Returns 409 Conflict if slot is already booked

---

## Valid Status Transitions

- **PaymentPending** → Confirmed, Failed, Cancelled
- **Confirmed** → Completed, Cancelled
- **Failed** → (terminal state)
- **Cancelled** → (terminal state)
- **Completed** → (terminal state)

For full API documentation, see the complete BOOKING_API.md file.

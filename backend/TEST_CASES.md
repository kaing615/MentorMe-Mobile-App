# Test Cases - MentorMe Backend API Integration

## Setup Data
**Mentee**: alo (uk@gmail.com) - ID: `694ab5fb480851a5e45cef13`
**Mentor**: thaygaywibu (thaigay@gmail.com) - ID: `69491c79de45708c17d9e6aa`
**Admin**: thaigay (kositetgay@gmail.com) - ID: `694a737eab7f79797dd13d25`

---

## Test Flow 1: Complete Booking Flow (Happy Path)

### 1.1 Mentor tạo availability slot
```http
POST /api/availability/slots
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "title": "Android Development Consulting",
  "description": "1:1 session về Jetpack Compose",
  "duration": 60,
  "price": 500000,
  "slotDate": "2025-12-26",
  "startTime": "14:00",
  "endTime": "15:00"
}
```
**Expected**: 201 Created, trả về slot với status "draft"

### 1.2 Mentor publish slot
```http
POST /api/availability/slots/{slotId}/publish
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "publishOptions": {
    "immediately": true
  }
}
```
**Expected**: 200 OK, tạo AvailabilityOccurrence với status "open"

### 1.3 Mentee topup wallet (mock)
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": 1000000,
  "currency": "VND",
  "clientRequestId": "topup-001-2025-12-24"
}
```
**Expected**: 
- 200 OK
- Wallet balance tăng 1,000,000 VND
- Tạo WalletTransaction type "CREDIT", source "MANUAL_TOPUP"

### 1.4 Mentee check wallet balance
```http
GET /api/wallet/me
Authorization: Bearer <mentee_token>
```
**Expected**: 
```json
{
  "wallet": {
    "balanceMinor": 1000000,
    "currency": "VND",
    "status": "ACTIVE"
  }
}
```

### 1.5 Mentee xem calendar mentor
```http
GET /api/availability/calendar/{mentorId}?from=2025-12-26&to=2025-12-31
```
**Expected**: Trả về list occurrences với slot vừa publish, status "open"

### 1.6 Mentee tạo booking
```http
POST /api/bookings
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "occurrenceId": "{occurrenceId}",
  "notes": "Tôi muốn học về Navigation Component"
}
```
**Expected**:
- 201 Created
- Booking status "PaymentPending"
- Occurrence status chuyển sang "booked"
- Booking có expiresAt (15 phút)

### 1.7 Mock payment webhook (success)
```http
POST /api/webhooks/payment
Content-Type: application/json

{
  "event": "payment.success",
  "bookingId": "{bookingId}",
  "paymentId": "PAY-12345",
  "status": "completed"
}
```
**Expected**:
- 200 OK
- **Wallet capture**: Mentee bị debit 500,000, Mentor được credit 500,000
- **WalletTransaction**: Tạo 2 records với source "BOOKING_PAYMENT", idempotencyKey "booking_payment:{bookingId}"
- Nếu MENTOR_CONFIRM_REQUIRED=true: Booking status -> "PendingMentor"
- Nếu MENTOR_CONFIRM_REQUIRED=false: Booking status -> "Confirmed"

### 1.8 Mentee check wallet sau payment
```http
GET /api/wallet/me
Authorization: Bearer <mentee_token>
```
**Expected**: balanceMinor = 500,000 (1,000,000 - 500,000)

### 1.9 Mentor check wallet
```http
GET /api/wallet/me
Authorization: Bearer <mentor_token>
```
**Expected**: balanceMinor = 500,000

### 1.10 Mentor confirm booking (nếu MENTOR_CONFIRM_REQUIRED=true)
```http
POST /api/bookings/{bookingId}/mentor-confirm
Authorization: Bearer <mentor_token>
```
**Expected**: 
- 200 OK
- Booking status -> "Confirmed"
- Email/notification gửi cho cả 2 bên

---

## Test Flow 2: Booking Cancellation (Refund Flow)

### 2.1 Mentee cancel booking
```http
POST /api/bookings/{bookingId}/cancel
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "reason": "Tôi bận đột xuất"
}
```
**Expected**:
- 200 OK
- Booking status -> "Cancelled"
- Occurrence status -> "open"
- **Wallet refund**: Mentor bị debit 500,000, Mentee được refund 500,000
- **WalletTransaction**: Tạo 2 records với source "BOOKING_REFUND", idempotencyKey "booking_refund:{bookingId}"

### 2.2 Mentee check wallet after refund
```http
GET /api/wallet/me
Authorization: Bearer <mentee_token>
```
**Expected**: balanceMinor = 1,000,000 (back to original)

### 2.3 Mentee xem transaction history
```http
GET /api/wallet/transactions?limit=10
Authorization: Bearer <mentee_token>
```
**Expected**: Trả về:
1. REFUND - BOOKING_REFUND (500,000)
2. DEBIT - BOOKING_PAYMENT (-500,000)
3. CREDIT - MANUAL_TOPUP (1,000,000)

---

## Test Flow 3: Mentor Decline Booking (Auto Refund)

### 3.1 Tạo booking mới (repeat 1.6 -> 1.7)
Setup: Mentee book slot khác, payment success, booking status "PendingMentor"

### 3.2 Mentor decline booking
```http
POST /api/bookings/{bookingId}/mentor-decline
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "reason": "Schedule conflict"
}
```
**Expected**:
- 200 OK
- Booking status -> "Declined"
- Occurrence status -> "open"
- **Automatic refund**: Mentor debit, Mentee refund
- WalletTransaction tạo với source "BOOKING_REFUND"

---

## Test Flow 4: Mentor Payout Flow

### 4.1 Mentor tạo payout request
```http
POST /api/payouts/requests
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "amount": 400000,
  "currency": "VND",
  "bankAccount": {
    "accountNumber": "1234567890",
    "accountName": "NGUYEN DINH MENTOR",
    "bankName": "Vietcombank",
    "bankBranch": "HCM"
  }
}
```
**Expected**:
- 201 Created
- MentorPayoutRequest status "PENDING"
- idempotencyKey: auto-generated

### 4.2 Admin list pending payouts
```http
GET /api/admin/payouts/requests?status=PENDING
Authorization: Bearer <admin_token>
```
**Expected**: Trả về payout request vừa tạo

### 4.3 Admin approve payout
```http
POST /api/admin/payouts/requests/{payoutId}/approve
Authorization: Bearer <admin_token>
```
**Expected**:
- 200 OK
- Payout status -> "PROCESSING"
- **Atomic wallet debit**: Mentor wallet bị debit 400,000
- **WalletTransaction**: type "DEBIT", source "MENTOR_PAYOUT", idempotencyKey "payout_debit:{payoutId}"
- externalId được generate (mock)

### 4.4 Admin check mentor wallet
```http
GET /api/wallet/me
Authorization: Bearer <mentor_token>
```
**Expected**: balanceMinor = 100,000 (500,000 - 400,000)

### 4.5 Mock payout webhook (PAID)
```http
POST /api/webhooks/payout
Content-Type: application/json

{
  "externalId": "{externalId}",
  "status": "PAID"
}
```
**Expected**:
- 200 OK
- Payout status -> "PAID"
- **NO wallet changes** (đã debit rồi)

---

## Test Flow 5: Payout Failed & Refund

### 5.1 Tạo payout và approve (repeat 4.1 -> 4.3)
Setup: Mentor tạo payout 200,000, admin approve, wallet debit

### 5.2 Mock payout webhook (FAILED)
```http
POST /api/webhooks/payout
Content-Type: application/json

{
  "externalId": "{externalId}",
  "status": "FAILED"
}
```
**Expected**:
- 200 OK
- Payout status -> "FAILED"
- **Automatic refund**: Mentor wallet credit back 200,000
- **WalletTransaction**: type "REFUND", source "MENTOR_PAYOUT_REFUND", idempotencyKey "payout_refund:{payoutId}"

### 5.3 Mentor check wallet after refund
```http
GET /api/wallet/me
Authorization: Bearer <mentor_token>
```
**Expected**: balanceMinor = 300,000 (100,000 + 200,000 refund)

---

## Test Flow 6: Idempotency Tests

### 6.1 Duplicate topup request
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": 500000,
  "currency": "VND",
  "clientRequestId": "topup-duplicate-test"
}
```
**Response 1**: 200 OK, balance tăng 500,000

**Retry với SAME clientRequestId**:
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": 500000,
  "currency": "VND",
  "clientRequestId": "topup-duplicate-test"
}
```
**Expected**: 
- 200 OK
- Message "Transaction already exists"
- Balance KHÔNG thay đổi (still 500,000 increase only)

### 6.2 Duplicate payment webhook
```http
POST /api/webhooks/payment
Content-Type: application/json

{
  "event": "payment.success",
  "bookingId": "{bookingId}",
  "paymentId": "PAY-12345"
}
```
**Call lần 1**: Capture thành công

**Call lần 2 (duplicate)**: 
**Expected**:
- 200 OK
- NO double charge
- WalletTransaction không tạo thêm (idempotencyKey duplicate)

### 6.3 Duplicate payout webhook
```http
POST /api/webhooks/payout
Content-Type: application/json

{
  "externalId": "{externalId}",
  "status": "PAID"
}
```
**Call lần 1**: Mark PAID

**Call lần 2 (duplicate)**:
**Expected**:
- 200 OK
- Message "Webhook already processed"
- Payout status vẫn là "PAID"

---

## Test Flow 7: Error Cases

### 7.1 Insufficient balance - Booking payment
```http
POST /api/bookings
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "occurrenceId": "{occurrenceId}",
  "notes": "Book với balance không đủ"
}
```
Sau đó call payment webhook

**Expected**: 
- Payment webhook throws error "INSUFFICIENT_BALANCE"
- Booking status -> "Failed"
- NO wallet debit

### 7.2 Insufficient balance - Payout
```http
POST /api/payouts/requests
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "amount": 999999999,
  "currency": "VND",
  "bankAccount": {...}
}
```
**Expected**: 201 Created (PENDING)

**Admin approve**:
```http
POST /api/admin/payouts/requests/{payoutId}/approve
Authorization: Bearer <admin_token>
```
**Expected**: 
- 400 Bad Request
- Error: "INSUFFICIENT_BALANCE"
- Payout status vẫn là "PENDING"

### 7.3 Currency mismatch
Setup: Mentor có wallet VND, booking price VND, nhưng force currency USD

**Expected**: "CURRENCY_MISMATCH" error khi capture payment

### 7.4 Wallet locked
```http
# Admin lock mentee wallet (giả sử có endpoint)
PATCH /api/admin/wallets/{userId}/lock
Authorization: Bearer <admin_token>
```

**Mentee thử topup**:
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": 100000,
  "currency": "VND",
  "clientRequestId": "locked-test"
}
```
**Expected**: 400 Bad Request, error "WALLET_LOCKED"

---

## Test Flow 8: Concurrent Transactions

### 8.1 Multiple topups đồng thời
Gửi 3 requests song song với DIFFERENT clientRequestId:
- topup-concurrent-1
- topup-concurrent-2  
- topup-concurrent-3

**Expected**:
- All 3 succeed
- Total balance increase = sum of all amounts
- 3 WalletTransaction records created

### 8.2 Same topup đồng thời (race condition)
Gửi 10 requests song song với SAME clientRequestId

**Expected**:
- Only 1 succeeds with actual credit
- 9 others return idempotent success
- Balance increases by amount only ONCE
- Only 1 WalletTransaction record

---

## Test Flow 9: Booking Expiry

### 9.1 Tạo booking không payment
```http
POST /api/bookings
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "occurrenceId": "{occurrenceId}",
  "notes": "Test expiry"
}
```
**Expected**: Booking status "PaymentPending", expiresAt = now + 15 minutes

### 9.2 Đợi 15 phút (hoặc trigger cron job)
**Expected**:
- Booking status auto -> "Failed"
- Occurrence status -> "open"
- NO wallet transactions (chưa payment)

---

## Test Flow 10: Filter & Pagination

### 10.1 Filter transactions by source
```http
GET /api/wallet/transactions?source=BOOKING_PAYMENT&limit=5
Authorization: Bearer <mentee_token>
```
**Expected**: Chỉ trả về transactions với source "BOOKING_PAYMENT"

### 10.2 Filter by type
```http
GET /api/wallet/transactions?type=DEBIT&limit=10
Authorization: Bearer <mentee_token>
```
**Expected**: Chỉ trả về DEBIT transactions

### 10.3 Cursor pagination
```http
GET /api/wallet/transactions?limit=5
Authorization: Bearer <mentee_token>
```
Response có cursor, dùng cursor để get page 2:
```http
GET /api/wallet/transactions?limit=5&cursor={cursor}
Authorization: Bearer <mentee_token>
```

### 10.4 Admin filter payouts
```http
GET /api/admin/payouts/requests?status=PROCESSING&mentorId={mentorId}
Authorization: Bearer <admin_token>
```
**Expected**: Chỉ trả về processing payouts của mentor cụ thể

---

## Test Flow 11: Late Cancellation Policy

### 11.1 Book session trong 24h tới
Setup: Slot startTime = now + 2 hours

### 11.2 Mentee cancel (late cancel)
```http
POST /api/bookings/{bookingId}/cancel
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "reason": "Late cancellation test"
}
```
**Expected** (nếu LATE_CANCEL_ACTION=block):
- 400 Bad Request
- Error "Late cancellation is not allowed within X minutes"

**Expected** (nếu LATE_CANCEL_ACTION=allow):
- 200 OK
- Booking.lateCancel = true
- Booking.lateCancelMinutes = X
- Refund vẫn xảy ra

---

## Test Flow 12: Double Booking Prevention

### 12.1 Mentee book occurrence
Setup: Mentee A book occurrence X

### 12.2 Mentee B cũng book occurrence X
```http
POST /api/bookings
Authorization: Bearer <mentee_b_token>
Content-Type: application/json

{
  "occurrenceId": "{occurrenceId}",
  "notes": "Double booking attempt"
}
```
**Expected**: 
- 409 Conflict
- Error "Occurrence already booked"

---

## Validation Test Cases

### V1. Invalid amount
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": -1000,
  "currency": "VND",
  "clientRequestId": "invalid-amount"
}
```
**Expected**: 400 Bad Request, "amount must be greater than 0"

### V2. Invalid currency
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentee_token>
Content-Type: application/json

{
  "amount": 100000,
  "currency": "EUR",
  "clientRequestId": "invalid-currency"
}
```
**Expected**: 400 Bad Request, "currency must be VND or USD"

### V3. Missing required fields
```http
POST /api/payouts/requests
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "amount": 100000
}
```
**Expected**: 400 Bad Request, validation errors cho bankAccount

---

## Performance Test Scenarios

### P1. Batch availability publish
```http
POST /api/availability/slots/publish-batch
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "slotIds": ["{id1}", "{id2}", ..., "{id50}"],
  "concurrent": true
}
```
**Expected**: All 50 slots published, test execution time

### P2. Large transaction history
```http
GET /api/wallet/transactions?limit=50
Authorization: Bearer <mentee_token>
```
**Expected**: Test query performance với database có 1000+ transactions

---

## Security Test Cases

### S1. Cross-user access
```http
GET /api/wallet/me
Authorization: Bearer <mentor_token>
```
**Expected**: Chỉ thấy wallet của mình, không thấy mentee wallet

### S2. Role-based access
```http
POST /api/wallet/topups/mock
Authorization: Bearer <mentor_token>
Content-Type: application/json

{
  "amount": 100000,
  "currency": "VND",
  "clientRequestId": "role-test"
}
```
**Expected**: 403 Forbidden (wallet chỉ cho mentee)

### S3. Unauthorized webhook
```http
POST /api/webhooks/payment
# NO Authorization header
Content-Type: application/json

{
  "event": "payment.success",
  "bookingId": "{bookingId}"
}
```
**Expected**: Webhook có thể public HOẶC verify signature (tùy implementation)

---

## Notes
- Tất cả amounts trong VND đều tính bằng đồng (VD: 500000 = 500k VND)
- idempotencyKey format:
  - Topup/Debit: client-provided clientRequestId
  - Booking payment: `booking_payment:{bookingId}`
  - Booking refund: `booking_refund:{bookingId}`
  - Payout debit: `payout_debit:{payoutId}`
  - Payout refund: `payout_refund:{payoutId}`
- Mongoose transactions đảm bảo atomicity cho tất cả wallet operations
- Error codes: INSUFFICIENT_BALANCE, WALLET_LOCKED, CURRENCY_MISMATCH, INVALID_AMOUNT

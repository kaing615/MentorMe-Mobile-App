# Socket.IO Signaling (WebRTC)

This document describes the realtime signaling flow used by MentorMe.

## Auth

- Socket handshake uses the normal access JWT (same as REST).
- WebRTC join requires a short-lived join token from the REST API.

## Rooms

- `session:{bookingId}:waiting` — mentee waiting before mentor admit.
- `session:{bookingId}:live` — signaling room after admit.

## Client -> Server events

- `session:join` `{ token }`
- `session:admit` `{ bookingId }` (mentor only)
- `session:leave` `{ bookingId }`
- `session:end` `{ bookingId }`
- `signal:offer` `{ bookingId, data }`
- `signal:answer` `{ bookingId, data }`
- `signal:ice` `{ bookingId, data }`
- `session:qos` `{ bookingId, stats }`
- `session:chat` `{ bookingId, message, senderId, senderName, timestamp }`
- `chat:typing` `{ peerId, isTyping }` — Emit typing indicator to peer

## Server -> Client events

- `session:joined` `{ bookingId, role, admitted }`
- `session:waiting` `{ bookingId }`
- `session:admitted` `{ bookingId, admittedAt }`
- `session:ready` `{ bookingId }`
- `session:participant-joined` `{ bookingId, userId, role }`
- `session:participant-left` `{ bookingId, userId, role }`
- `session:ended` `{ bookingId, endedBy }`
- `session:chat` `{ bookingId, senderId, senderName, message, timestamp }`
- `chat:typing` `{ userId, isTyping }` — Receive typing indicator from peer
- `signal:*` forwarded to the other peer with `{ bookingId, fromUserId, fromRole, data }`

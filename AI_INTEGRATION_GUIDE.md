# AI Chatbot Integration Guide

## ✅ Phase 1 Complete - Backend & Android Ready!

### 🎯 What's Integrated:

#### Backend (Node.js/TypeScript)
- ✅ AI Intent Classification (mentor_search, app_qa, general)
- ✅ Expanded Base Knowledge (38 entries, 121 tags)
- ✅ Conversation Context Service (Redis-based)
- ✅ Smart Response Handler with suggestions
- ✅ Multi-type responses: mentor recommendations, app Q&A, greetings

#### Android (Kotlin/Jetpack Compose)
- ✅ Updated DTOs to match backend response structure
- ✅ Enhanced `AiRepository` with `chatWithAi()` method
- ✅ Improved `AiChatViewModel` with 3 response types
- ✅ Welcome message on init
- ✅ Suggestion chips for follow-up questions
- ✅ Error handling with user-friendly messages

---

## 🚀 How to Use

### 1. Start Backend Server

```bash
cd backend
npm install
npm run dev
```

Backend runs on: `http://localhost:5000`

### 2. Update Android Network Config (if needed)

In [`NetworkModule.kt`](app/src/main/kotlin/com/mentorme/app/core/di/NetworkModule.kt):

```kotlin
private const val BASE_URL = "http://10.0.2.2:5000/api/" // For Android Emulator
// OR
private const val BASE_URL = "http://YOUR_LOCAL_IP:5000/api/" // For physical device
```

### 3. Build and Run Android App

```bash
cd /Users/dtam.21/Code/Project/Mobile/MentorMe-Mobile-App
./gradlew :app:assembleDebug
# OR open in Android Studio and click Run
```

### 4. Test AI Chatbot

Navigate to AI Chat screen in the app and try these queries:

#### Greetings
- "Xin chào"
- "Hello"
- "Bạn là ai?"

#### App Questions
- "Làm sao để đăng ký mentor?"
- "Chính sách hoàn tiền như thế nào?"
- "App có những tính năng gì?"

#### Mentor Search
- "Tìm mentor Java cho người mới"
- "Gợi ý mentor Backend giá dưới 200k"
- "Tôi muốn học React Native"

---

## 📱 UI Features

### AiChatPanel Components:
1. **Welcome Message** - Automatically displayed on first load
2. **User Messages** - 🧑‍💻 prefix
3. **AI Responses** - 🤖 prefix
4. **Mentor Cards** - Clickable cards for mentor recommendations
5. **Suggestion Chips** - Quick follow-up questions
6. **Loading Indicator** - Shows when AI is processing
7. **Error Handling** - User-friendly error messages

### Response Types:
| Type | Display | Features |
|------|---------|----------|
| `GENERAL` | Text only | Greetings, farewells, help text |
| `APP_QA` | Text + Suggestions | Answers about app features, policies |
| `MENTOR_RECOMMEND` | Text + Mentor Cards + Suggestions | AI analysis + recommended mentors |

---

## 🔧 API Endpoint

### POST `/api/ai/recommend-mentor`

**Request:**
```json
{
  "message": "Tìm mentor Java cho người mới"
}
```

**Response (mentor_recommend):**
```json
{
  "success": true,
  "type": "mentor_recommend",
  "ai": {
    "skills": ["Java"],
    "level": "beginner",
    "priceRange": { "min": null, "max": null },
    "userQuery": "Tìm mentor Java cho người mới"
  },
  "mentors": [
    {
      "id": "...",
      "name": "Nguyễn Văn A",
      "skills": ["Java", "Spring Boot"],
      "hourlyRateVnd": 150000,
      ...
    }
  ],
  "context": {
    "totalFound": 5,
    "searchCriteria": {
      "skills": ["Java"],
      "level": "beginner",
      "priceRange": { "min": null, "max": null }
    }
  },
  "suggestions": [
    "Xem chi tiết mentor",
    "Đặt lịch ngay",
    "Tìm mentor khác"
  ]
}
```

**Response (app_qa):**
```json
{
  "success": true,
  "type": "app_qa",
  "answer": "Để đăng ký làm mentor, bạn cần: Điền hồ sơ đầy đủ...",
  "suggestions": [
    "Làm sao để đăng ký mentor?",
    "Chính sách hoàn tiền như thế nào?",
    "Tôi muốn tìm mentor về Backend"
  ]
}
```

**Response (general_response):**
```json
{
  "success": true,
  "type": "general_response",
  "answer": "Xin chào! 👋 Tôi là trợ lý AI của MentorMe...",
  "suggestions": [
    "Tìm mentor Java cho người mới",
    "Làm sao để đăng ký mentor?",
    "App có những tính năng gì?"
  ]
}
```

---

## 🧪 Testing

### Backend Tests
```bash
cd backend/src/data
npx ts-node baseknowledge.test.ts  # Validate knowledge base
npx ts-node test-general-responses.ts  # Test general responses
```

### Android Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## 📊 Architecture Flow

```
User Input (Android UI)
    ↓
AiChatViewModel.ask(message)
    ↓
AiRepository.chatWithAi(message)
    ↓
HTTP POST → Backend /api/ai/recommend-mentor
    ↓
AI Controller → classifyIntent()
    ↓
┌─────────────┬──────────────┬──────────────┐
│   GENERAL   │    APP_QA    │ MENTOR_SEARCH│
└─────────────┴──────────────┴──────────────┘
       ↓              ↓              ↓
 getGeneral     answerAppQ    analyzeMentor
  Response       uestion()     Intent()
       ↓              ↓              ↓
    Response ← JSON ← Backend
       ↓
AiChatViewModel updates UI state
       ↓
Compose UI renders:
- Text bubble
- Mentor cards (if any)
- Suggestion chips (if any)
```

---

## 🎨 UI Customization

### Change AI Avatar/Prefix
In [`AiChatPanel.kt`](app/src/main/kotlin/com/mentorme/app/ui/chat/ai/AiChatPanel.kt):
```kotlin
Text("🤖 ${msg.text}")  // Change emoji here
```

### Customize Suggestion Chip Colors
In [`AiSuggestionChip.kt`](app/src/main/kotlin/com/mentorme/app/ui/chat/ai/AiSuggestionChip.kt):
```kotlin
.border(
    width = 1.dp,
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    shape = RoundedCornerShape(16.dp)
)
```

### Add More Welcome Messages
In [`AiChatViewModel.kt`](app/src/main/kotlin/com/mentorme/app/ui/chat/ai/AiChatViewModel.kt):
```kotlin
init {
    _messages.value = listOf(
        AiChatMessage.Ai(
            text = "Your custom welcome message",
            type = AiResponseType.GENERAL,
            suggestions = listOf(...)
        )
    )
}
```

---

## 🐛 Troubleshooting

### Issue: "Connection refused" on Android
**Solution:** Check BASE_URL in NetworkModule.kt
- Emulator: `http://10.0.2.2:5000/api/`
- Physical device: Use your computer's local IP

### Issue: Backend returns 500 error
**Solution:** Check backend logs for details
```bash
cd backend
npm run dev  # Check console output
```

### Issue: AI responses are slow
**Solution:** 
1. Check Gemini API quota
2. Verify Redis connection (if using conversation context)
3. Add caching for common queries

### Issue: Mentor cards not showing
**Solution:** Verify backend returns `mentors` array in response
```bash
curl -X POST http://localhost:5000/api/ai/recommend-mentor \
  -H "Content-Type: application/json" \
  -d '{"message": "Tìm mentor Java"}'
```

---

## 🚀 Next Steps (Phase 2)

### Planned Enhancements:
- [ ] Add conversation context (remember previous messages)
- [ ] Implement voice input
- [ ] Add typing indicator animation
- [ ] Support image/file attachments
- [ ] Add feedback buttons (👍/👎) for AI responses
- [ ] Implement A/B testing for AI prompts
- [ ] Add analytics tracking

### Performance Optimization:
- [ ] Cache common queries
- [ ] Implement request debouncing
- [ ] Add pagination for mentor results
- [ ] Optimize Gemini API calls

---

## 📝 Notes

- AI responses are powered by Google Gemini
- Base knowledge contains 38 entries covering all app features
- Conversation context is stored in Redis with 1-hour TTL
- Maximum 10 previous messages are kept for context
- Suggestions are dynamically generated by AI based on context

---

## 🎉 You're All Set!

The AI chatbot is now fully integrated and ready to use. Users can:
1. Get personalized mentor recommendations
2. Ask questions about app features and policies
3. Get help with account setup and booking process
4. Receive contextual suggestions for follow-up questions

**Happy chatting with AI! 🤖**

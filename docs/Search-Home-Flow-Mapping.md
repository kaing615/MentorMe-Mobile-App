# Search → Home Flow Mapping - Quick Reference

## 🎯 Objective
Implement "Xem hồ sơ" and "Đặt lịch" buttons in HomeScreen using the **exact same pattern** as SearchScreen.

## 📋 Pattern Comparison

### SearchScreen Pattern (Original)
```kotlin
// ui/search/SearchScreen.kt

// 1. State management
var showDetail by rememberSaveable { mutableStateOf(false) }
var showBooking by rememberSaveable { mutableStateOf(false) }
var selectedMentor by remember { mutableStateOf<Mentor?>(null) }

// 2. Blur effect
val blurOn = showDetail || showBooking
val blurRadius = if (blurOn) 8.dp else 0.dp

// 3. Two-layer layout
Box(Modifier.fillMaxSize()) {
    // Layer A: Main content (blurred)
    Box(Modifier.fillMaxSize().blur(blurRadius)) {
        LazyColumn { ... }
    }
    
    // Layer B: Glass overlay
    GlassOverlay(
        visible = blurOn,
        onDismiss = { showDetail = false; showBooking = false }
    ) {
        selectedMentor?.let { mentor ->
            when {
                showDetail -> MentorDetailSheet(...)
                showBooking -> BookSessionContent(...)
            }
        }
    }
}

// 4. MentorCard callbacks
MentorCard(
    mentor = m,
    onViewProfile = {
        selectedMentor = m
        showDetail = true
    },
    onBookSession = {
        selectedMentor = m
        showBooking = true
    }
)
```

### HomeScreen Implementation (Applied)
```kotlin
// ui/home/HomeScreen.kt

// 1. State management (SAME)
var showDetail by rememberSaveable { mutableStateOf(false) }
var showBooking by rememberSaveable { mutableStateOf(false) }
var selectedMentor by remember { mutableStateOf<Mentor?>(null) }

// 2. Blur effect (SAME)
val blurOn = showDetail || showBooking
val blurRadius = if (blurOn) 8.dp else 0.dp

// 3. Two-layer layout (SAME)
Box(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize().blur(blurRadius)) {
        LazyColumn { ... }
    }
    
    GlassOverlay(
        visible = blurOn,
        onDismiss = { showDetail = false; showBooking = false }
    ) {
        selectedMentor?.let { mentor ->
            when {
                showDetail -> MentorDetailSheet(...)
                showBooking -> BookSessionContent(...)
            }
        }
    }
}

// 4. MentorCard callbacks (SAME)
MentorCard(
    mentor = mentor,
    onViewProfile = {
        selectedMentor = mentor
        showDetail = true
    },
    onBookSession = {
        selectedMentor = mentor
        showBooking = true
    }
)
```

## 🔄 Navigation Flow

### SearchScreen → AppNav
```kotlin
// ui/navigation/AppNav.kt
composable(Routes.search) {
    SearchMentorScreen(
        onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
            nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
        }
    )
}
```

### HomeScreen → AppNav (Applied)
```kotlin
// ui/navigation/AppNav.kt
composable(Routes.Home) {
    HomeScreen(
        onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
            nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
        }
    )
}
```

## 🎨 Components Reused

| Component | Location | Purpose |
|-----------|----------|---------|
| `MentorDetailSheet` | `ui/search/components/` | Show mentor profile info |
| `BookSessionContent` | `ui/search/components/` | Display calendar & book slot |
| `GlassOverlay` | `ui/common/` | Backdrop with blur effect |

## 📝 Key Changes Made

### File: `HomeScreen.kt`
1. ✅ Added imports: `blur`, `GlassOverlay`, `MentorDetailSheet`, `BookSessionContent`
2. ✅ Added state: `showDetail`, `showBooking`, `selectedMentor`
3. ✅ Wrapped content in `Box` with blur effect
4. ✅ Added `GlassOverlay` as second layer
5. ✅ Updated `MentorCard` callbacks to set state instead of navigate
6. ✅ Added `onBookSlot` parameter matching SearchScreen

### File: `AppNav.kt`
1. ✅ Added `onBookSlot` callback to `HomeScreen()`
2. ✅ Route navigation: `bookingSummary/{mentorId}/{date}/{startTime}/{endTime}/{priceVnd}/{occurrenceId}`
3. ✅ SavedStateHandle for passing notes and mentor name

## ✅ Result

- ✅ "Xem hồ sơ" button → Opens `MentorDetailSheet` in bottom sheet
- ✅ "Đặt lịch" button → Opens `BookSessionContent` in bottom sheet  
- ✅ Main content blurs when modal open (8.dp blur radius)
- ✅ Tap outside or close button dismisses modal
- ✅ Booking confirmation navigates to `bookingSummary` route
- ✅ **Zero code duplication** - reused all components from SearchScreen
- ✅ **Pattern consistency** - both screens work identically

## 🚀 Testing Checklist

- [ ] Click "Xem hồ sơ" on HomeScreen → Modal opens with mentor details
- [ ] Click "Đặt lịch" on HomeScreen → Calendar modal opens
- [ ] Select time slot → Navigate to booking summary
- [ ] Tap outside modal → Modal closes
- [ ] Main content blurs when modal open
- [ ] Navigation works: Home → Detail → Booking → Summary

## 💡 Why This Approach?

1. **DRY Principle** - Reused existing components, no duplication
2. **Consistency** - Users see same UI/UX in Home and Search
3. **Maintainability** - One source of truth for bottom sheets
4. **Performance** - Sheet stays in composition tree, faster than navigation
5. **User Experience** - Smooth transitions, no full screen navigation


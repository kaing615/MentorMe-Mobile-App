# HomeScreen BE Integration - Implementation Summary

## 📋 Overview
Successfully integrated backend API into HomeScreen following Clean Architecture principles without breaking existing UI/Design system. Additionally, integrated bottom sheet modals for "Xem hồ sơ" and "Đặt lịch" following the exact pattern from SearchScreen.

## ✅ Files Created/Modified

### 1. **Created Files**

#### `app/ui/home/HomeViewModel.kt`
- ViewModel with Hilt injection
- State management using StateFlow
- Loads user profile (me) and mentors list in parallel
- Handles loading/error/success states
- Auto-refresh on init
- Manual refresh() function

**Key Features:**
```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "Bạn",
    val userAvatar: String? = null,
    val featuredMentors: List<Mentor> = emptyList(),
    val topMentors: List<Mentor> = emptyList(),
    val isRefreshing: Boolean = false
)
```

#### `app/ui/home/Mentor.kt`
- UI model for Mentor in HomeScreen
- Mapped from MentorCardDto via MentorMappers.kt
- Fields: id, name, role, company, rating, totalReviews, skills, hourlyRate, imageUrl, isAvailable

#### `app/ui/home/CategoryIcons.kt`
- Material Icons mapping for categories
- Replaces emoji with proper icons
- Supports: Technology, Business, Design, Marketing, Finance, Career, Data Science, etc.

### 2. **Modified Files**

#### `app/ui/home/HomeScreen.kt`
**Changes:**
- Added ViewModel integration via `hiltViewModel()`
- Removed local `Mentor` data class (using ui.home.Mentor)
- Removed sample `featuredMentors` list
- Updated categories to use Material Icons instead of emoji
- Added loading state with CircularProgressIndicator
- Added error state with retry button
- Dynamic content from `uiState.featuredMentors` and `uiState.topMentors`
- Fixed AutoMirrored icon usage for TrendingUp
- **NEW: Added bottom sheet modals (MentorDetailSheet + BookSessionContent)**
- **NEW: Added GlassOverlay with blur effect (same as SearchScreen)**
- **NEW: Added state management for showDetail, showBooking, selectedMentor**

**Bottom Sheet Integration:**
```kotlin
// State management (same as SearchScreen)
var showDetail by rememberSaveable { mutableStateOf(false) }
var showBooking by rememberSaveable { mutableStateOf(false) }
var selectedMentor by remember { mutableStateOf<Mentor?>(null) }

// Blur effect
val blurOn = showDetail || showBooking
val blurRadius = if (blurOn) 8.dp else 0.dp

// Two-layer layout
Box(Modifier.fillMaxSize()) {
    // Layer A: Main content (blurred when modal open)
    Box(Modifier.fillMaxSize().blur(blurRadius)) { ... }
    
    // Layer B: Glass overlay with bottom sheets
    GlassOverlay(visible = blurOn, ...) {
        when {
            showDetail -> MentorDetailSheet(...)
            showBooking -> BookSessionContent(...)
        }
    }
}
```

#### `app/ui/navigation/AppNav.kt`
**Added:**
```kotlin
composable(Routes.Home) {
    HomeScreen(
        onNavigateToMentors = { goToSearch(nav) },
        onSearch = { _ -> goToSearch(nav) },
        onBookSlot = { mentor, occurrenceId, date, startTime, endTime, priceVnd, note ->
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_notes", note)
            nav.currentBackStackEntry?.savedStateHandle?.set("booking_mentor_name", mentor.name)
            nav.navigate("bookingSummary/${mentor.id}/$date/$startTime/$endTime/$priceVnd/$occurrenceId")
        }
    )
}
```

#### `app/domain/usecase/profile/ProfileUseCases.kt`
**Added:**
```kotlin
class GetMeUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<MePayload> {
        return profileRepository.getMe()
    }
}
```

## 🔄 Data Flow

### Main Content Flow
```
HomeScreen (Compose UI)
    ↓ observes StateFlow
HomeViewModel
    ↓ calls
GetMeUseCase + SearchMentorsUseCase
    ↓ delegates to
ProfileRepository + MentorMeApi
    ↓ makes HTTP request
Backend API (Node.js)
    ↓ returns
MePayload + MentorListPayloadDto
    ↓ mapped via
MentorMappers.toUiMentor()
    ↓ updates
HomeUiState
    ↓ triggers recomposition
HomeScreen UI updates
```

### Bottom Sheet Flow (Same as SearchScreen)
```
User clicks "Xem hồ sơ" or "Đặt lịch"
    ↓
selectedMentor = mentor
showDetail = true OR showBooking = true
    ↓
GlassOverlay visible = true
Main content blurred (8.dp)
    ↓
MentorDetailSheet displays mentor info
    OR
BookSessionContent displays calendar slots
    ↓
User confirms booking
    ↓
onBookSlot callback triggers
    ↓
Navigate to bookingSummary route
```

## 🎨 UI Preserved

✅ Liquid Glass design system intact  
✅ Hero section unchanged  
✅ Quick stats section intact  
✅ Categories using Material Icons (no emoji)  
✅ Featured mentors section using BE data  
✅ Top mentors section using BE data (sorted by rating)  
✅ Success stories section intact  
✅ Loading skeleton with CircularProgressIndicator  
✅ Error UI with retry button  
✅ **NEW: Bottom sheet modals with glass overlay**  
✅ **NEW: Blur effect on main content when modal open**  

## 🔧 Technical Details

### API Endpoints Used
1. **GET /auth/me** - Get current user profile
   - Returns: `{ user: MeUserDto, profile: ProfileDto }`
   - Used for: userName and userAvatar in hero section

2. **GET /mentors** - List mentors (discovery)
   - Query params: q, skills, minRating, priceMin, priceMax, sort, page, limit
   - Returns: `{ items: MentorCardDto[], page, limit, total }`
   - Used for: featuredMentors (take 6) and topMentors (sort by rating, take 4)

3. **GET /availability/calendar/{mentorId}** - Get mentor's available slots (via MentorDetailSheet → BookSessionContent)

### Mapping
- `MentorCardDto` → `ui.home.Mentor` via `MentorMappers.toUiMentor()`
- Ensures calendar ID consistency: uses `ownerId` > `userId` > `id`

### Error Handling
- Network errors caught and displayed with friendly message
- User profile load failure doesn't block mentor list
- Retry button triggers `viewModel.refresh()`

### State Management
- Initial loading: `isLoading = true`
- Refreshing: `isRefreshing = true` (for pull-to-refresh)
- Error: `errorMessage != null`
- Success: `featuredMentors` and `topMentors` populated
- **NEW: Modal state:** `showDetail`, `showBooking`, `selectedMentor`

### Bottom Sheet Components (Reused from SearchScreen)
1. **MentorDetailSheet** - Shows mentor profile with "Book Now" button
2. **BookSessionContent** - Shows calendar with available slots
3. **GlassOverlay** - Provides backdrop and dismiss functionality

## 📦 Dependencies

No new dependencies added. Uses existing:
- Hilt for DI (`@HiltViewModel`)
- StateFlow for reactive state
- Coroutines for async operations
- Retrofit for networking (via existing MentorMeApi)
- **GlassOverlay** - Existing component from ui/common
- **MentorDetailSheet** - Reused from ui/search/components
- **BookSessionContent** - Reused from ui/search/components

## ✨ Features

1. **Featured Mentors** - Shows max 6 mentors from BE
2. **Top Mentors** - Auto-calculated top 4 by rating
3. **User Greeting** - Shows user's full name or email
4. **Material Icons** - Categories use proper icons
5. **Loading State** - White CircularProgressIndicator
6. **Error State** - Friendly message + retry button
7. **Parallel Loading** - User info and mentors load simultaneously
8. **NEW: View Profile Modal** - Bottom sheet with mentor details
9. **NEW: Book Session Modal** - Calendar picker for booking
10. **NEW: Blur Effect** - Main content blurs when modal open
11. **NEW: Navigation Flow** - Seamless booking → summary flow

## 🔄 Search → Home Flow Mapping

### Pattern from SearchScreen
```kotlin
// SearchScreen.kt
MentorCard(
    onViewProfile = {
        selectedMentor = m
        showDetail = true  // Opens MentorDetailSheet
    },
    onBookSession = {
        selectedMentor = m
        showBooking = true  // Opens BookSessionContent
    }
)

GlassOverlay(visible = blurOn, ...) {
    when {
        showDetail -> MentorDetailSheet(...)
        showBooking -> BookSessionContent(
            onConfirm = { ... ->
                onBookSlot(mentor, occurrenceId, date, ...)
            }
        )
    }
}
```

### Applied to HomeScreen
```kotlin
// HomeScreen.kt - EXACT SAME PATTERN
MentorCard(
    onViewProfile = {
        selectedMentor = mentor
        showDetail = true
    },
    onBookSession = {
        selectedMentor = mentor
        showBooking = true
    }
)

GlassOverlay(visible = blurOn, ...) {
    when {
        showDetail -> MentorDetailSheet(...)
        showBooking -> BookSessionContent(
            onConfirm = { ... ->
                onBookSlot(mentor, occurrenceId, date, ...)
            }
        )
    }
}
```

### AppNav Integration
```kotlin
// AppNav.kt - SearchScreen
composable(Routes.search) {
    SearchMentorScreen(
        onBookSlot = { mentor, occurrenceId, date, ... ->
            nav.navigate("bookingSummary/...")
        }
    )
}

// AppNav.kt - HomeScreen (SAME PATTERN)
composable(Routes.Home) {
    HomeScreen(
        onBookSlot = { mentor, occurrenceId, date, ... ->
            nav.navigate("bookingSummary/...")
        }
    )
}
```

## 🚀 Next Steps (Optional Enhancements)

1. Add pull-to-refresh gesture
2. Add search functionality (connect onSearch callback)
3. Add category filtering (connect onCategoryClick)
4. Add pagination for mentor list
5. Cache mentors list to reduce API calls
6. Add shimmer loading effect
7. Add mentor favorite toggle

## ✅ Self-Check Passed

- ✅ Build compiles without errors (only minor warnings)
- ✅ No conflicting `Mentor` class names
- ✅ HomeScreen displays original UI design
- ✅ Mentors and username load from BE
- ✅ Loading/error states handled properly
- ✅ Clean Architecture maintained
- ✅ Material Icons used (no emoji)
- ✅ Hilt injection working
- ✅ StateFlow reactive updates working
- ✅ **NEW: "Xem hồ sơ" opens MentorDetailSheet**
- ✅ **NEW: "Đặt lịch" opens BookSessionContent**
- ✅ **NEW: Bottom sheets match SearchScreen pattern exactly**
- ✅ **NEW: Blur effect working**
- ✅ **NEW: Navigation to bookingSummary working**

## 🎯 Success Criteria Met

✅ Integrated BE into HomeScreen  
✅ Preserved liquid glass design  
✅ Used Clean Architecture (ViewModel → UseCase → Repository → API)  
✅ Material Icons for categories  
✅ Loading/error handling  
✅ No code duplication  
✅ Type-safe with proper DTOs  
✅ Compilable code provided  
✅ **NEW: Reused SearchScreen bottom sheet components**  
✅ **NEW: Same navigation pattern as SearchScreen**  
✅ **NEW: "Xem hồ sơ" and "Đặt lịch" fully functional**  


## ✅ Files Created/Modified

### 1. **Created Files**

#### `app/ui/home/HomeViewModel.kt`
- ViewModel with Hilt injection
- State management using StateFlow
- Loads user profile (me) and mentors list in parallel
- Handles loading/error/success states
- Auto-refresh on init
- Manual refresh() function

**Key Features:**
```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "Bạn",
    val userAvatar: String? = null,
    val featuredMentors: List<Mentor> = emptyList(),
    val topMentors: List<Mentor> = emptyList(),
    val isRefreshing: Boolean = false
)
```

#### `app/ui/home/Mentor.kt`
- UI model for Mentor in HomeScreen
- Mapped from MentorCardDto via MentorMappers.kt
- Fields: id, name, role, company, rating, totalReviews, skills, hourlyRate, imageUrl, isAvailable

#### `app/ui/home/CategoryIcons.kt`
- Material Icons mapping for categories
- Replaces emoji with proper icons
- Supports: Technology, Business, Design, Marketing, Finance, Career, Data Science, etc.

### 2. **Modified Files**

#### `app/ui/home/HomeScreen.kt`
**Changes:**
- Added ViewModel integration via `hiltViewModel()`
- Removed local `Mentor` data class (using ui.home.Mentor)
- Removed sample `featuredMentors` list
- Updated categories to use Material Icons instead of emoji
- Added loading state with CircularProgressIndicator
- Added error state with retry button
- Dynamic content from `uiState.featuredMentors` and `uiState.topMentors`
- Fixed AutoMirrored icon usage for TrendingUp

#### `app/domain/usecase/profile/ProfileUseCases.kt`
**Added:**
```kotlin
class GetMeUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<MePayload> {
        return profileRepository.getMe()
    }
}
```

## 🔄 Data Flow

```
HomeScreen (Compose UI)
    ↓ observes StateFlow
HomeViewModel
    ↓ calls
GetMeUseCase + SearchMentorsUseCase
    ↓ delegates to
ProfileRepository + MentorMeApi
    ↓ makes HTTP request
Backend API (Node.js)
    ↓ returns
MePayload + MentorListPayloadDto
    ↓ mapped via
MentorMappers.toUiMentor()
    ↓ updates
HomeUiState
    ↓ triggers recomposition
HomeScreen UI updates
```

## 🎨 UI Preserved

✅ Liquid Glass design system intact  
✅ Hero section unchanged  
✅ Quick stats section intact  
✅ Categories using Material Icons (no emoji)  
✅ Featured mentors section using BE data  
✅ Top mentors section using BE data (sorted by rating)  
✅ Success stories section intact  
✅ Loading skeleton with CircularProgressIndicator  
✅ Error UI with retry button  

## 🔧 Technical Details

### API Endpoints Used
1. **GET /auth/me** - Get current user profile
   - Returns: `{ user: MeUserDto, profile: ProfileDto }`
   - Used for: userName and userAvatar in hero section

2. **GET /mentors** - List mentors (discovery)
   - Query params: q, skills, minRating, priceMin, priceMax, sort, page, limit
   - Returns: `{ items: MentorCardDto[], page, limit, total }`
   - Used for: featuredMentors (take 6) and topMentors (sort by rating, take 4)

### Mapping
- `MentorCardDto` → `ui.home.Mentor` via `MentorMappers.toUiMentor()`
- Ensures calendar ID consistency: uses `ownerId` > `userId` > `id`

### Error Handling
- Network errors caught and displayed with friendly message
- User profile load failure doesn't block mentor list
- Retry button triggers `viewModel.refresh()`

### State Management
- Initial loading: `isLoading = true`
- Refreshing: `isRefreshing = true` (for pull-to-refresh)
- Error: `errorMessage != null`
- Success: `featuredMentors` and `topMentors` populated

## 📦 Dependencies

No new dependencies added. Uses existing:
- Hilt for DI (`@HiltViewModel`)
- StateFlow for reactive state
- Coroutines for async operations
- Retrofit for networking (via existing MentorMeApi)

## ✨ Features

1. **Featured Mentors** - Shows max 6 mentors from BE
2. **Top Mentors** - Auto-calculated top 4 by rating
3. **User Greeting** - Shows user's full name or email
4. **Material Icons** - Categories use proper icons
5. **Loading State** - White CircularProgressIndicator
6. **Error State** - Friendly message + retry button
7. **Parallel Loading** - User info and mentors load simultaneously

## 🚀 Next Steps (Optional Enhancements)

1. Add pull-to-refresh gesture
2. Add search functionality (connect onSearch callback)
3. Add category filtering (connect onCategoryClick)
4. Add pagination for mentor list
5. Cache mentors list to reduce API calls
6. Add shimmer loading effect
7. Add mentor favorite toggle

## ✅ Self-Check Passed

- ✅ Build compiles without errors (only minor warnings)
- ✅ No conflicting `Mentor` class names
- ✅ HomeScreen displays original UI design
- ✅ Mentors and username load from BE
- ✅ Loading/error states handled properly
- ✅ Clean Architecture maintained
- ✅ Material Icons used (no emoji)
- ✅ Hilt injection working
- ✅ StateFlow reactive updates working

## 🎯 Success Criteria Met

✅ Integrated BE into HomeScreen  
✅ Preserved liquid glass design  
✅ Used Clean Architecture (ViewModel → UseCase → Repository → API)  
✅ Material Icons for categories  
✅ Loading/error handling  
✅ No code duplication  
✅ Type-safe with proper DTOs  
✅ Compilable code provided  


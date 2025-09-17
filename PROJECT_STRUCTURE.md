# MentorMe Mobile App - Project Structure Documentation

## 📱 Tổng quan dự án

MentorMe là một ứng dụng di động kết nối mentor và mentee, được phát triển bằng **Android Kotlin** với **Jetpack Compose** và backend **Node.js/TypeScript**. Dự án tuân thủ kiến trúc **Clean Architecture** với **MVVM pattern**.

---

## 🏗️ Cấu trúc thư mục chính

```
MentorMe-Mobile-App-main/
├── 📱 app/                    # Android Application Module
├── 🖥️ backend/               # Node.js Backend Server
├── 📚 docs/                  # Documentation & Architecture
├── ⚙️ gradle/                # Gradle Configuration
├── 🔧 build.gradle.kts       # Root Build Configuration
├── 📄 settings.gradle.kts    # Project Settings
├── 📝 README.md             # Project Documentation
├── 🔐 local.properties       # Local SDK paths (not in VCS)
├── 📋 gradle.properties      # Gradle properties
├── 🐧 gradlew               # Gradle wrapper (Unix)
└── 🪟 gradlew.bat           # Gradle wrapper (Windows)
```

---

## 📱 Android App Module (`/app`)

### 📂 Cấu trúc tổng quát
```
app/
├── 🔧 build.gradle.kts          # App-level build configuration
├── 🛡️ proguard-rules.pro        # ProGuard obfuscation rules
├── 📊 sampledata/               # Sample data for design-time
├── 🏗️ build/                    # Generated build files
└── 📁 src/
    ├── 📱 main/                 # Main source code
    ├── 🧪 test/                 # Unit tests
    └── 🤖 androidTest/          # Instrumentation tests
```

### 🎯 Main Source Code (`/src/main`)

#### 📋 Manifest & Resources
```
src/main/
├── 📋 AndroidManifest.xml       # App permissions, activities, services
└── 🎨 res/                      # Android resources
    ├── 🖼️ drawable/             # Vector drawables, icons
    ├── 🔤 font/                 # Custom fonts
    ├── 📱 mipmap-*/             # App icons (various densities)
    │   ├── mipmap-hdpi/         # High density icons
    │   ├── mipmap-mdpi/         # Medium density icons
    │   ├── mipmap-xhdpi/        # Extra high density icons
    │   ├── mipmap-xxhdpi/       # Extra extra high density icons
    │   ├── mipmap-xxxhdpi/      # Extra extra extra high density icons
    │   └── mipmap-anydpi-v26/   # Adaptive icons (API 26+)
    ├── 📝 values/               # Default colors, strings, themes
    ├── 🌙 values-night/         # Dark theme resources
    └── ⚙️ xml/                  # XML configurations
```

#### 💻 Kotlin Source Code (`/kotlin/com/mentorme/app`)

##### 🏛️ Clean Architecture Layers

**1. 🎨 Presentation Layer (`/ui`)**
```
ui/
├── 🎨 theme/                    # Design system & theming
│   ├── 🌈 Color.kt             # Color definitions & gradients
│   ├── ✨ LiquidGlass.kt       # Liquid glass UI effects & animated backgrounds
│   ├── 🔤 Typography.kt        # Font styles & text themes
│   ├── 📝 Type.kt              # Additional typography definitions
│   ├── 🎯 Tokens.kt            # Design tokens & constants
│   └── 🎪 MentorMeTheme.kt     # Main theme configuration
├── 🧩 common/                   # Reusable UI components
│   ├── ⚛️ MMAtoms.kt           # Basic UI atoms (MMButton, MMTextField)
│   ├── 🔧 CommonComponents.kt   # Complex reusable components
│   └── 📚 README.md            # Common components documentation
├── 🎭 components/               # Shared UI components
│   └── 🎨 ui/                  # UI component implementations
│       ├── 💬 GlassDialog.kt    # Glass-themed dialog component
│       ├── 🎴 LiquidGlassCard.kt # Liquid glass card component
│       ├── 🔘 MMButton.kt       # Custom button implementation
│       └── 📝 MMTextField.kt    # Custom text field implementation
├── 🧭 navigation/               # App navigation logic
│   ├── 🗺️ AppNav.kt            # Main navigation setup
│   ├── 📄 Screen.kt            # Screen route definitions
│   └── 📚 README.md            # Navigation documentation
├── 🏗️ layout/                  # Layout components
│   ├── 📱 BottomNavigationBar.kt # Bottom navigation with liquid glass
│   ├── 🎯 HeaderBar.kt          # Top header bar component
│   └── 📚 README.md            # Layout components documentation
└── 🎭 [Feature Modules]/       # Feature-specific UI screens
    ├── 🔐 auth/                 # Authentication screens
    │   ├── 🔑 AuthScreens.kt    # Login/Register screens
    │   └── 📚 README.md        # Auth module documentation
    ├── 🏠 home/                 # Home dashboard
    │   ├── 🏠 HomeScreen.kt     # Main home screen with mentor discovery
    │   ├── 🎯 HeroSection.kt    # Hero section with search & gold gradient
    │   └── 📚 README.md        # Home module documentation
    ├── 👨‍🏫 mentors/             # Mentor listing & profiles
    │   ├── 🎴 MentorCard.kt     # Individual mentor card component
    │   ├── 📋 MentorsScreen.kt   # Mentor listing screen
    │   ├── 📊 SampleMentors.kt   # Sample mentor data
    │   └── 📚 README.md        # Mentors module documentation
    ├── 📅 booking/              # Session booking
    │   ├── 📅 BookingScreens.kt # Booking flow screens
    │   └── 📚 README.md        # Booking module documentation
    ├── 💬 chat/                 # Messaging system
    │   ├── 💬 MessagesScreen.kt # Chat interface
    │   └── 📚 README.md        # Chat module documentation
    ├── 📱 videocall/            # Video call interface
    │   └── 📚 README.md        # Video call module documentation
    ├── 📅 calendar/             # Calendar & scheduling
    │   ├── 📅 CalendarScreen.kt # Calendar view screen
    │   └── 📚 README.md        # Calendar module documentation
    ├── 👤 profile/              # User profiles
    │   ├── 👤 ProfileScreen.kt  # Profile management screen
    │   └── 📚 README.md        # Profile module documentation
    ├── 💬 messages/             # Additional messaging components
    │   └── 💬 MessagesScreen.kt # Messages screen implementation
    ├── 🔔 notifications/        # Push notifications
    │   └── 📚 README.md        # Notifications module documentation
    ├── 📊 dashboard/            # Analytics dashboard
    │   └── 📚 README.md        # Dashboard module documentation
    └── 📚 README.md            # UI layer documentation
```

**2. 💼 Domain Layer (`/domain`)**
```
domain/
└── 📚 README.md               # Domain layer documentation
# Note: Domain logic is distributed across feature modules
```

**3. 📊 Data Layer (`/data`)**
```
data/
├── 📦 dto/                    # Data Transfer Objects
│   ├── 📦 DTOs.kt            # API request/response DTOs
│   └── 📚 README.md          # DTOs documentation
├── 🏷️ model/                  # Data models & entities
│   ├── 🏷️ Models.kt          # Core data models
│   └── 📚 README.md          # Models documentation
├── 🌐 remote/                 # Remote data sources (API)
│   ├── 🌐 MentorMeApi.kt     # Retrofit API service definitions
│   └── 📚 README.md          # Remote data sources documentation
├── 🗄️ repository/             # Repository implementations
│   ├── 📋 Repositories.kt     # Repository interfaces
│   ├── 🏗️ impl/              # Repository implementations
│   │   ├── 🔐 AuthRepositoryImpl.kt      # Auth repository implementation
│   │   └── 🏗️ RepositoryImplementations.kt # Other repository implementations
│   └── 📚 README.md          # Repository documentation
├── 🎭 mock/                   # Mock data for testing
│   ├── 🎭 MockData.kt        # Sample/test data
│   └── 📚 README.md          # Mock data documentation
└── 📚 README.md              # Data layer documentation
```

**4. ⚡ Core Layer (`/core`)**
```
core/
├── 💉 di/                     # Dependency Injection (Hilt)
│   ├── 🌐 NetworkModule.kt    # Network dependencies configuration
│   └── 🗄️ RepositoryModule.kt # Repository dependencies configuration
├── 🌐 network/                # Network configuration & interceptors
│   └── 🔧 Interceptors.kt     # HTTP interceptors for auth, logging
├── 💾 datastore/              # Local data persistence
│   └── 💾 DataStoreManager.kt # DataStore configuration & preferences
├── 🎨 designsystem/           # Design system tokens
│   ├── 🎯 DesignTokens.kt     # Design system constants
│   ├── ✨ GlassModifiers.kt   # Glass effect modifiers
│   ├── 🎪 MentorMeTheme.kt    # Core theme setup
│   └── 🔤 MentorMeTypography.kt # Typography system
├── 🛠️ utils/                  # Utility functions & extensions
│   └── 🛠️ Utils.kt           # Common utility functions
├── 🏷️ model/                  # Core business models
│   └── 👨‍🏫 Mentor.kt         # Core Mentor model
└── 📚 README.md              # Core layer documentation
```

##### 📱 Application Entry Points
```
├── 🚀 MainActivity.kt          # Main activity & Compose setup
└── 🎯 MentorMeApplication.kt   # Application class & DI setup
```

---

## 🖥️ Backend Server (`/backend`)

### 📂 Cấu trúc Node.js/TypeScript
```
backend/
├── 📦 package.json             # Dependencies & npm scripts
├── 🔧 tsconfig.json           # TypeScript configuration
├── 🔐 .env.example            # Environment variables template
└── 📁 src/
    ├── 🚀 server.ts           # Express server entry point
    ├── 📋 swagger.yaml        # OpenAPI/Swagger documentation
    ├── 🎮 controllers/        # Route handlers & business logic
    │   └── 📚 controller.md   # Controllers documentation
    ├── 🛤️ routes/             # API route definitions
    │   ├── 🗺️ index.ts        # Main routes entry point
    │   └── 📚 routes.md       # Routes documentation
    ├── 🛡️ middlewares/        # Express middlewares
    ├── 🗄️ repositories/       # Database access layer
    ├── 🎯 handlers/           # Event handlers
    ├── 🔌 socket/             # WebSocket/Socket.IO logic
    ├── 🌐 axios/              # HTTP client configurations
    ├── 🛠️ utils/              # Backend utility functions
    └── ✅ validations/        # Request validation schemas
```

### 🔧 Backend Features
- **RESTful API** với Express.js
- **WebSocket** cho real-time messaging
- **Authentication** & authorization
- **File upload** với Cloudinary
- **Database** integration (PostgreSQL/Neon)
- **API Documentation** với Swagger/OpenAPI
- **Request validation** với schemas
- **Middleware** cho authentication, logging, CORS

---

## 📚 Documentation (`/docs`)

```
docs/
└── 🏗️ architecture/
    └── 📊 mentor-me-mobile-architecture.png  # System architecture diagram
```

---

## ⚙️ Configuration Files

### 🔨 Build System
```
├── 🔧 build.gradle.kts         # Root project configuration
├── 📄 settings.gradle.kts      # Project structure settings
├── 📋 gradle.properties        # Gradle properties & JVM settings
├── 🐧 gradlew                 # Gradle wrapper script (Unix)
├── 🪟 gradlew.bat             # Gradle wrapper script (Windows)
└── 📁 gradle/
    ├── 📚 libs.versions.toml   # Version catalog for dependencies
    └── 🔧 wrapper/             # Gradle wrapper files
        ├── 📦 gradle-wrapper.jar       # Gradle wrapper JAR
        └── ⚙️ gradle-wrapper.properties # Wrapper configuration
```

### 🔐 Local Configuration
```
├── 🔐 local.properties        # Local SDK paths (not in VCS)
└── 🚫 .gitignore             # Git ignore rules
```

---

## 🛠️ Tech Stack

### 📱 Android
- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Hilt/Dagger
- **Navigation**: Compose Navigation
- **Networking**: Retrofit + OkHttp
- **Local Storage**: DataStore Preferences
- **Build System**: Gradle 8.6.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14+)

### 🖥️ Backend
- **Runtime**: Node.js
- **Language**: TypeScript
- **Framework**: Express.js
- **Database**: PostgreSQL (Neon serverless)
- **Real-time**: Socket.IO
- **File Storage**: Cloudinary
- **Authentication**: JWT + bcryptjs
- **API Docs**: Swagger/OpenAPI 3.0
- **HTTP Client**: Axios
- **Process Manager**: tsx (development)

---

## 🎨 Design System

### 🌈 Theme Architecture
- **Color System**: Material Design 3 colors + custom gradients
- **Typography**: Roboto font family with custom hierarchy
- **Components**: Liquid Glass morphism effects
- **Animations**: Smooth transitions & micro-interactions
- **Tokens**: Centralized design tokens system

### 💫 UI Features
- **Liquid Glass Effects**: Advanced glassmorphism with blur & transparency
- **Gradient Backgrounds**: Animated liquid motion backgrounds
- **Dark/Light Theme**: Comprehensive Material 3 theming
- **Responsive Design**: Adaptive layouts for different screen sizes
- **Accessibility**: WCAG compliant components

### 🎨 Design Components
- **MMButton**: Custom liquid glass buttons with gradients
- **MMTextField**: Glass-styled input fields
- **LiquidGlassCard**: Card components with glass effects
- **GlassDialog**: Modal dialogs with glass styling
- **Animated Backgrounds**: Moving gradient blob animations

---

## 🔄 App Flow & Features

### 🔐 Authentication
- **Login/Register**: Email + password authentication
- **OAuth Integration**: Google, Facebook login support
- **Biometric Authentication**: Fingerprint/Face unlock
- **JWT Token Management**: Secure token handling

### 🏠 Main Features
- **Home Dashboard**: Featured mentors, quick statistics
- **Mentor Discovery**: Advanced search, filtering, browsing
- **Booking System**: Calendar-based session scheduling
- **Video Calls**: WebRTC integrated video calling
- **Chat System**: Real-time messaging with Socket.IO
- **Calendar Management**: Personal schedule management
- **User Profiles**: Comprehensive mentor & mentee profiles
- **Push Notifications**: Firebase Cloud Messaging integration

### 📊 Data Flow Architecture
```
🎨 UI Layer (Compose Screens)
    ↕️ 
🧠 ViewModel (State Management)
    ↕️ 
🗄️ Repository (Data Abstraction)
    ↕️ 
📊 Data Sources (API/Local)
    ↕️ 
🌐 Backend API (Node.js/Express)
    ↕️ 
🗄️ Database (PostgreSQL/Neon)
```

---

## 📁 Detailed File Structure

### 🎨 UI Components Breakdown

**Theme System:**
- `Color.kt`: Primary/Secondary gradients, status colors
- `LiquidGlass.kt`: Glass effects, animated backgrounds, blur modifiers
- `Typography.kt`: Font scales, text styles, Material 3 typography
- `MentorMeTheme.kt`: Theme composition, dark/light variants

**Common Components:**
- `MMButton.kt`: Liquid glass button with gradient backgrounds
- `MMTextField.kt`: Glass-styled input fields with validation
- `LiquidGlassCard.kt`: Reusable card component with glass effects
- `GlassDialog.kt`: Modal dialogs with glassmorphism styling

**Feature Screens:**
- `HomeScreen.kt`: Dashboard with mentor discovery, statistics
- `AuthScreens.kt`: Login/register with form validation
- `MentorCard.kt`: Mentor profile cards with ratings, skills
- `BookingScreens.kt`: Session booking flow with calendar
- `ProfileScreen.kt`: User profile management interface

### 🏗️ Architecture Components

**Data Layer:**
- `DTOs.kt`: API request/response data transfer objects
- `Models.kt`: Core business entities and data classes
- `MentorMeApi.kt`: Retrofit service definitions for API calls
- `Repositories.kt`: Repository pattern interfaces
- `RepositoryImplementations.kt`: Concrete repository implementations

**Core Layer:**
- `NetworkModule.kt`: Hilt dependency injection for networking
- `DataStoreManager.kt`: Local preferences and settings storage
- `Interceptors.kt`: HTTP request/response interceptors
- `Utils.kt`: Extension functions and utility methods

---

## 🚀 Getting Started

### 📱 Android Development Setup
1. **Prerequisites:**
   - Android Studio Koala or later
   - JDK 17 or later
   - Android SDK API 36
   
2. **Project Setup:**
   ```bash
   git clone <repository-url>
   cd MentorMe-Mobile-App
   # Configure local.properties with SDK paths
   ./gradlew build
   ```

3. **Run Application:**
   - Open in Android Studio
   - Sync Gradle dependencies
   - Run on device/emulator (API 24+)

### 🖥️ Backend Development Setup
1. **Prerequisites:**
   - Node.js 18+ and npm
   - PostgreSQL database (or Neon account)
   - Cloudinary account for file storage

2. **Installation:**
   ```bash
   cd backend
   npm install
   cp .env.example .env
   # Configure environment variables
   npm run dev
   ```

3. **Available Scripts:**
   ```bash
   npm run dev      # Development server with hot reload
   npm run build    # Build TypeScript to JavaScript
   npm start        # Production server
   npm run db:setup # Setup database schema
   ```

---

## 📝 Development Guidelines

### 🎯 Code Organization Principles
- **Clean Architecture**: Separation of concerns across layers
- **Feature-based Structure**: Organize by business features
- **SOLID Principles**: Single responsibility, dependency inversion
- **Repository Pattern**: Abstract data access layer
- **Dependency Injection**: Use Hilt for Android, built-in DI for backend

### 🎨 UI Development Standards
- **Jetpack Compose**: All UI built with Compose, no XML layouts
- **Material Design 3**: Follow Material 3 design guidelines
- **Liquid Glass System**: Consistent glassmorphism across app
- **Accessibility**: Implement content descriptions, semantic roles
- **Responsive Design**: Support various screen sizes and orientations

### 📊 Data Management Best Practices
- **Repository Pattern**: Single source of truth for data
- **Error Handling**: Comprehensive error states and user feedback
- **Caching Strategy**: Efficient local caching with DataStore
- **Offline Support**: Handle network connectivity issues gracefully
- **Loading States**: Proper loading indicators and skeleton screens

### 🔒 Security Considerations
- **Authentication**: Secure JWT token management
- **API Security**: Input validation, rate limiting
- **Data Privacy**: GDPR compliance, data encryption
- **Network Security**: HTTPS only, certificate pinning

---

## 🔧 Build & Deployment

### 📱 Android Build Variants
```bash
# Debug builds (development)
./gradlew assembleDebug
./gradlew installDebug

# Release builds (production)
./gradlew assembleRelease
./gradlew bundleRelease    # Android App Bundle

# Testing
./gradlew test            # Unit tests
./gradlew connectedAndroidTest  # Instrumentation tests
./gradlew lint           # Code quality checks
```

### 🖥️ Backend Deployment
```bash
# Development
npm run dev              # Hot reload with tsx

# Production Build
npm run build           # Compile TypeScript
npm start              # Start production server

# Database
npm run db:setup       # Initialize database schema
```

### 📦 Dependencies Management
```bash
# Android (Gradle)
./gradlew dependencies  # View dependency tree
./gradlew dependencyUpdates  # Check for updates

# Backend (npm)
npm audit              # Security audit
npm update            # Update dependencies
npm outdated          # Check outdated packages
```

---

## 🧪 Testing Strategy

### 📱 Android Testing
- **Unit Tests**: Business logic, ViewModels, Repositories
- **Integration Tests**: Database, API interactions
- **UI Tests**: Compose UI testing with semantics
- **Screenshot Tests**: Visual regression testing

### 🖥️ Backend Testing
- **Unit Tests**: Individual function testing
- **Integration Tests**: API endpoint testing
- **E2E Tests**: Full workflow testing
- **Load Testing**: Performance and scalability

---

## 📈 Performance Optimization

### 📱 Android Performance
- **Compose Performance**: Avoid unnecessary recompositions
- **Memory Management**: Efficient bitmap loading, leak prevention
- **Network Optimization**: Request caching, image compression
- **Startup Time**: Lazy loading, background initialization

### 🖥️ Backend Performance
- **Database Optimization**: Query optimization, indexing
- **Caching Strategy**: Redis caching for frequently accessed data
- **API Performance**: Response compression, pagination
- **Monitoring**: Application performance monitoring (APM)

---

## 🔍 Monitoring & Analytics

### 📊 Analytics Integration
- **User Analytics**: Firebase Analytics for user behavior
- **Performance Monitoring**: Crashlytics for crash reporting
- **A/B Testing**: Feature flag management
- **Custom Events**: Track user interactions and conversions

### 🐛 Error Tracking
- **Crash Reporting**: Automatic crash collection and analysis
- **Error Logging**: Structured logging with context
- **Performance Issues**: ANR detection, memory leak tracking
- **User Feedback**: In-app feedback collection

---

*This comprehensive documentation serves as the complete reference for the MentorMe project structure. Each component is designed to work together in a cohesive, scalable architecture that supports the app's mentoring platform functionality.*

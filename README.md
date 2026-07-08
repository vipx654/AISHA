# AISHA

AISHA is an Android mobile application built with modern Android development practices.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Hilt
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Architecture Overview

The project follows Clean Architecture principles with three main layers:

### Presentation Layer
- Jetpack Compose UI
- ViewModels with StateFlow
- Navigation Compose

### Domain Layer
- Use Cases
- Repository Interfaces
- Domain Models

### Data Layer
- Repository Implementations
- Remote Data Sources (Firebase)
- Hilt Dependency Injection Modules

## Features

- User Authentication (Sign In, Sign Up, Password Reset)
- User Profile Management
- Real-time Profile Updates via Firestore
- Profile Photo Upload via Firebase Storage

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34
- Firebase Project with:
  - Authentication enabled
  - Cloud Firestore enabled
  - Storage enabled

### Setup

1. Clone the repository
2. Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
3. Add an Android app with package name `com.aisha`
4. Download `google-services.json` and place it in `app/` directory
5. Build the project: `./gradlew assembleDebug`

## Project Structure

```
app/src/main/java/com/aisha/
├── AishaApplication.kt          # Application class with Hilt
├── di/                          # Dependency Injection modules
├── domain/                      # Domain layer
│   ├── model/                   # Domain models
│   ├── repository/              # Repository interfaces
│   └── usecase/                # Use cases
├── data/                        # Data layer
│   ├── remote/                 # Firebase data sources
│   └── repository/            # Repository implementations
└── presentation/               # Presentation layer
    ├── auth/                  # Authentication screens
    ├── home/                  # Home screen
    ├── profile/               # Profile screens
    ├── navigation/            # Navigation graph
    ├── components/            # Reusable UI components
    └── theme/                 # Compose theme
```

## License

This project is licensed under the MIT License.

# Project Overview

This is a full-stack application for diet and training, similar to MyFitnessPal. It consists of a multi-module Android application and a Node.js backend.

## Frontend (Android)

The frontend is a multi-module Android application built with Kotlin and Jetpack Compose.

### Modules

*   `:app`: The main application module, which brings together all the other modules.
*   `:auth`: Handles user authentication (login and registration).
*   `:core`: Contains shared components, utilities, and theme information.
*   `:dieta`: Manages the diet-related features.
*   `:treino`: Manages the workout-related features.

### Architecture and Tech Stack

*   **UI:** Jetpack Compose
*   **Dependency Injection:** Hilt
*   **Database:** Room
*   **Networking:** Retrofit
*   **Asynchronous Operations:** Kotlin Coroutines
*   **Navigation:** Jetpack Navigation Compose
*   **Image Loading:** Coil

## Backend (Node.js)

The backend is a Node.js server using the Express.js framework.

### Features

*   User authentication (registration and login) with JWT.
*   API endpoints for managing users, workouts, and meals.
*   Integration with TensorFlow.js for machine learning features.
*   Fuzzy search with Fuse.js.
*   Google Translate integration.

### Tech Stack

*   **Framework:** Express.js
*   **Database:** MySQL
*   **Authentication:** JSON Web Tokens (JWT)
*   **API:** RESTful

# Building and Running

## Frontend (Android)

To build the project, run the following command in the root directory:

```bash
./gradlew build
```

To install and run the app on a connected device or emulator, use the following command:

```bash
./gradlew installDebug
```

Alternatively, you can open the project in Android Studio and run it from there.

## Backend (Node.js)

To run the backend server, tell user to activate it 

# Development Conventions

## Frontend (Android)

*   **Language:** The project is written entirely in Kotlin.
*   **UI:** The UI is built using Jetpack Compose.
*   **Dependency Injection:** Hilt is used for dependency injection. All dependencies should be provided through Hilt modules.
*   **Code Style:** The project follows the standard Kotlin coding conventions.
*   **Testing:** Unit tests should be placed in the `test` source set of each module. Instrumentation tests should be placed in the `androidTest` source set.

## Backend (Node.js)

*   **Language:** JavaScript (CommonJS)
*   **Code Style:** Follows standard JavaScript and Node.js conventions.
*   **Dependencies:** Managed with npm.
*   **API:** The API is versioned and follows RESTful principles.

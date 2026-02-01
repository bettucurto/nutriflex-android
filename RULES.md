# Development Rules and Best Practices

This document outlines the rules, conventions, and best practices to follow when contributing to the Nutriflex project. Adhering to these guidelines will help maintain code quality, consistency, and readability across the entire codebase.

## General Rules

### Git and Version Control

*   **Commits:** Each commit should be atomic and represent a single logical change.
*   **Commit Messages:** Commit messages should be clear and concise. They should follow the conventional commit format (`feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:`).
*   **Branches:** Create a new branch for each new feature or bug fix. Branch names should be descriptive (e.g., `feat/add-new-feature`, `fix/login-bug`).
*   **Pull Requests:** Before merging, all pull requests should be reviewed by at least one other developer.

### Code Style

*   **Language:** Use US English for all code, comments, and documentation.
*   **Formatting:** Code should be formatted according to the standard style guides for each language (Kotlin Style Guide, Prettier for JavaScript).
*   **Comments:** Write comments only when necessary to explain complex logic. Well-structured code should be self-documenting.

---

## Frontend (Android)

### Architecture

*   **MVVM:** The project follows the Model-View-ViewModel (MVVM) architecture pattern.
*   **UI:** The UI is built with Jetpack Compose.
*   **State Management:** Use `StateFlow` and `SharedFlow` in ViewModels to expose state to the UI.

### Jetpack Compose

*   **Composable Functions:**
    *   Composable functions should be small and reusable.
    *   They should be stateless whenever possible, with state hoisted to the ViewModel.
    *   Use the `Modifier` parameter to allow for flexible customization.
*   **Naming:** Composable functions should be named using PascalCase (e.g., `MyComposableScreen`).
*   **Previews:** All screens and complex components should have a `@Preview` composable for easy testing and development.

### Dependency Injection

*   **Hilt:** Use Hilt for dependency injection.
*   **ViewModels:** Inject dependencies into ViewModels using the `@HiltViewModel` annotation.
*   **Modules:** Provide dependencies that cannot be constructor-injected (e.g., interfaces, classes from external libraries) in Hilt modules.

### Navigation

*   **Navigation Component:** Use the Jetpack Navigation component for all navigation.
*   **Routes:** Define navigation routes as constants in a central location.
*   **Arguments:** Pass arguments between screens using the Navigation component's argument passing mechanism.

---

## Backend (Node.js)

### Architecture

*   **Layered Architecture:** The backend is structured in layers: `routes`, `controllers`, `services`, and `data access`.
*   **API:** The API is a RESTful API.

### API Design

*   **Versioning:** The API is not currently versioned, but this may be added in the future.
*   **Endpoints:** API endpoints should be named using nouns (e.g., `/users`, `/workouts`).
*   **HTTP Methods:** Use the appropriate HTTP methods for each action (e.g., `GET` for retrieving data, `POST` for creating data, `PUT` for updating data, `DELETE` for deleting data).
*   **Status Codes:** Use appropriate HTTP status codes to indicate the outcome of a request.

### Error Handling

*   **Middleware:** Use a centralized error-handling middleware to catch and handle errors.
*   **Error Responses:** Error responses should have a consistent format.

### Asynchronous Operations

*   **Async/Await:** Use `async/await` for all asynchronous operations.
*   **Promises:** Avoid using raw promises or callbacks.

### Security

*   **Authentication:** User authentication is handled with JSON Web Tokens (JWT).
*   **Password Hashing:** Passwords are hashed with `bcrypt`. Never store plain-text passwords.
*   **Input Validation:** Validate and sanitize all user input to prevent security vulnerabilities like SQL injection and XSS.
*   **Environment Variables:** Store sensitive information (e.g., database credentials, JWT secrets) in a `.env` file and do not commit it to version control.

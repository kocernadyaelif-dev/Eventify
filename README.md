# Eventify — Social Event Manager

Eventify is an Android prototype developed for the Mobile Software Engineering case study at IU International University of Applied Sciences. It demonstrates an MVVM-style data flow, a Room-backed local cache and reactive event-list updates.

## Implemented functionality

- Kotlin models for events, participants, event memberships, roles and RSVP status
- Jetpack Compose event-list screen with loading, content, error and Refresh states
- Room database with `events`, `participants` and `event_memberships` tables
- Repository and ViewModel connected through Kotlin Flow and StateFlow
- Local initialization of two demonstration events when the event table is empty
- Local database observation and persistence
- JVM ViewModel tests and Android instrumented Room DAO tests

**Scope:** This is a local prototype, not a fully synchronized social-event service. The current Refresh action initializes the demo events locally when needed; it does not call a remote API. Sign-in, interactive RSVP submission, media uploads, persistent upload queues, remote conflict resolution and multi-device synchronization are not implemented.

## Project structure

- `app/src/main/java/eventify/` — models, DAO, Room database, repository, ViewModel and Compose screen
- `app/src/main/java/com/example/eventify/` — Android activity
- `app/src/test/java/eventify/EventsViewModelTest.kt` — ViewModel unit tests
- `app/src/androidTest/java/eventify/EventDaoTest.kt` — Room instrumentation tests
- `app/build.gradle.kts` — application build configuration

## Run locally

1. Install a compatible version of Android Studio and the Android SDK.
2. Clone this repository or download and extract its source ZIP.
3. Open the repository root as a project in Android Studio.
4. Allow Gradle synchronization to finish and install any SDK components requested by Android Studio. The project specifies compile SDK 37 and minimum SDK 26.
5. Select an Android emulator or a connected device, then run the `app` configuration.

On Windows, Gradle tasks may also be run from the project root using `gradlew.bat`.

## Tests

- JVM ViewModel tests: `./gradlew testDebugUnitTest` (Windows: `gradlew.bat testDebugUnitTest`)
- Room instrumented tests (requires an emulator or device): `./gradlew connectedDebugAndroidTest` (Windows: `gradlew.bat connectedDebugAndroidTest`)

The accompanying case study records five successful ViewModel tests and four successful Room DAO tests in the tested development environment on 25 November 2026. These results do not establish remote synchronization or multi-device behavior.

## Academic context

The project discusses an eventual API, offline RSVP outbox, synchronization cursor, idempotency and conflict-handling strategy. Those distributed features are design proposals and are not part of the currently running application.

## License

No open-source license has been selected. Public repository visibility does not itself grant permission to reuse or redistribute the source code.

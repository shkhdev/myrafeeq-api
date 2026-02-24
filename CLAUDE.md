# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build                  # Build with tests
./gradlew build -x test          # Build without tests
./gradlew test                   # Run all tests
./gradlew test --tests "uz.myrafeeq.api.SomeTest"  # Single test class
./gradlew spotlessCheck          # Check formatting
./gradlew spotlessApply          # Fix formatting (always run before committing)
./gradlew bootRun                # Run locally (requires PostgreSQL)
```

Tests use **Testcontainers** (PostgreSQL), so Docker must be running. No external services needed for tests.

## Architecture

**Islamic companion Telegram Mini App backend:** authentication, prayer times, prayer tracking, user preferences.

**Package:** `uz.myrafeeq.api`

### Core Flow

```
Telegram Mini App -> AuthController (POST /api/auth/validate)
                       -> TelegramAuthService (HMAC verification, user upsert, JWT generation)

Authenticated requests -> JwtAuthFilter (Bearer token validation)
                            -> UserController (preferences, onboarding)
                            -> PrayerTimesController (prayer time calculations)
                            -> PrayerTrackingController (daily tracking, stats)

Public requests -> CityController (city search, nearest city)
                -> PrayerTimesController /by-location (anonymous prayer times)
```

### Endpoints

| Method | Path                            | Action                    | Auth     | Status |
|--------|---------------------------------|---------------------------|----------|--------|
| POST   | `/api/auth/validate`            | Authenticate via Telegram | Public   | 201    |
| GET    | `/api/user/preferences`         | Get user preferences      | Required | 200    |
| PUT    | `/api/user/preferences`         | Update user preferences   | Required | 200    |
| POST   | `/api/user/onboarding`          | Complete onboarding       | Required | 201    |
| GET    | `/api/prayer-times`             | Get prayer times          | Required | 200    |
| GET    | `/api/prayer-times/by-location` | Prayer times by location  | Public   | 200    |
| GET    | `/api/prayer-tracking`          | Get tracking data         | Required | 200    |
| POST   | `/api/prayer-tracking/toggle`   | Toggle prayer status      | Required | 200    |
| GET    | `/api/prayer-tracking/stats`    | Get prayer statistics     | Required | 200    |
| GET    | `/api/cities/search`            | Search cities             | Public   | 200    |
| GET    | `/api/cities/nearest`           | Find nearest city         | Public   | 200    |

### Key Design Decisions

- **Sealed exception hierarchy:** `MyRafeeqException` is sealed, subtypes exhaustively matched via switch in
  `GlobalExceptionHandler`. Adding a new exception type requires handling it in the switch.
- **Optimistic locking:** `@Version` on entities -- concurrent modifications throw
  `ObjectOptimisticLockingFailureException` (409 Conflict).
- **JWT authentication:** Telegram init data HMAC verified, then JWT issued. Bearer token required for authenticated
  endpoints.
- **Prayer time calculations:** Uses [Adhan](https://github.com/batoulapps/adhan-kotlin) library (v1.x Java) for prayer
  time computation. Supports 9 calculation methods (MWL, ISNA, EGYPT, KARACHI, UMM_AL_QURA, DUBAI, QATAR, KUWAIT, SINGAPORE).
- **Hijri calendar:** Uses JDK's `java.time.chrono.HijrahDate` (Umm Al-Qura calendar).
- **Partial updates:** `UpdatePreferencesRequest` uses nullable fields -- only non-null values are applied.
- **JSONB fields:** `prayer_notifications` and `manual_adjustments` stored as JSONB, serialized/deserialized via Jackson
  in mappers.

## Database

**Schema:** `myrafeeq` (PostgreSQL)

4 tables: `users`, `cities`, `user_preferences`, `prayer_tracking`

Migrations: `src/main/resources/db/changelog/changes/` (Liquibase SQL). Seed data in changeset 005.

## Code Conventions

- **Formatting:** Google Java Format via Spotless. Wildcard imports forbidden.
- **Lombok** for boilerplate (builders, getters, etc.)
- **MapStruct** 1.6.3 for DTO mapping. Lombok-MapStruct binding configured.
- **Sealed exceptions** with exhaustive switch matching in GlobalExceptionHandler.

## Git

- **Default branch:** `main`

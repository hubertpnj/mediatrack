# Auth Design — MediaTrack

**Date:** 2026-06-17  
**Scope:** User registration, login (email/password + Google OAuth2), JWT session via HttpOnly cookie, frontend routing + auth UI

---

## Architecture Principles Applied

- Package-by-feature on the backend (`auth/`, `media/`, `user/`, `config/`)
- No external auth SaaS — pure Spring Security 6 + JJWT 0.12.6 (already in build.gradle)
- JWT stored in `HttpOnly; SameSite=Lax` cookie — not localStorage
- TanStack Query v5 + React Router v7 added to frontend

---

## Backend

### Package structure

```
hpnj.mediatrack
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── JwtAuthFilter.java           ← OncePerRequestFilter, reads cookie
│   ├── CustomOAuth2UserService.java ← finds/creates UserAccount after Google login
│   ├── OAuth2SuccessHandler.java    ← issues JWT cookie, redirects to frontend
│   ├── RegisterRequest.java         ← record DTO with @Valid
│   ├── LoginRequest.java            ← record DTO with @Valid
│   └── UserPrincipal.java           ← implements UserDetails
├── user/
│   ├── UserAccount.java             ← existing entity
│   └── UserRepository.java          ← new JPA repository
├── media/                           ← existing, unchanged
└── config/
    └── SecurityConfig.java          ← updated
```

### Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | public | Creates user, sets JWT cookie, returns `UserInfo` |
| POST | `/api/auth/login` | public | Validates credentials, sets JWT cookie, returns `UserInfo` |
| POST | `/api/auth/logout` | public | Clears JWT cookie |
| GET | `/api/auth/me` | public | Returns `UserInfo` if authenticated, `401` otherwise |
| GET | `/oauth2/authorization/google` | public | Spring Security redirect to Google |
| GET | `/login/oauth2/code/google` | public | Spring Security callback (handled automatically) |

`UserInfo` response: `{ id, username, email }`

### JWT

- Algorithm: HS256
- TTL: 7 days
- Stored as: `Set-Cookie: auth_token=<jwt>; HttpOnly; SameSite=Lax; Path=/; Max-Age=604800`
- Read by: `JwtAuthFilter` (reads `auth_token` cookie, validates, sets `SecurityContext`)
- Secret: configured in `application.properties` as `app.jwt.secret` (256-bit random string)

### Security rules (SecurityConfig)

- **Permit all:** `GET /api/media/**`, `GET /api/auth/me`, `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `/oauth2/**`, `/login/oauth2/**`
- **Authenticated:** everything else under `/api/**`
- Session: `STATELESS`
- CSRF: disabled (cookie is HttpOnly + SameSite=Lax, not a CSRF vector for AJAX)

### Google OAuth2

- Spring Security handles the redirect and callback automatically
- `CustomOAuth2UserService` receives the Google profile, finds existing `user_account` by email or creates a new one (with `password_hash = null` for OAuth users)
- `OAuth2SuccessHandler` issues the JWT cookie and redirects to `http://localhost:5173/media/all/`

### Password handling

- BCrypt via Spring Security's `BCryptPasswordEncoder`
- OAuth2 users have `password_hash = null` — they cannot use email/password login

### Error handling

- `@RestControllerAdvice` — global handler returning `{ error: "message" }` with appropriate HTTP status
- Register: 409 if email/username already exists, 400 for validation errors
- Login: 401 for bad credentials

---

## Frontend

### New dependencies

- `react-router-dom` v7
- `@tanstack/react-query` v5

### Routes

| Path | Component | Auth required |
|------|-----------|---------------|
| `/media/all/` | `App` (existing) | No |
| `/login` | `LoginPage` | No (redirect to `/media/all/` if already logged in) |
| `/register` | `RegisterPage` | No (redirect to `/media/all/` if already logged in) |

### Auth state

`AuthContext` (React Context + `useAuth` hook):
- Calls `GET /api/auth/me` on mount (via TanStack Query)
- Provides `{ user, isLoading, login, logout, register }`
- `user` is `null` when not authenticated

### Header changes

- When **not logged in**: show "Log in" and "Sign up" buttons in top-right corner (link to `/login` and `/register`)
- When **logged in**: show username + "Log out" button

### Login page (`/login`)

- Email + password fields
- "Log in" button → `POST /api/auth/login`
- "Sign in with Google" button → links to `http://localhost:8080/oauth2/authorization/google`
- Link to `/register`

### Register page (`/register`)

- Username + email + password fields
- "Sign up" button → `POST /api/auth/register`
- "Sign up with Google" button → links to `http://localhost:8080/oauth2/authorization/google`
- Link to `/login`

### useMedia migration

`useMedia` hook refactored to use TanStack Query (`useQuery`) — existing behavior preserved, adds automatic caching and refetch.

---

## Database

No new migrations needed — `user_account` table (V9) already has all required columns (`id`, `username`, `email`, `password_hash`, `created_at`). Google users will have `password_hash = null`.

---

## Out of scope

- Email verification
- Password reset
- Apple Sign In (deferred)
- Refresh tokens (JWT TTL 7 days is sufficient for now)

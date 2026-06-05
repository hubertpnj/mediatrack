# MediaTrack - Specifications

## Overview
A platform for users to track their media consumption (movies, TV shows, books, games, music).

## Tech Stack
- Spring Boot 4.0.6
- Java 25
- Gradle 9.5.1
- REST API (no templates - frontend is separate)
- Spring Data JPA + PostgreSQL
- Spring Security with JWT

## Data Model

### Media hierarchy — `@Inheritance(JOINED)`, `@DiscriminatorColumn("dtype")`
- **Media** (abstract) — `id`, `title`, `release_date`, `dtype`
  - **Movie** — `duration_minutes`
  - **TVShow** — (no extra fields)
    - **Season** — `show_id`, `season_number`, `title?`, `release_date`
      - **Episode** — `season_id`, `episode_number`, `title`, `duration_minutes`, `release_date`
  - **Book** — `isbn`, `page_count`
  - **Album** — `album_type` (AlbumType enum)
    - **Track** — `album_id`, `track_number`, `title`, `duration_seconds`
  - **Game** — (tbd)

> `Season` and `Episode` are sub-entities of `TVShow`, not part of the Media hierarchy.
> A single is an `Album` with `album_type=SINGLE` containing one `Track`.

### Party hierarchy — `@Inheritance(JOINED)`, `@DiscriminatorColumn("dtype")`
- **Party** (abstract) — `id`, `dtype`
  - **Person** — `first_name`, `last_name`, `birth_date`
  - **Organization** — `name`, `founded_year`

### Relationships
- **PartyMembership** — `person_id`, `organization_id`, `role`, `start_date`, `end_date`
- **Contribution** — `party_id`, `media_id`, `role` (ContributionRole), `display_order`

### User & Library
- **UserAccount** — `username`, `email`, `password_hash`, `created_at`
- **LibraryEntry** — `user_id`, `media_id`, `status` (LibraryStatus), `completed` (boolean), `rating` (1–10), `review`, `added_at`, `updated_at`
- **UserList** — `user_id`, `name`, `description`, `is_public`
- **UserListItem** — `list_id`, `media_id`, `position`, `added_at`

### Grouping
- **MediaGroup** — `name`, `group_type` (MediaGroupType), `description`
- **MediaGroupItem** — surrogate PK, `group_id`, `media_id`, `position`; UNIQUE(`group_id`, `media_id`)

### Activity
- **ActivityEvent** — `user_id`, `event_type`, `media_id?`, `entry_id?`, `occurred_at` (DB table, no Kafka for now)

### Enums
| Enum | Values |
|------|--------|
| `LibraryStatus` | `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `DROPPED`, `ON_HOLD` |
| `MediaGroupType` | `GENRE`, `FRANCHISE`, `UNIVERSE`, `SERIES`, `THEME` |
| `AlbumType` | `LP`, `EP`, `SINGLE`, `MIXTAPE`, `COMPILATION`, `LIVE` |
| `ContributionRole` | `DIRECTOR`, `WRITER`, `ACTOR`, `AUTHOR`, `DEVELOPER`, `COMPOSER`, `PERFORMER`, `PRODUCER`, `ILLUSTRATOR`, `EDITOR` |

## Features (MVP)
1. User registration / login (JWT auth)
2. Add media items to library
3. Track status (planned / in progress / completed / dropped / on hold) + completed flag
4. Rate media (1–10)
5. Lists (watchlist, favorites, custom)
6. Search / filter media

## Implementation Order
1. Entities + database (domain model first)
2. Repositories + service layer + REST API
3. Authentication (Spring Security + JWT)

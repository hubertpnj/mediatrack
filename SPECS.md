# MediaTrack - Specifications

## Overview
A platform for users to track their media consumption (movies, TV shows, books, games, music).
Multiple users can compare collections, statuses, and ratings.

## Tech Stack
- Spring Boot 4.0.6
- Java 25
- Gradle 9.5.1
- REST API (no templates — frontend is separate)
- Spring Data JPA + PostgreSQL 17
- Spring Security with JWT (jjwt 0.12.6)
- Flyway 11 for schema migrations

> **Spring Boot 4 note:** Flyway autoconfiguration is in a separate module — add `org.springframework.boot:spring-boot-flyway` alongside `org.flywaydb:flyway-core` and `org.flywaydb:flyway-database-postgresql`.

> **Hibernate 7 note:** Camel-case entity class names are no longer auto-converted to snake_case table names. Add explicit `@Table(name = "...")` to all multi-word entities (e.g. TVShow, UserAccount, LibraryEntry, etc.).

## Infrastructure
- PostgreSQL 17 via Docker (`docker-compose.yml` at project root)
- DB: `mediatrack`, user: `postgres`, password: `postgres`, port: `5432`
- `spring.jpa.hibernate.ddl-auto=validate` — schema managed exclusively by Flyway

## Data Model

### Media hierarchy — `@Inheritance(JOINED)`, `@DiscriminatorColumn("dtype")`
- **Media** (abstract) — `id`, `title`, `release_date`, `dtype`
  - **Movie** — `duration_minutes`
  - **TVShow** — (no extra fields; table: `tv_show`)
    - **Season** — `show_id`, `season_number`, `title?`, `release_date`; UNIQUE(`show_id`, `season_number`)
      - **Episode** — `season_id`, `episode_number`, `title`, `duration_minutes`, `release_date`; UNIQUE(`season_id`, `episode_number`)
  - **Book** — `isbn`, `page_count`
  - **Album** — `album_type` (AlbumType enum)
    - **Track** — `album_id`, `track_number`, `title`, `duration_seconds`; UNIQUE(`album_id`, `track_number`)
  - **Game** — platforms via `@ElementCollection` → `game_platform` table

> `Season` and `Episode` are sub-entities of `TVShow`, not part of the Media hierarchy.
> A single is an `Album` with `album_type=SINGLE` containing one `Track`.

### Party hierarchy — `@Inheritance(JOINED)`, `@DiscriminatorColumn("dtype")`
- **Party** (abstract) — `id`, `dtype`
  - **Person** — `first_name`, `last_name`, `birth_date`
  - **Organization** — `name`, `founded_year`

### Relationships
- **PartyMembership** — `person_id`, `organization_id`, `role`, `start_date`, `end_date`; table: `party_membership`
- **Contribution** — `party_id`, `media_id`, `role` (ContributionRole), `display_order`

### User & Library
- **UserAccount** — `username`, `email`, `password_hash`, `created_at`; table: `user_account`
- **LibraryEntry** — `user_id`, `media_id`, `status` (LibraryStatus), `completed` (boolean), `added_at`, `updated_at`; UNIQUE(`user_id`, `media_id`); table: `library_entry`
- **Review** — separate entity from LibraryEntry; `user_id`, `media_id`, `rating` (NUMERIC 3,1 — 0.0–10.0 in 0.5 steps), `comment` (TEXT, optional), `created_at`, `updated_at`; UNIQUE(`user_id`, `media_id`)
- **UserList** — `user_id`, `name`, `description`, `is_public`; table: `user_list`
- **UserListItem** — `list_id`, `media_id`, `position`, `added_at`; table: `user_list_item`

### Grouping
- **MediaGroup** — `name`, `group_type` (MediaGroupType), `description`; table: `media_group`
- **MediaGroupItem** — surrogate PK, `group_id`, `media_id`, `position`; UNIQUE(`group_id`, `media_id`); table: `media_group_item`

### Activity
- **ActivityEvent** — `user_id`, `event_type`, `media_id?`, `entry_id?`, `occurred_at`; table: `activity_event` (no Kafka for now)

### Enums
| Enum | Values |
|------|--------|
| `LibraryStatus` | `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `DROPPED`, `ON_HOLD` |
| `MediaGroupType` | `GENRE`, `FRANCHISE`, `UNIVERSE`, `SERIES`, `THEME` |
| `AlbumType` | `LP`, `EP`, `SINGLE`, `MIXTAPE`, `COMPILATION`, `LIVE` |
| `ContributionRole` | `DIRECTOR`, `WRITER`, `ACTOR`, `AUTHOR`, `DEVELOPER`, `COMPOSER`, `PERFORMER`, `PRODUCER`, `ILLUSTRATOR`, `EDITOR` |
| `Platform` | (game platforms — stored as `@ElementCollection`) |

## Package Structure
```
hpnj.mediatrack
├── domain/
│   ├── media/      # Media, Movie, TVShow, Season, Episode, Book, Album, Track, Game, MediaGroup, MediaGroupItem, enums
│   ├── party/      # Party, Person, Organization, PartyMembership, Contribution, enums
│   └── user/       # UserAccount, LibraryEntry, Review, UserList, UserListItem, ActivityEvent, enums
```

## Database Migrations (Flyway — V1–V14)
| Version | Content |
|---------|---------|
| V1 | `media` table |
| V2 | `movie` |
| V3 | `tv_show`, `season`, `episode` |
| V4 | `book` |
| V5 | `game`, `game_platform` |
| V6 | `album`, `track` |
| V7 | `party`, `person`, `organization` |
| V8 | `party_membership`, `contribution` |
| V9 | `user_account` |
| V10 | `library_entry` |
| V11 | `review` (rating CHECK: 0–10, multiples of 0.5) |
| V12 | `user_list`, `user_list_item` |
| V13 | `media_group`, `media_group_item` |
| V14 | `activity_event` |

## Features (MVP)
1. User registration / login (JWT auth)
2. Add media items to library
3. Track status (`PLANNED` / `IN_PROGRESS` / `COMPLETED` / `DROPPED` / `ON_HOLD`) + `completed` flag
4. Rate and review media (0–10 scale, 0.5 increments, comment optional)
5. Compare collections and ratings between users
6. Lists (watchlist, favorites, custom)
7. Search / filter media

## Implementation Status
- [x] Domain model — all JPA entities implemented
- [x] Flyway migrations V1–V14
- [x] Docker Compose (PostgreSQL 17)
- [x] Application starts and schema validates
- [ ] Repositories (JpaRepository per entity)
- [ ] Service layer
- [ ] REST controllers / API
- [ ] Authentication (Spring Security + JWT)

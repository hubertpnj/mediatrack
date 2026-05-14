# Mediatrack — CLAUDE.md

Portfolio project: media tracking app (movies, games, books, music). Goal: learning + portfolio.

## Stack

I work on my MacBook Pro M3 Max 36GB RAM, macOS 26.
VS Code and latest IntelliJ IDEA (Free), Docker Desktop.

| Layer | Technology |
| --- | --- |  
| Language | Java 25 LTS |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security 6 + JWT |
| Persistence | Spring Data JPA · Hibernate 6 · PostgreSQL 17 |
| Migrations | Flyway |
| Mapping | Manual mappers |
| Cache | Redis 7.4.x |
| Messaging | Apache Kafka 3.8.x |
| Frontend | React 19 |
| Infra | Docker Compose |

## Domain model (JOINED inheritance)

``` log
Media (abstract)        → Movie, Game, Book, Album
Party (abstract)        → Person, Organization
PartyMembership         — Person ↔ Organization (role, date range)
Contribution            — Party ↔ Media (role, order)
UserAccount             → LibraryEntry (status/rating/review)
                        → UserList → UserListItem
                        → ActivityEvent (Kafka consumer)
MediaGroup              — group_type: GENRE/FRANCHISE/UNIVERSE/SERIES/THEME
  └── MediaGroupItem    — composite PK
```

`@Inheritance(JOINED)` + `@DiscriminatorColumn("dtype")` everywhere.

## Enums

- `LibraryStatus`: PLANNED / IN_PROGRESS / COMPLETED / DROPPED / ON_HOLD
- `MediaGroupType`: GENRE / FRANCHISE / UNIVERSE / SERIES / THEME
- `ContributionRole`: varies per media type

Always `@Enumerated(STRING)`.

## Redis key schema

| Key | Type | TTL |
| --- | --- | --- |  
| `media:popular` | ZSet | — |
| `media:cache:{uuid}` | String | 1h |
| `search:{hash}` | String | 15min |
| `user:session:{uuid}` | String | 24h |

## Conventions

- UUID PKs everywhere
- Auditing: `created_at` / `updated_at` on all entities
- DTOs strictly separated from entities; mapping written manually
- Global `@ControllerAdvice` exception handler
- Flyway for all schema changes — no manual DDL
- No business logic in controllers; services own transactions

## User roles & permissions

`UserAccount.role` — single enum column, hierarchical, mutually exclusive.

| Operacja | USER | MODERATOR | ADMIN |
| --- | :---: | :---: | :---: |
| Przeglądanie zatwierdzonej treści | ✅ | ✅ | ✅ |
| Zarządzanie własną biblioteką | ✅ | ✅ | ✅ |
| Składanie sugestii (każdy typ encji) | ✅ | ✅ | ✅ |
| Zatwierdzanie / odrzucanie sugestii | ❌ | ✅ | ✅ |
| Pełny CRUD na treści (Media, Party, Group) | ❌ | ✅ | ✅ |
| Zarządzanie kontami użytkowników | ❌ | ❌ | ✅ |
| Zmiana ról | ❌ | ❌ | ✅ |

### Suggestion workflow

Encja `Suggestion` przechowuje proponowane dane jako JSONB z cyklem życia `PENDING → APPROVED / REJECTED`. Po zatwierdzeniu serwis materializuje rekord w docelowej tabeli.

```text
entity_type : MOVIE | GAME | BOOK | ALBUM | PERSON | ORGANIZATION | MEDIA_GROUP
status      : PENDING | APPROVED | REJECTED
```

## Architecture decisions — always explain trade-offs

When proposing implementation choices, explain the trade-offs and ask for justification before proceeding. This is a learning project.

Do not explain code using comments, I want clean code and if I need to know something then I will ask you in the chat.

Speak briefly - SAVE TOKENS.
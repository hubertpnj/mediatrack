# Design: User Roles, Suggestion Workflow & CLI Tool

Date: 2026-05-14

## Scope

Two foundational features built together because the CLI enforces the same role boundaries as the REST API:

1. User roles & permissions model
2. Suggestion workflow for all entity types
3. Admin CLI tool (Spring Shell, `cli` profile)

---

## 1. User Roles & Permissions

### Model

`UserAccount` gets two new columns:

```text
role      : UserRole  (USER | MODERATOR | ADMIN, default: USER)
suspended : boolean   (default: false — set by ADMIN, blocks login)
```

Roles are mutually exclusive and hierarchical. No join table needed.

### Permission matrix

| Operacja | USER | MODERATOR | ADMIN |
| --- | :---: | :---: | :---: |
| Przeglądanie zatwierdzonej treści | ✅ | ✅ | ✅ |
| Zarządzanie własną biblioteką | ✅ | ✅ | ✅ |
| Składanie sugestii (każdy typ encji) | ✅ | ✅ | ✅ |
| Zatwierdzanie / odrzucanie sugestii | ❌ | ✅ | ✅ |
| Pełny CRUD na treści (Media, Party, Group) | ❌ | ✅ | ✅ |
| Zarządzanie kontami użytkowników | ❌ | ❌ | ✅ |
| Zmiana ról | ❌ | ❌ | ✅ |

### Security enforcement

Spring Security method-level `@PreAuthorize` on service layer. Roles stored as `ROLE_USER`, `ROLE_MODERATOR`, `ROLE_ADMIN` for Spring compatibility.

---

## 2. Suggestion Workflow

### Model

Separate `suggestions` table — suggestions are fundamentally different from approved records (own lifecycle, history, reviewer).

```text
Suggestion
  id            : UUID (PK)
  entity_type   : SuggestionEntityType  (MOVIE | GAME | BOOK | ALBUM |
                                         PERSON | ORGANIZATION | MEDIA_GROUP)
  proposed_data : JSONB
  status        : SuggestionStatus  (PENDING | APPROVED | REJECTED)
  submitted_by  : FK → UserAccount
  reviewed_by   : FK → UserAccount  (nullable)
  review_note   : TEXT  (nullable)
  created_at    / updated_at
```

New enums: `UserRole`, `SuggestionEntityType`, `SuggestionStatus` — all `@Enumerated(STRING)`.

### Lifecycle

```
USER submits → PENDING → MODERATOR or ADMIN reviews → APPROVED / REJECTED
```

On approval: service materializes the record into the target table (e.g., inserts a `Movie` row from `proposed_data` JSON).

---

## 3. CLI Tool

### Approach

Spring Shell integrated into the main Spring Boot module, activated via `--spring.profiles.active=cli`. Profile disables web server, Redis, and Kafka — only PostgreSQL is required.

```bash
java -jar backend/target/mediatrack.jar --spring.profiles.active=cli
```

### Authentication

CLI prompts for credentials at startup. Spring Security verifies via `UserDetailsService` + BCrypt directly against the database. No JWT, no Redis session. Access is scoped to the running process.

```
mediatrack-cli> login --email admin@example.com --password ...
Logged in as admin@example.com [ADMIN]
```

### Commands — Content management (MODERATOR + ADMIN)

```
media list [--type MOVIE|GAME|BOOK|ALBUM]
media show <id>
media add --type MOVIE --title "Inception" --year 2010
media delete <id>

person list
person add --name "Christopher Nolan"
person show <id>

group list [--type GENRE|FRANCHISE|SERIES]
group add --name "Sci-Fi" --type GENRE

suggestion list [--status PENDING|APPROVED|REJECTED]
suggestion show <id>
suggestion approve <id>
suggestion reject <id> [--note "powód"]
```

### Commands — User administration (ADMIN only)

```
user list
user show <id|email>
user create --email "jan@example.com" --password "..." --role USER
user role <id> --set MODERATOR
user suspend <id>
user delete <id>
```

### Profile configuration (`application-cli.yml`)

```yaml
spring:
  main:
    web-application-type: none
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
```

---

## Out of scope (this iteration)

- Import/seed from JSON/CSV
- Diagnostic/cache operations
- Frontend (React)
- REST API endpoints (separate feature)

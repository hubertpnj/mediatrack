# System Design

## Domain model

```
Media (abstract)        → Movie, Game, Book, Album
Party (abstract)        → Person, Organization
PartyMembership         — Person ↔ Organization (role, date range)
Contribution            — Party ↔ Media (role, order)
UserAccount             → LibraryEntry (status/rating/review)
                        → UserList → UserListItem
                        → ActivityEvent (Kafka consumer)
MediaGroup              — group_type: GENRE/FRANCHISE/UNIVERSE/SERIES/THEME
  └── MediaGroupItem    — composite PK
Suggestion              — proposed entity data (JSONB), lifecycle PENDING → APPROVED/REJECTED
```

`@Inheritance(JOINED)` + `@DiscriminatorColumn("dtype")` on Media and Party.

## Enums

| Enum | Values |
| --- | --- |
| `LibraryStatus` | PLANNED / IN_PROGRESS / COMPLETED / DROPPED / ON_HOLD |
| `MediaGroupType` | GENRE / FRANCHISE / UNIVERSE / SERIES / THEME |
| `UserRole` | USER / MODERATOR / ADMIN (hierarchical, ordinal-based comparison) |
| `SuggestionEntityType` | MOVIE / GAME / BOOK / ALBUM / PERSON / ORGANIZATION / MEDIA_GROUP |
| `SuggestionStatus` | PENDING / APPROVED / REJECTED |

## User roles & permissions

| Action | USER | MODERATOR | ADMIN |
| --- | :---: | :---: | :---: |
| View approved content | ✅ | ✅ | ✅ |
| Manage own library | ✅ | ✅ | ✅ |
| Submit suggestions | ✅ | ✅ | ✅ |
| Approve / reject suggestions | ❌ | ✅ | ✅ |
| Full CRUD on Media, Party, Group | ❌ | ✅ | ✅ |
| Manage user accounts | ❌ | ❌ | ✅ |
| Change roles | ❌ | ❌ | ✅ |

## Suggestion workflow

`Suggestion` stores proposed entity data as JSONB. On approval, `SuggestionService.materialize()` creates the target entity (Movie, Person, MediaGroup, etc.) and saves it.

```
submit()  →  PENDING
approve() →  APPROVED  +  materialize target entity
reject()  →  REJECTED  +  review note
```

## Redis key schema

| Key | Type | TTL |
| --- | --- | --- |
| `media:popular` | ZSet | — |
| `media:cache:{uuid}` | String | 1h |
| `search:{hash}` | String | 15min |
| `user:session:{uuid}` | String | 24h |

## CLI tool

Spring Shell 3.3.3. Activated via `--spring.profiles.active=cli`. Requires only PostgreSQL (Redis, Kafka, OAuth2 resource server disabled via `application-cli.yml`).

`CliSecurityContext` — in-memory singleton holding logged-in user. `hasRole()` uses `UserRole.ordinal()` for hierarchical checks.

### CLI command groups

| Group | Commands |
| --- | --- |
| auth | login, logout, whoami |
| media | list, show, add, delete |
| person | list, show, add |
| group | list, add |
| suggestion | list, show, approve, reject |
| user | list, show, create, role, suspend, delete |

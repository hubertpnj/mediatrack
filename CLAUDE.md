# Mediatrack — CLAUDE.md

Portfolio project: media tracking app (movies, games, books, music). Goal: learning + portfolio.

## Dev environment

MacBook Pro M3 Max, macOS 26. VS Code + IntelliJ IDEA (Free), Docker Desktop.

## Stack

Java 25 · Spring Boot 3.5.x · Spring Security 6 + JWT · Spring Data JPA / Hibernate 6 · PostgreSQL 17 · Flyway · Redis 7.4.x · Kafka 3.8.x · React 19 · Docker Compose · Spring Shell 3.3.3 (CLI tool, `cli` profile)

## Conventions

- UUID PKs, `created_at`/`updated_at` on all entities (Spring Data auditing)
- DTOs separate from entities, mapped manually
- Flyway for all schema changes — no manual DDL
- Services own transactions, no business logic in controllers
- Global `@ControllerAdvice` for exceptions
- `@Enumerated(STRING)` always
- `@Inheritance(JOINED)` + `@DiscriminatorColumn("dtype")` for Media and Party hierarchies

## Working preferences

- No conventional commit prefixes — no `feat:`, `fix:`, `refactor:`, etc. Plain descriptive messages only.
- No code comments — clean code; ask in chat if explanation needed
- Always explain trade-offs before implementing — this is a learning project
- Brief responses — save tokens

## Design docs

See [`docs/design/`](docs/design/) for domain model, roles, permissions, Redis schema, and architecture decisions.

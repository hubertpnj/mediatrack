# MediaTrack - Specifications

## Overview
A platform for users to track their media consumption (movies, TV shows, books, games, music, etc.)

## Tech Stack
- Spring Boot 4.0.6
- Java 25
- Gradle 9.5.1
- REST API (no templates - frontend is separate)
- Spring Data JDBC or JPA + PostgreSQL
- Spring Security with JWT

## Core Domains
- **Media** - abstract base type for Movies, TV Shows, Books, Games, Music
- **Entry** - per-user tracking record (status, progress, ratings, dates)
- **User** - authentication & profiles

## Suggested Features (MVP)
1. User registration/login (JWT auth)
2. Add media items to library
3. Track progress (not started, in progress, completed, etc.)
4. Rate media (1-5 stars)
5. Lists (watchlist/favorites)
6. Search/filter media

## Suggested Implementation Order
1. Database + entities first - define the domain model
2. API + service layer
3. Auth

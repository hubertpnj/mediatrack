# MediaTrack

Platforma do śledzenia konsumpcji mediów — filmy, seriale, książki, gry, muzyka.

## Wymagania

| Narzędzie | Wersja |
|-----------|--------|
| Java | 25 |
| Gradle | przez wrapper (`./gradlew`) |
| Node.js | 18+ |
| Docker + Docker Compose | dowolna aktualna |

---

## 1. Baza danych (Docker)

```bash
docker compose up -d
```

Uruchamia PostgreSQL 17 na porcie `5432`. Dane są przechowywane w wolumenie `mediatrack_data`.

Aby zatrzymać (bez usuwania danych):

```bash
docker compose down
```

Aby zatrzymać i usunąć dane:

```bash
docker compose down -v
```

---

## 2. Zmienne środowiskowe

Utwórz plik `.env` w katalogu głównym projektu:

```dotenv
# TMDB API — klucz do synchronizacji filmów i seriali
# Pobierz na: https://www.themoviedb.org/settings/api (pole "API Read Access Token")
TMDB_API_KEY=eyJhbGc...

# Google OAuth2 — logowanie przez Google
# Utwórz w: https://console.cloud.google.com → APIs & Services → Credentials
GOOGLE_CLIENT_ID=123456789-abc.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-...
```

> **TMDB:** Aplikacja działa bez klucza TMDB — wystarczy ustawić `tmdb.sync.enabled=false` (patrz sekcja [Konfiguracja synchronizacji TMDB](#konfiguracja-synchronizacji-tmdb)). Bez klucza baza nie zostanie zasilona danymi z TMDB, ale seedy z migracji (`V15`) są zawsze dostępne.
>
> **Google OAuth2:** Logowanie przez Google jest opcjonalne. Bez tych zmiennych logowanie e-mail + hasło działa normalnie. Jeśli nie chcesz konfigurować Google, uruchom backend z flagą `--spring.security.oauth2.client.registration.google.client-id=dummy --spring.security.oauth2.client.registration.google.client-secret=dummy`.

---

## 3. Backend

```bash
./gradlew bootRun
```

Spring Boot automatycznie wczyta plik `.env` (przez `spring-dotenv`). Aplikacja startuje na porcie `8080`.

Przy pierwszym uruchomieniu Flyway zastosuje wszystkie migracje (`V1`–`V18`), a następnie — jeśli `TMDB_API_KEY` jest ustawiony — uruchomi import popularnych filmów i seriali z TMDB (domyślnie 5 stron ≈ 100 pozycji każdego typu).

---

## 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite startuje na `http://localhost:5173` i proxy'uje zapytania `/api`, `/oauth2` i `/login/oauth2` do backendu (`localhost:8080`).

---

## 5. Konfiguracja synchronizacji TMDB

Opcjonalne właściwości w `application.properties` lub jako argumenty uruchomieniowe:

| Właściwość | Domyślnie | Opis |
|-----------|-----------|------|
| `tmdb.sync.enabled` | `true` | Włącz/wyłącz całą synchronizację |
| `tmdb.sync.popular-pages` | `5` | Liczba stron importu przy pierwszym uruchomieniu |
| `tmdb.sync.cron` | `0 0 3 * * *` | Harmonogram codziennej synchronizacji (3:00 rano) |

Przykład — wyłącz TMDB (np. do lokalnego developmentu bez klucza):

```bash
./gradlew bootRun --args='--tmdb.sync.enabled=false'
```

---

## 6. Google OAuth2 — konfiguracja

1. Przejdź do [Google Cloud Console](https://console.cloud.google.com) → **APIs & Services** → **Credentials**
2. Utwórz **OAuth 2.0 Client ID** (typ: Web application)
3. Dodaj `http://localhost:5173` do **Authorized JavaScript origins**
4. Dodaj `http://localhost:5173/login/oauth2/code/google` do **Authorized redirect URIs**
5. Skopiuj **Client ID** i **Client Secret** do pliku `.env`

---

## Szybki start (TL;DR)

```bash
# 1. Baza danych
docker compose up -d

# 2. Zmienne środowiskowe
cp .env.example .env   # uzupełnij klucze

# 3. Backend
./gradlew bootRun

# 4. Frontend (nowy terminal)
cd frontend && npm install && npm run dev

# 5. Otwórz http://localhost:5173
```

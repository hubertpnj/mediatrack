# Frontend Media List Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Vite + vanilla TypeScript frontend that fetches all media from `GET /api/media` and displays them in a filterable list by type.

**Architecture:** The frontend lives in `frontend/` alongside the existing Spring Boot project root. It's a pure TypeScript app — no framework — that fetches from the backend and renders to the DOM. The backend gets CORS configured to allow requests from Vite's dev server (`localhost:5173`).

**Tech Stack:** Vite 6, TypeScript 5, Vitest (unit tests for pure functions), vanilla CSS, Node 26 / npm 11.

---

## File Map

| File | Responsibility |
|---|---|
| `frontend/index.html` | HTML shell — mounts `#app` |
| `frontend/src/types.ts` | `MediaSummary` interface |
| `frontend/src/api.ts` | `fetchMedia(): Promise<MediaSummary[]>` |
| `frontend/src/filter.ts` | Pure filter logic + type-pill UI |
| `frontend/src/mediaList.ts` | Renders media cards to DOM |
| `frontend/src/main.ts` | Entry: wires api → filter → renderer |
| `frontend/src/style.css` | App styles |
| `frontend/vite.config.ts` | Proxy `/api` → `localhost:8080` |
| `frontend/package.json` | Deps + scripts |
| `frontend/tsconfig.json` | TS config |
| `src/main/java/hpnj/mediatrack/config/SecurityConfig.java` | Add CORS (already exists) |

---

## Task 1: Backend — enable CORS for Vite dev server

**Files:**
- Modify: `src/main/java/hpnj/mediatrack/config/SecurityConfig.java`

- [ ] **Step 1: Add CORS config to SecurityConfig**

Replace the entire file content:

```java
package hpnj.mediatrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

- [ ] **Step 2: Restart backend and verify CORS header**

Kill port 8080, restart with `./gradlew bootRun &`, then:

```bash
curl -s -I -X OPTIONS http://localhost:8080/api/media \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET"
```

Expected: response includes `Access-Control-Allow-Origin: http://localhost:5173`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/hpnj/mediatrack/config/SecurityConfig.java
git commit -m "add CORS for Vite dev server"
```

---

## Task 2: Scaffold Vite + TypeScript project

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/tsconfig.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/style.css`

- [ ] **Step 1: Create `frontend/package.json`**

```json
{
  "name": "mediatrack-frontend",
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "test": "vitest run"
  },
  "devDependencies": {
    "typescript": "^5.8.3",
    "vite": "^6.3.5",
    "vitest": "^3.2.3"
  }
}
```

- [ ] **Step 2: Create `frontend/tsconfig.json`**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "noEmit": true
  },
  "include": ["src"]
}
```

- [ ] **Step 3: Create `frontend/vite.config.ts`**

The proxy rewrites `/api/*` to the Spring Boot server so the frontend never deals with CORS in dev — and in the same config, we point Vitest at the `src` directory.

```typescript
import { defineConfig } from 'vite'

export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  test: {
    environment: 'jsdom'
  }
})
```

- [ ] **Step 4: Create `frontend/index.html`**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MediaTrack</title>
  <link rel="stylesheet" href="/src/style.css" />
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
```

- [ ] **Step 5: Create placeholder `frontend/src/main.ts`**

```typescript
document.querySelector<HTMLDivElement>('#app')!.innerHTML = '<p>Loading...</p>'
```

- [ ] **Step 6: Create empty `frontend/src/style.css`**

```css
/* styles added in Task 5 */
```

- [ ] **Step 7: Install dependencies**

```bash
cd frontend && npm install
```

Expected: `node_modules/` created, no errors.

- [ ] **Step 8: Verify dev server starts**

```bash
cd frontend && npm run dev
```

Expected: Vite prints `Local: http://localhost:5173/`. Open it in browser — see "Loading...". Stop server with Ctrl+C.

- [ ] **Step 9: Commit**

```bash
git add frontend/
git commit -m "scaffold Vite + TypeScript frontend"
```

---

## Task 3: TypeScript types

**Files:**
- Create: `frontend/src/types.ts`

- [ ] **Step 1: Create `frontend/src/types.ts`**

```typescript
export interface MediaSummary {
  id: number
  title: string
  type: 'Movie' | 'TVShow' | 'Book' | 'Album' | 'Game'
  releaseDate: string | null
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/types.ts
git commit -m "add MediaSummary type"
```

---

## Task 4: API client with tests

**Files:**
- Create: `frontend/src/api.ts`
- Create: `frontend/src/api.test.ts`

- [ ] **Step 1: Write failing tests in `frontend/src/api.test.ts`**

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchMedia } from './api'

describe('fetchMedia', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('returns parsed media list on success', async () => {
    const data = [
      { id: 1, title: 'Inception', type: 'Movie', releaseDate: '2010-07-16' },
      { id: 2, title: 'Dune', type: 'Book', releaseDate: '1965-08-01' }
    ]
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(data), { status: 200 })
    )

    const result = await fetchMedia()

    expect(result).toEqual(data)
  })

  it('throws on non-ok response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response('', { status: 500 })
    )

    await expect(fetchMedia()).rejects.toThrow('Failed to fetch media: 500')
  })
})
```

- [ ] **Step 2: Run tests to confirm RED**

```bash
cd frontend && npm test
```

Expected: compilation error — `fetchMedia` not found.

- [ ] **Step 3: Create `frontend/src/api.ts`**

```typescript
import type { MediaSummary } from './types'

export async function fetchMedia(): Promise<MediaSummary[]> {
  const response = await fetch('/api/media')
  if (!response.ok) {
    throw new Error(`Failed to fetch media: ${response.status}`)
  }
  return response.json()
}
```

- [ ] **Step 4: Run tests to confirm GREEN**

```bash
cd frontend && npm test
```

Expected: 2 tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api.ts frontend/src/api.test.ts
git commit -m "add media API client"
```

---

## Task 5: Filter logic with tests

**Files:**
- Create: `frontend/src/filter.ts`
- Create: `frontend/src/filter.test.ts`

- [ ] **Step 1: Write failing tests in `frontend/src/filter.test.ts`**

```typescript
import { describe, it, expect } from 'vitest'
import { filterByType, ALL_TYPES } from './filter'
import type { MediaSummary } from './types'

const items: MediaSummary[] = [
  { id: 1, title: 'Inception',    type: 'Movie',  releaseDate: '2010-07-16' },
  { id: 2, title: 'Breaking Bad', type: 'TVShow', releaseDate: '2008-01-20' },
  { id: 3, title: 'Dune',         type: 'Book',   releaseDate: '1965-08-01' },
]

describe('filterByType', () => {
  it('returns all items when type is null', () => {
    expect(filterByType(items, null)).toEqual(items)
  })

  it('returns only items matching the given type', () => {
    expect(filterByType(items, 'Movie')).toEqual([items[0]])
  })

  it('returns empty array when no items match', () => {
    expect(filterByType(items, 'Game')).toEqual([])
  })
})

describe('ALL_TYPES', () => {
  it('contains all five media types', () => {
    expect(ALL_TYPES).toEqual(['Movie', 'TVShow', 'Book', 'Album', 'Game'])
  })
})
```

- [ ] **Step 2: Run tests to confirm RED**

```bash
cd frontend && npm test
```

Expected: compilation error — `filterByType` and `ALL_TYPES` not found.

- [ ] **Step 3: Create `frontend/src/filter.ts`**

```typescript
import type { MediaSummary } from './types'

export const ALL_TYPES = ['Movie', 'TVShow', 'Book', 'Album', 'Game'] as const

export function filterByType(
  items: MediaSummary[],
  type: MediaSummary['type'] | null
): MediaSummary[] {
  if (type === null) return items
  return items.filter(item => item.type === type)
}
```

- [ ] **Step 4: Run tests to confirm GREEN**

```bash
cd frontend && npm test
```

Expected: all 4 tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/filter.ts frontend/src/filter.test.ts
git commit -m "add filter logic"
```

---

## Task 6: Media list renderer

**Files:**
- Create: `frontend/src/mediaList.ts`

No unit tests for DOM rendering — the end-to-end check in Task 7 covers it.

- [ ] **Step 1: Create `frontend/src/mediaList.ts`**

```typescript
import type { MediaSummary } from './types'

const TYPE_LABELS: Record<MediaSummary['type'], string> = {
  Movie:  'Film',
  TVShow: 'Serial',
  Book:   'Książka',
  Album:  'Album',
  Game:   'Gra',
}

function formatDate(date: string | null): string {
  if (!date) return '—'
  return new Date(date).getFullYear().toString()
}

function createCard(item: MediaSummary): HTMLElement {
  const card = document.createElement('div')
  card.className = 'media-card'
  card.innerHTML = `
    <span class="media-type media-type--${item.type.toLowerCase()}">${TYPE_LABELS[item.type]}</span>
    <h3 class="media-title">${item.title}</h3>
    <p class="media-year">${formatDate(item.releaseDate)}</p>
  `
  return card
}

export function renderMediaList(container: HTMLElement, items: MediaSummary[]): void {
  container.innerHTML = ''
  if (items.length === 0) {
    container.innerHTML = '<p class="empty">Brak wyników.</p>'
    return
  }
  items.forEach(item => container.appendChild(createCard(item)))
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/mediaList.ts
git commit -m "add media list renderer"
```

---

## Task 7: Wire everything in main.ts + CSS

**Files:**
- Modify: `frontend/src/main.ts`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Replace `frontend/src/main.ts`**

```typescript
import './style.css'
import { fetchMedia } from './api'
import { filterByType, ALL_TYPES } from './filter'
import { renderMediaList } from './mediaList'
import type { MediaSummary } from './types'

const app = document.querySelector<HTMLDivElement>('#app')!

app.innerHTML = `
  <header class="header">
    <h1>MediaTrack</h1>
  </header>
  <main class="main">
    <nav class="filter-bar">
      <button class="filter-btn active" data-type="">Wszystkie</button>
      ${ALL_TYPES.map(t => `<button class="filter-btn" data-type="${t}">${t}</button>`).join('')}
    </nav>
    <section id="media-list" class="media-grid"></section>
  </main>
`

const listEl = document.querySelector<HTMLElement>('#media-list')!
let allMedia: MediaSummary[] = []
let activeType: MediaSummary['type'] | null = null

function refresh(): void {
  renderMediaList(listEl, filterByType(allMedia, activeType))
}

document.querySelectorAll<HTMLButtonElement>('.filter-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'))
    btn.classList.add('active')
    const raw = btn.dataset.type
    activeType = raw ? (raw as MediaSummary['type']) : null
    refresh()
  })
})

listEl.innerHTML = '<p class="loading">Ładowanie...</p>'

fetchMedia()
  .then(data => {
    allMedia = data
    refresh()
  })
  .catch(() => {
    listEl.innerHTML = '<p class="error">Błąd ładowania danych.</p>'
  })
```

- [ ] **Step 2: Replace `frontend/src/style.css`**

```css
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: system-ui, sans-serif;
  background: #0f0f0f;
  color: #e8e8e8;
  min-height: 100vh;
}

.header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #2a2a2a;
}

.header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  color: #fff;
}

.main { padding: 1.5rem 2rem; }

.filter-bar {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.filter-btn {
  padding: 0.4rem 1rem;
  border: 1px solid #3a3a3a;
  border-radius: 999px;
  background: transparent;
  color: #aaa;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.15s;
}

.filter-btn:hover  { border-color: #666; color: #eee; }
.filter-btn.active { background: #fff; color: #000; border-color: #fff; }

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.media-card {
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: border-color 0.15s;
}

.media-card:hover { border-color: #555; }

.media-type {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  align-self: flex-start;
}

.media-type--movie  { background: #1e3a5f; color: #7ab3f0; }
.media-type--tvshow { background: #1e4a2e; color: #6fc98a; }
.media-type--book   { background: #4a2e1e; color: #e8a87c; }
.media-type--album  { background: #3a1e4a; color: #c47ae8; }
.media-type--game   { background: #4a3a1e; color: #e8c87a; }

.media-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #f0f0f0;
  line-height: 1.3;
}

.media-year { font-size: 0.8rem; color: #666; }

.loading, .empty, .error { color: #666; font-size: 0.9rem; padding: 2rem 0; }
.error { color: #e87a7a; }
```

- [ ] **Step 3: Run tests to confirm all still pass**

```bash
cd frontend && npm test
```

Expected: 6 tests pass.

- [ ] **Step 4: Verify in browser**

Make sure backend is running on 8080, then:

```bash
cd frontend && npm run dev
```

Open `http://localhost:5173`. Expected:
- Dark page with "MediaTrack" header
- Filter buttons: Wszystkie, Movie, TVShow, Book, Album, Game
- 17 media cards loaded from the API
- Clicking a filter type shows only matching cards
- Clicking "Wszystkie" shows all 17

- [ ] **Step 5: Commit**

```bash
git add frontend/src/main.ts frontend/src/style.css
git commit -m "wire media list with filter UI"
```

import { useState, useMemo, useEffect } from 'react'
import { useMedia } from '../hooks/useMedia'
import type { MediaType } from '../types'
import Header from '../components/Header'
import TypeFilter from '../components/TypeFilter'
import MediaGrid from '../components/MediaGrid'

function getInitialTheme(): 'light' | 'dark' {
  const stored = localStorage.getItem('theme')
  if (stored === 'light' || stored === 'dark') return stored
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export default function CatalogPage() {
  const mediaState = useMedia()
  const [search, setSearch] = useState('')
  const [activeType, setActiveType] = useState<MediaType | null>(null)
  const [theme, setTheme] = useState<'light' | 'dark'>(getInitialTheme)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }, [theme])

  function toggleTheme() {
    setTheme(t => t === 'light' ? 'dark' : 'light')
  }

  const allItems = mediaState.status === 'success' ? mediaState.data : []

  const typeCounts = useMemo(() => {
    const counts: Partial<Record<MediaType, number>> = {}
    for (const item of allItems) {
      counts[item.type] = (counts[item.type] ?? 0) + 1
    }
    return counts
  }, [allItems])

  const filtered = useMemo(() => {
    let items = allItems
    if (activeType) items = items.filter(m => m.type === activeType)
    const q = search.trim().toLowerCase()
    if (q) items = items.filter(m => m.title.toLowerCase().includes(q))
    return items
  }, [allItems, activeType, search])

  const isFiltered = activeType !== null || search.trim() !== ''

  return (
    <div className="app">
      <Header
        search={search}
        onSearch={setSearch}
        totalCount={allItems.length}
        filteredCount={filtered.length}
        theme={theme}
        onToggleTheme={toggleTheme}
      />
      <main className="main">
        <TypeFilter
          activeType={activeType}
          onChange={setActiveType}
          counts={typeCounts}
        />
        <MediaGrid
          status={mediaState.status}
          items={filtered}
          isFiltered={isFiltered}
        />
      </main>
    </div>
  )
}

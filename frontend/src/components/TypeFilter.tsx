import type { MediaType } from '../types'
import { ALL_MEDIA_TYPES, TYPE_LABELS } from '../types'

interface Props {
  activeType: MediaType | null
  onChange: (type: MediaType | null) => void
  counts: Partial<Record<MediaType, number>>
}

export default function TypeFilter({ activeType, onChange, counts }: Props) {
  return (
    <nav className="type-filter" aria-label="Filter by media type">
      <div className="type-filter__scroll">
        <button
          className={`filter-pill${activeType === null ? ' filter-pill--active' : ''}`}
          onClick={() => onChange(null)}
        >
          All
        </button>
        {ALL_MEDIA_TYPES.map(type => {
          const count = counts[type] ?? 0
          return (
            <button
              key={type}
              className={`filter-pill filter-pill--${type.toLowerCase()}${activeType === type ? ' filter-pill--active' : ''}`}
              onClick={() => onChange(type)}
            >
              {TYPE_LABELS[type]}
              {count > 0 && <span className="filter-pill__count">{count}</span>}
            </button>
          )
        })}
      </div>
    </nav>
  )
}

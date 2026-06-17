import type { MediaSummary, MediaType } from '../types'
import { TYPE_LABELS } from '../types'

function getYear(date: string | number[] | null): string {
  if (!date) return '—'
  if (Array.isArray(date)) return String(date[0])
  const d = new Date(date)
  return isNaN(d.getFullYear()) ? '—' : String(d.getFullYear())
}

const TYPE_ICONS: Record<MediaType, JSX.Element> = {
  Movie: (
    <svg viewBox="0 0 16 16" fill="currentColor" className="type-icon">
      <path d="M2 4a2 2 0 012-2h8a2 2 0 012 2v1H2V4zm0 3h12v5a2 2 0 01-2 2H4a2 2 0 01-2-2V7zm4 1.5v3l3-1.5-3-1.5z"/>
    </svg>
  ),
  TVShow: (
    <svg viewBox="0 0 16 16" fill="currentColor" className="type-icon">
      <path d="M1 4.5A1.5 1.5 0 012.5 3h11A1.5 1.5 0 0115 4.5v7A1.5 1.5 0 0113.5 13h-11A1.5 1.5 0 011 11.5v-7zM5 14h6v-1H5v1z"/>
    </svg>
  ),
  Book: (
    <svg viewBox="0 0 16 16" fill="currentColor" className="type-icon">
      <path d="M2 2.5A1.5 1.5 0 013.5 1h9A1.5 1.5 0 0114 2.5v11a1.5 1.5 0 01-1.5 1.5h-9A1.5 1.5 0 012 13.5v-11zM4 3v10h1.5V3H4zm3 0v10h5V3H7z"/>
    </svg>
  ),
  Album: (
    <svg viewBox="0 0 16 16" fill="currentColor" className="type-icon">
      <path d="M8 15A7 7 0 108 1a7 7 0 000 14zm0-9.5a2.5 2.5 0 110 5 2.5 2.5 0 010-5zm0 1.5a1 1 0 100 2 1 1 0 000-2z"/>
    </svg>
  ),
  Game: (
    <svg viewBox="0 0 16 16" fill="currentColor" className="type-icon">
      <path d="M11.5 6H10V4.5a.5.5 0 00-1 0V6H7.5a.5.5 0 000 1H9v1.5a.5.5 0 001 0V7h1.5a.5.5 0 000-1zM2 5a3 3 0 013-3h6a3 3 0 013 3v6a3 3 0 01-3 3H5a3 3 0 01-3-3V5zm10.5 5.5a1 1 0 10-2 0 1 1 0 002 0zm-2.5-1a1 1 0 10-2 0 1 1 0 002 0z"/>
    </svg>
  ),
}

interface Props {
  item: MediaSummary
}

export default function MediaCard({ item }: Props) {
  const typeClass = item.type.toLowerCase()

  return (
    <article className={`media-card media-card--${typeClass}`}>
      <div className="media-card__bar" />
      <img
        className="media-card__image"
        src={`/images/${item.id}.jpg`}
        alt={item.title}
        onError={e => {
          const img = e.currentTarget
          img.style.display = 'none'
          const ph = img.nextElementSibling as HTMLElement | null
          if (ph) ph.style.display = 'flex'
        }}
      />
      <div className="media-card__image-placeholder">
        {TYPE_ICONS[item.type]}
      </div>
      <div className="media-card__body">
        <div className="media-card__meta">
          <span className={`type-badge type-badge--${typeClass}`}>
            {TYPE_ICONS[item.type]}
            {TYPE_LABELS[item.type]}
          </span>
          <span className="media-card__year">{getYear(item.releaseDate)}</span>
        </div>
        <h3 className="media-card__title">{item.title}</h3>
      </div>
    </article>
  )
}

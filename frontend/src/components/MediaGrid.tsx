import type { MediaSummary } from '../types'
import MediaCard from './MediaCard'

interface Props {
  status: 'loading' | 'error' | 'success'
  items: MediaSummary[]
  isFiltered: boolean
}

export default function MediaGrid({ status, items, isFiltered }: Props) {
  if (status === 'loading') {
    return (
      <div className="grid-status">
        <div className="spinner" />
        <p className="grid-status__text">Loading your media library...</p>
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="grid-status grid-status--error">
        <svg className="grid-status__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 8v4m0 4h.01" strokeLinecap="round" />
        </svg>
        <p className="grid-status__text">Could not connect to the backend.</p>
        <p className="grid-status__sub">Make sure the Spring Boot server is running on port 8080.</p>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="grid-status">
        <svg className="grid-status__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" strokeLinecap="round" />
        </svg>
        <p className="grid-status__text">
          {isFiltered ? 'No titles match your filters.' : 'No media in the library yet.'}
        </p>
      </div>
    )
  }

  return (
    <div className="media-grid" role="list">
      {items.map(item => (
        <MediaCard key={item.id} item={item} />
      ))}
    </div>
  )
}

interface Props {
  search: string
  onSearch: (value: string) => void
  totalCount: number
  filteredCount: number
}

export default function Header({ search, onSearch, totalCount, filteredCount }: Props) {
  const showCount = totalCount > 0

  return (
    <header className="header">
      <div className="header__inner">
        <div className="header__logo">
          <div className="logo-mark">M</div>
          <span className="logo-text">MediaTrack</span>
          {showCount && (
            <span className="header__count">
              {filteredCount === totalCount
                ? `${totalCount} titles`
                : `${filteredCount} of ${totalCount}`}
            </span>
          )}
        </div>

        <div className="search-wrapper">
          <svg className="search-icon" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.8">
            <circle cx="8.5" cy="8.5" r="5.5" />
            <path d="M15 15l-3-3" strokeLinecap="round" />
          </svg>
          <input
            className="search-input"
            type="search"
            placeholder="Search titles..."
            value={search}
            onChange={e => onSearch(e.target.value)}
            aria-label="Search media by title"
          />
        </div>
      </div>
    </header>
  )
}

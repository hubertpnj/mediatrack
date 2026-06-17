import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

interface Props {
  search: string
  onSearch: (value: string) => void
  totalCount: number
  filteredCount: number
  theme: 'light' | 'dark'
  onToggleTheme: () => void
}

export default function Header({ search, onSearch, totalCount, filteredCount, theme, onToggleTheme }: Props) {
  const { user, logout } = useAuth()
  const showCount = totalCount > 0

  return (
    <header className="header">
      <div className="header__inner">
        <div className="header__logo">
          <div className="logo-mark">M</div>
          <span className="logo-text">Media<span>Track</span></span>
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

        <div className="header__actions">
          <button className="theme-toggle" onClick={onToggleTheme} aria-label="Toggle dark mode">
            {theme === 'dark' ? (
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4.22 1.78a1 1 0 010 1.42l-.7.7a1 1 0 11-1.42-1.42l.7-.7a1 1 0 011.42 0zM18 9a1 1 0 110 2h-1a1 1 0 110-2h1zM5.78 14.22a1 1 0 010 1.42l-.7.7a1 1 0 11-1.42-1.42l.7-.7a1 1 0 011.42 0zM10 16a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zm-6-7a1 1 0 110 2H3a1 1 0 110-2h1zm1.78-5.22a1 1 0 011.42 0l.7.7a1 1 0 01-1.42 1.42l-.7-.7a1 1 0 010-1.42zM10 6a4 4 0 100 8 4 4 0 000-8z"/>
              </svg>
            ) : (
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z"/>
              </svg>
            )}
          </button>

          {user ? (
            <div className="header__user">
              <span className="header__username">{user.username}</span>
              <button className="auth-link-btn" onClick={() => logout()}>Log out</button>
            </div>
          ) : (
            <div className="header__user">
              <Link to="/login" className="auth-link-btn">Log in</Link>
              <Link to="/register" className="auth-btn-pill">Sign up</Link>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import type { UserInfo } from '../types'
import { apiFetch } from '../lib/api'

interface AuthContextValue {
  user: UserInfo | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    apiFetch<UserInfo>('/api/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const u = await apiFetch<UserInfo>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
    setUser(u)
  }

  async function register(username: string, email: string, password: string) {
    const u = await apiFetch<UserInfo>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    })
    setUser(u)
  }

  async function logout() {
    await apiFetch('/api/auth/logout', { method: 'POST' })
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}

import { useState, useEffect } from 'react'
import type { MediaSummary } from '../types'
import { fetchMedia } from '../api'

export type MediaState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'success'; data: MediaSummary[] }

export function useMedia(): MediaState {
  const [state, setState] = useState<MediaState>({ status: 'loading' })

  useEffect(() => {
    fetchMedia()
      .then(data => setState({ status: 'success', data }))
      .catch((err: Error) => setState({ status: 'error', message: err.message }))
  }, [])

  return state
}

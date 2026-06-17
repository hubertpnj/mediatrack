import { useQuery } from '@tanstack/react-query'
import type { MediaSummary } from '../types'
import { apiFetch } from '../lib/api'

export function useMedia() {
  return useQuery<MediaSummary[]>({
    queryKey: ['media'],
    queryFn: () => apiFetch<MediaSummary[]>('/api/media'),
  })
}

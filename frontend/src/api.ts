import type { MediaSummary } from './types'

export async function fetchMedia(): Promise<MediaSummary[]> {
  const res = await fetch('/api/media')
  if (!res.ok) throw new Error(`Failed to fetch media: ${res.status}`)
  return res.json()
}

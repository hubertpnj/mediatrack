export type MediaType = 'Movie' | 'TVShow' | 'Book' | 'Album' | 'Game'

export const ALL_MEDIA_TYPES: MediaType[] = ['Movie', 'TVShow', 'Book', 'Album', 'Game']

export const TYPE_LABELS: Record<MediaType, string> = {
  Movie: 'Movie',
  TVShow: 'TV Show',
  Book: 'Book',
  Album: 'Album',
  Game: 'Game',
}

export interface MediaSummary {
  id: number
  title: string
  type: MediaType
  releaseDate: string | number[] | null
}

export interface UserInfo {
  id: number
  username: string
  email: string
}

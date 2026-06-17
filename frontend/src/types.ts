export interface MediaSummary {
  id: number
  title: string
  type: 'Movie' | 'TVShow' | 'Book' | 'Album' | 'Game'
  releaseDate: string | null
}

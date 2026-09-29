// Mirrors the backend error contract (pe.edu.utp.ebenezer.exception.ApiError).
export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: Record<string, string>
}

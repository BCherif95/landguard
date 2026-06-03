import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
} from "axios"
import { env } from "@/lib/config/env"
import { ApiError, ApiErrorBody, AuthResponse } from "./types"

type RetryableConfig = InternalAxiosRequestConfig & { _retry?: boolean }

// Side-channel hooks the auth store registers at boot. The Axios client lives in
// a non-React module, so we communicate via these globals — the store owns the
// truth, the client only asks "give me a token" / "rotate".
type TokenAccessor = () => string | null
type RefreshHandler = () => Promise<string | null>
type LogoutHandler = () => void

let getAccessToken: TokenAccessor = () => null
let refreshTokens: RefreshHandler = async () => null
let onAuthLost: LogoutHandler = () => {}

export function bindAuthHandlers(handlers: {
  getAccessToken: TokenAccessor
  refreshTokens: RefreshHandler
  onAuthLost: LogoutHandler
}) {
  getAccessToken = handlers.getAccessToken
  refreshTokens = handlers.refreshTokens
  onAuthLost = handlers.onAuthLost
}

export const api: AxiosInstance = axios.create({
  baseURL: env.NEXT_PUBLIC_API_URL,
  timeout: 15_000,
  headers: { "Content-Type": "application/json" },
})

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let pendingRefresh: Promise<string | null> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorBody>) => {
    const original = error.config as RetryableConfig | undefined

    if (
      error.response?.status === 401 &&
      original &&
      !original._retry &&
      !isAuthEndpoint(original.url)
    ) {
      original._retry = true
      try {
        const newAccess = await (pendingRefresh ?? (pendingRefresh = refreshTokens()))
        pendingRefresh = null
        if (newAccess) {
          original.headers.Authorization = `Bearer ${newAccess}`
          return api.request(original)
        }
      } catch {
        pendingRefresh = null
      }
      onAuthLost()
    }

    if (error.response?.data) {
      return Promise.reject(new ApiError(error.response.data))
    }
    return Promise.reject(
      new ApiError({
        timestamp: new Date().toISOString(),
        status: error.response?.status ?? 0,
        code: "NETWORK_ERROR",
        message: error.message ?? "Erreur réseau.",
        path: original?.url ?? "",
        errors: [],
      }),
    )
  },
)

function isAuthEndpoint(url?: string) {
  if (!url) return false
  return /\/(v1\/)?auth\/(login|register|refresh|logout)$/.test(url)
}

export async function rawRefresh(refreshToken: string): Promise<AuthResponse> {
  const { data } = await axios.post<AuthResponse>(
    `${env.NEXT_PUBLIC_API_URL}/auth/refresh`,
    { refreshToken },
    { headers: { "Content-Type": "application/json" } },
  )
  return data
}

export type { AxiosRequestConfig }

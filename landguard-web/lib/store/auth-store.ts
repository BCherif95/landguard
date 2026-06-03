import { create } from "zustand"
import { persist, createJSONStorage } from "zustand/middleware"
import { bindAuthHandlers, rawRefresh } from "@/lib/api/client"
import { authApi } from "@/lib/api/auth"
import type { AuthResponse, AuthenticatedUser } from "@/lib/api/types"

interface AuthState {
  user: AuthenticatedUser | null
  accessToken: string | null
  refreshToken: string | null
  accessTokenExpiresAt: string | null
  refreshTokenExpiresAt: string | null
  hydrated: boolean

  setSession: (response: AuthResponse) => void
  setUser: (user: AuthenticatedUser) => void
  clear: () => void
  markHydrated: () => void
  refresh: () => Promise<string | null>
}

const emptySession = {
  user: null,
  accessToken: null,
  refreshToken: null,
  accessTokenExpiresAt: null,
  refreshTokenExpiresAt: null,
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      ...emptySession,
      hydrated: false,

      setSession: (response) =>
        set({
          user: response.user ?? get().user,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          accessTokenExpiresAt: response.accessTokenExpiresAt,
          refreshTokenExpiresAt: response.refreshTokenExpiresAt,
        }),

      setUser: (user) => set({ user }),

      clear: () => set({ ...emptySession }),

      markHydrated: () => set({ hydrated: true }),

      refresh: async () => {
        const current = get().refreshToken
        if (!current) return null
        try {
          const response = await rawRefresh(current)
          set({
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
            accessTokenExpiresAt: response.accessTokenExpiresAt,
            refreshTokenExpiresAt: response.refreshTokenExpiresAt,
          })
          return response.accessToken
        } catch {
          set({ ...emptySession })
          return null
        }
      },
    }),
    {
      name: "laboussole.auth",
      storage: createJSONStorage(() => {
        if (typeof window === "undefined") {
          return {
            getItem: () => null,
            setItem: () => {},
            removeItem: () => {},
          }
        }
        return window.localStorage
      }),
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        accessTokenExpiresAt: state.accessTokenExpiresAt,
        refreshTokenExpiresAt: state.refreshTokenExpiresAt,
      }),
      onRehydrateStorage: () => (state) => {
        state?.markHydrated()
      },
    },
  ),
)

// Wire the Axios client to the store. Done at module load so any first request
// already has a working refresh handler.
bindAuthHandlers({
  getAccessToken: () => useAuthStore.getState().accessToken,
  refreshTokens: () => useAuthStore.getState().refresh(),
  onAuthLost: () => useAuthStore.getState().clear(),
})

export async function logoutEverywhere(): Promise<void> {
  const { refreshToken, clear } = useAuthStore.getState()
  try {
    if (refreshToken) await authApi.logout(refreshToken)
  } catch {
    // ignore — server may already have invalidated the session
  } finally {
    clear()
  }
}

import { api } from "./client"
import {
  AuthResponse,
  AuthenticatedUser,
  LoginPayload,
  RegisterPayload,
} from "./types"

export const authApi = {
  register: async (payload: RegisterPayload): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>("/auth/register", payload)
    return data
  },

  login: async (payload: LoginPayload): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>("/auth/login", payload)
    return data
  },

  logout: async (refreshToken: string | null): Promise<void> => {
    await api.post("/auth/logout", refreshToken ? { refreshToken } : {})
  },

  me: async (): Promise<AuthenticatedUser> => {
    const { data } = await api.get<AuthenticatedUser>("/auth/me")
    return data
  },
}

"use client"

import { useEffect, type ReactNode } from "react"
import { useQuery } from "@tanstack/react-query"
import { authApi } from "@/lib/api/auth"
import { useAuthStore } from "@/lib/store/auth-store"

/**
 * Mounted once at the root layout. While the auth store hydrates from localStorage
 * and we have an access token, fetches /auth/me so the UI carries the freshest profile.
 */
export function AuthBootstrap({ children }: { children: ReactNode }) {
  const hydrated = useAuthStore((s) => s.hydrated)
  const accessToken = useAuthStore((s) => s.accessToken)
  const setUser = useAuthStore((s) => s.setUser)

  const { data } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: authApi.me,
    enabled: hydrated && Boolean(accessToken),
    staleTime: 60_000,
  })

  useEffect(() => {
    if (data) setUser(data)
  }, [data, setUser])

  return <>{children}</>
}

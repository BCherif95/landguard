"use client"

import { useAuthStore } from "@/lib/store/auth-store"

export function useCurrentUser() {
  return useAuthStore((s) => s.user)
}

export function useIsAuthenticated() {
  return useAuthStore((s) => Boolean(s.accessToken && s.user))
}

export function useAuthHydrated() {
  return useAuthStore((s) => s.hydrated)
}

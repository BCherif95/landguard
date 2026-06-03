"use client"

import { useEffect, type ReactNode } from "react"
import { useRouter, usePathname } from "next/navigation"
import { useAuthHydrated, useIsAuthenticated } from "@/lib/hooks/use-current-user"
import { useAuthStore } from "@/lib/store/auth-store"

/**
 * Client-side guard for the (app) route group. Tokens live in localStorage so the
 * Next.js middleware cannot inspect them — we wait for the auth store to hydrate,
 * then redirect unauthenticated visitors to /auth?next=<original>.
 */
export function AuthGuard({ children }: { children: ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const hydrated = useAuthHydrated()
  const isAuthenticated = useIsAuthenticated()
  const accessToken = useAuthStore((s) => s.accessToken)

  useEffect(() => {
    if (!hydrated) return
    if (!accessToken) {
      const next = pathname ? `?next=${encodeURIComponent(pathname)}` : ""
      router.replace(`/auth${next}`)
    }
  }, [hydrated, accessToken, pathname, router])

  if (!hydrated || !accessToken) {
    return (
      <div className="flex min-h-svh items-center justify-center bg-background">
        <div className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
          Chargement de la session…
        </div>
      </div>
    )
  }

  // We know we have a token; the user object may still be null briefly while
  // /auth/me resolves — render the app shell anyway so the chrome shows up.
  void isAuthenticated
  return <>{children}</>
}

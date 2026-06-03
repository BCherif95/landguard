import type { ReactNode } from "react"
import { AppSidebar } from "@/components/app/app-sidebar"
import { AuthGuard } from "@/components/auth/auth-guard"
import { AppErrorBoundary } from "@/components/app/error-boundary"

export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <AuthGuard>
      <div className="flex min-h-svh bg-background">
        <AppSidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <AppErrorBoundary name="Application">
            {children}
          </AppErrorBoundary>
        </div>
      </div>
    </AuthGuard>
  )
}

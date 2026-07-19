"use client"

import { Logo } from "@/components/brand/logo"

interface AppTopbarProps {
  title: string
  subtitle?: string
  /** Optional right-side content (live status, page actions). */
  actions?: React.ReactNode
}

export function AppTopbar({ title, subtitle, actions }: AppTopbarProps) {
  return (
    <header className="sticky top-0 z-40 flex h-16 items-center gap-3 border-b border-border/60 bg-background/70 px-4 backdrop-blur-xl sm:px-6">
      <div className="flex items-center gap-3 lg:hidden">
        <Logo showWordmark={false} />
      </div>
      <div className="min-w-0 flex-1">
        <h1 className="truncate font-display text-lg tracking-tight text-foreground sm:text-xl">
          {title}
        </h1>
        {subtitle && (
          <p className="truncate font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {subtitle}
          </p>
        )}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </header>
  )
}

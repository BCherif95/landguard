"use client"

import { Search, Command, Bell, ShieldCheck } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Logo } from "@/components/brand/logo"

interface AppTopbarProps {
  title: string
  subtitle?: string
}

export function AppTopbar({ title, subtitle }: AppTopbarProps) {
  return (
    <header className="sticky top-0 z-40 flex h-16 items-center gap-3 border-b border-border/60 bg-background/70 px-4 backdrop-blur-xl sm:px-6">
      <div className="flex items-center gap-3 lg:hidden">
        <Logo showWordmark={false} />
      </div>
      <div className="min-w-0 flex-1">
        <h1 className="truncate font-display text-lg font-medium tracking-tight text-foreground sm:text-xl">
          {title}
        </h1>
        {subtitle && (
          <p className="truncate font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {subtitle}
          </p>
        )}
      </div>

      {/* Command palette trigger */}
      <button
        type="button"
        className="hidden h-9 items-center gap-2 rounded-lg border border-border bg-card/60 px-3 text-sm text-muted-foreground transition-colors hover:bg-secondary md:flex"
        aria-label="Rechercher"
      >
        <Search className="h-4 w-4" />
        <span>Rechercher une parcelle, un titre…</span>
        <span className="ml-2 inline-flex items-center gap-1 rounded border border-border bg-background px-1.5 py-0.5 font-mono text-[10px]">
          <Command className="h-2.5 w-2.5" />K
        </span>
      </button>

      <div className="flex items-center gap-2">
        <span className="hidden items-center gap-1.5 rounded-md border border-emerald/30 bg-emerald-soft px-2 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald sm:inline-flex">
          <ShieldCheck className="h-3 w-3" />
          MFA actif
        </span>

        <Button
          variant="ghost"
          size="icon"
          className="relative h-9 w-9 text-muted-foreground hover:text-foreground"
          aria-label="Notifications"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-danger" />
        </Button>
      </div>
    </header>
  )
}

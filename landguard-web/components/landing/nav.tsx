"use client"

import Link from "next/link"
import { ArrowUpRight } from "lucide-react"
import { Logo } from "@/components/brand/logo"
import { Button } from "@/components/ui/button"

const navItems = [
  { label: "Plateforme", href: "#plateforme" },
  { label: "Modules", href: "#modules" },
  { label: "Profils", href: "#profils" },
  { label: "Sécurité", href: "#securite" },
  { label: "Tarifs", href: "#tarifs" },
]

export function LandingNav() {
  return (
    <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/70 backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-6 px-6">
        <Link href="/" aria-label="LA BOUSSOLE — Accueil">
          <Logo />
        </Link>
        <nav className="hidden items-center gap-1 md:flex" aria-label="Navigation principale">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="flex items-center gap-2">
          <Button
            asChild
            variant="ghost"
            size="sm"
            className="hidden text-muted-foreground hover:text-foreground sm:inline-flex"
          >
            <Link href="/auth">Se connecter</Link>
          </Button>
          <Button
            asChild
            size="sm"
            className="bg-emerald text-primary-foreground hover:bg-emerald/90"
          >
            <Link href="/dashboard" className="gap-1.5">
              Accéder à la plateforme
              <ArrowUpRight className="h-3.5 w-3.5" />
            </Link>
          </Button>
        </div>
      </div>
    </header>
  )
}

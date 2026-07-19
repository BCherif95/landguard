"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import {
  LayoutDashboard,
  Satellite,
  FolderLock,
  Link2,
  Users,
  Map,
  ClipboardCheck,
  ShieldCheck,
  FileSignature,
  LogOut,
  type LucideIcon,
} from "lucide-react"
import { Logo } from "@/components/brand/logo"
import { cn } from "@/lib/utils"
import { useCurrentUser } from "@/lib/hooks/use-current-user"
import { logoutEverywhere } from "@/lib/store/auth-store"

/** Roles allowed into the expert instruction console (PRD Feature 02.2). */
const EXPERT_ROLES = ["OFFICER", "LEGAL", "ADMIN"]

interface NavItem {
  label: string
  href: string
  icon: LucideIcon
  badge?: string
  expertOnly?: boolean
}

// One entry per PRD module, using the cahier des charges nomenclature:
// Épic 1 Vision Live, Épic 2 Certification « Double Clé », Épic 3
// Surveillance & preuves, Épic 4 Conseil de Famille, plus le scellement
// cryptographique transverse.
const primary: NavItem[] = [
  { label: "Tableau de bord", href: "/dashboard", icon: LayoutDashboard },
  { label: "Vision Live", href: "/carte", icon: Map, badge: "LIVE" },
  { label: "Registre foncier", href: "/registre", icon: FolderLock },
  { label: "Certification Double Clé", href: "/verification", icon: ShieldCheck },
  { label: "Console d'instruction", href: "/certification", icon: ClipboardCheck, expertOnly: true },
  { label: "Surveillance & Alertes", href: "/surveillance", icon: Satellite },
  { label: "Dossier de preuve", href: "/preuves", icon: FileSignature },
  { label: "Conseil de Famille", href: "/heritage", icon: Users },
  { label: "Scellement blockchain", href: "/blockchain", icon: Link2 },
]

export function AppSidebar() {
  const pathname = usePathname()
  const router = useRouter()
  const user = useCurrentUser()

  const handleLogout = async () => {
    await logoutEverywhere()
    router.replace("/auth")
  }

  const initials = user?.fullName
    ? user.fullName
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((p) => p[0]?.toUpperCase() || "")
        .join("")
    : "··"

  const ROLE_LABELS: Record<string, string> = {
    CITIZEN: "Citoyen",
    OFFICER: "Agent cadastral",
    LEGAL: "Notaire / Juriste",
    BANKER: "Banque",
    ADMIN: "Administrateur",
  }

  return (
    <aside className="hidden h-svh w-64 shrink-0 flex-col border-r border-border/60 bg-card/40 backdrop-blur-xl lg:flex">
      <div className="flex h-16 items-center border-b border-border/60 px-5">
        <Link href="/" aria-label="Accueil LA BOUSSOLE">
          <Logo />
        </Link>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-4" aria-label="Navigation principale">
        <div className="px-2 pb-2 font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
          Plateforme
        </div>
        <ul className="space-y-0.5">
          {primary
            .filter((item) => !item.expertOnly || (user?.role && EXPERT_ROLES.includes(user.role)))
            .map((item) => {
            const active =
              pathname === item.href ||
              (item.href !== "/dashboard" && pathname?.startsWith(item.href))
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    "group relative flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm transition-all",
                    active
                      ? "bg-emerald-soft text-foreground"
                      : "text-muted-foreground hover:bg-secondary hover:text-foreground",
                  )}
                >
                  {active && (
                    <span
                      aria-hidden
                      className="absolute inset-y-1.5 left-0 w-0.5 rounded-r bg-emerald"
                    />
                  )}
                  <item.icon
                    className={cn(
                      "h-4 w-4 shrink-0",
                      active ? "text-emerald" : "text-muted-foreground group-hover:text-foreground",
                    )}
                  />
                  <span className="flex-1 truncate">{item.label}</span>
                  {item.badge && (
                    <span className="ml-auto rounded bg-emerald/15 px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-[0.18em] text-emerald">
                      {item.badge}
                    </span>
                  )}
                </Link>
              </li>
            )
          })}
        </ul>
      </nav>

      {/* User card */}
      <div className="border-t border-border/60 p-3">
        <div className="flex items-center gap-3 rounded-lg border border-border bg-background/40 p-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-emerald to-info font-display text-xs font-medium text-primary-foreground">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <div className="truncate text-sm font-medium text-foreground">
              {user?.fullName ?? "Chargement…"}
            </div>
            <div className="truncate font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
              {user?.role ? ROLE_LABELS[user.role] ?? user.role : "—"}
            </div>
          </div>
          <button
            type="button"
            onClick={handleLogout}
            aria-label="Se déconnecter"
            className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </aside>
  )
}

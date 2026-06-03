"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import {
  LayoutDashboard,
  Satellite,
  FolderLock,
  Link2,
  Scale,
  Users,
  Banknote,
  Bell,
  Settings,
  LifeBuoy,
  Map,
  Smartphone,
  ScanSearch,
  ShieldCheck,
  FileSignature,
  LogOut,
} from "lucide-react"
import { Logo } from "@/components/brand/logo"
import { cn } from "@/lib/utils"
import { useCurrentUser } from "@/lib/hooks/use-current-user"
import { logoutEverywhere } from "@/lib/store/auth-store"

const primary = [
  { label: "Vision Live", href: "/dashboard", icon: LayoutDashboard, badge: "LIVE" },
  { label: "Carte cadastrale", href: "/carte", icon: Map },
  { label: "Surveillance", href: "/surveillance", icon: Satellite, badge: "3" },
  { label: "Registre foncier", href: "/registre", icon: FolderLock },
  { label: "Vérification IA", href: "/verification", icon: ScanSearch },
  { label: "Blockchain", href: "/blockchain", icon: Link2 },
  { label: "Certification TF", href: "/certification", icon: ShieldCheck, badge: "NEW" },
  { label: "Preuves juridiques", href: "/preuves", icon: FileSignature },
  { label: "Juridique IA", href: "/juridique", icon: Scale },
  { label: "Héritage", href: "/heritage", icon: Users },
  { label: "Banque & Valorisation", href: "/banque", icon: Banknote },
  { label: "App Terrain", href: "/mobile", icon: Smartphone },
]

const secondary = [
  { label: "Notifications", href: "/notifications", icon: Bell },
  { label: "Paramètres", href: "/parametres", icon: Settings },
  { label: "Assistance", href: "/assistance", icon: LifeBuoy },
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
          {primary.map((item) => {
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
                    <span
                      className={cn(
                        "ml-auto rounded px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-[0.18em]",
                        item.badge === "LIVE"
                          ? "bg-emerald/15 text-emerald"
                          : "bg-danger/15 text-danger",
                      )}
                    >
                      {item.badge}
                    </span>
                  )}
                </Link>
              </li>
            )
          })}
        </ul>

        <div className="mt-6 px-2 pb-2 font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
          Compte
        </div>
        <ul className="space-y-0.5">
          {secondary.map((item) => {
            const active = pathname?.startsWith(item.href)
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    "flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm transition-colors",
                    active
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:bg-secondary hover:text-foreground",
                  )}
                >
                  <item.icon className="h-4 w-4 shrink-0" />
                  <span className="truncate">{item.label}</span>
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

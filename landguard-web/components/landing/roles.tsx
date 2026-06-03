import {
  ShieldCheck,
  Landmark,
  Gavel,
  Building2,
  TrendingUp,
  User,
  Users,
  ScrollText,
} from "lucide-react"

const roles = [
  {
    icon: ShieldCheck,
    name: "Super Admin",
    description: "Supervision globale, audit logs et gouvernance plateforme.",
  },
  {
    icon: ScrollText,
    name: "Agent cadastral",
    description: "Vérifications terrain, validation des coordonnées GPS.",
  },
  {
    icon: Landmark,
    name: "Notaire",
    description: "Signature électronique et authentification des titres.",
  },
  {
    icon: Building2,
    name: "Banque",
    description: "Scoring foncier, garanties et indicateurs de solvabilité.",
  },
  {
    icon: Gavel,
    name: "Tribunal",
    description: "Preuves horodatées exportables et historiques opposables.",
  },
  {
    icon: TrendingUp,
    name: "Investisseur",
    description: "Portefeuille foncier certifié et potentiel économique.",
  },
  {
    icon: User,
    name: "Citoyen",
    description: "Surveillance de son terrain, alertes et certificats.",
  },
  {
    icon: Users,
    name: "Héritier",
    description: "Validation collective, parts et historique familial.",
  },
]

export function LandingRoles() {
  return (
    <section
      id="profils"
      className="relative border-b border-border/40 py-24 sm:py-32"
    >
      <div className="mx-auto max-w-7xl px-6">
        <div className="grid gap-12 lg:grid-cols-[1fr_1.4fr] lg:items-center">
          <div>
            <span className="font-mono text-xs uppercase tracking-[0.22em] text-emerald">
              Pour chaque acteur du foncier
            </span>
            <h2 className="mt-4 font-display text-4xl font-medium tracking-tight text-balance text-foreground sm:text-5xl">
              Une expérience taillée pour votre rôle.
            </h2>
            <p className="mt-4 text-pretty text-muted-foreground">
              Huit espaces dédiés, chacun avec ses tableaux de bord, ses
              vérifications et ses garanties d&apos;intégrité — du citoyen propriétaire
              au tribunal foncier.
            </p>
            <div className="mt-8 flex items-center gap-2 rounded-lg border border-border bg-card/60 p-3">
              <span className="h-2 w-2 rounded-full bg-emerald animate-pulse" />
              <span className="font-mono text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
                Authentification MFA · Biométrie · RBAC
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 sm:grid-cols-2">
            {roles.map((r) => (
              <div
                key={r.name}
                className="group relative rounded-xl border border-border bg-card/60 p-4 transition-all hover:border-emerald/40 hover:bg-card"
              >
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-md bg-secondary text-emerald">
                    <r.icon className="h-4.5 w-4.5" />
                  </div>
                  <span className="font-display text-sm font-medium tracking-tight text-foreground">
                    {r.name}
                  </span>
                </div>
                <p className="mt-2.5 text-xs leading-relaxed text-muted-foreground">
                  {r.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

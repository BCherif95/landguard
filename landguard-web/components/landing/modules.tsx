import {
  Eye,
  Satellite,
  FileCheck2,
  Link2,
  Scale,
  Users,
  Banknote,
  Smartphone,
} from "lucide-react"

const modules = [
  {
    icon: Eye,
    name: "Vision Live",
    tagline: "Centre de commandement",
    description:
      "Tableau de bord central avec carte satellite, timeline historique, time-lapse et détection IA d'activité suspecte.",
  },
  {
    icon: Satellite,
    name: "Surveillance active",
    tagline: "Monitoring géospatial",
    description:
      "Détection automatisée de constructions, terrassements, engins et occupations illégales avec heatmaps prédictives.",
  },
  {
    icon: FileCheck2,
    name: "Registre numérique",
    tagline: "Titres certifiés",
    description:
      "Création de terrain, OCR intelligent, signature électronique et certification blockchain — détection des faux documents.",
  },
  {
    icon: Link2,
    name: "Blockchain",
    tagline: "Traçabilité immuable",
    description:
      "Timeline visuelle de toutes les transactions, transferts et vérifications. Hash unique et certificat numérique par parcelle.",
  },
  {
    icon: Scale,
    name: "Générateur juridique",
    tagline: "Preuves opposables",
    description:
      "Export PDF avec images satellite horodatées, coordonnées GPS, historique et signatures — bouton « Preuve tribunal ».",
  },
  {
    icon: Users,
    name: "Héritage familial",
    tagline: "Multi-propriété",
    description:
      "Gestion des héritiers, validation collective, pourcentage de propriété et historique transgénérationnel.",
  },
  {
    icon: Banknote,
    name: "Banque & Valorisation",
    tagline: "Scoring crédit",
    description:
      "Estimation, profil foncier certifié, indicateur de solvabilité et risque crédit pour garanties bancaires.",
  },
  {
    icon: Smartphone,
    name: "Application terrain",
    tagline: "Mobile-first · Hors ligne",
    description:
      "Scan QR, capture photo géolocalisée, notes vocales et assistant IA en français, bambara et wolof.",
  },
]

export function LandingModules() {
  return (
    <section
      id="modules"
      className="relative border-b border-border/40 py-24 sm:py-32"
    >
      <div className="mx-auto max-w-7xl px-6">
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-mono text-xs uppercase tracking-[0.22em] text-emerald">
            Plateforme modulaire
          </span>
          <h2 className="mt-4 font-display text-4xl font-medium tracking-tight text-balance text-foreground sm:text-5xl">
            Huit modules. Une seule vérité foncière.
          </h2>
          <p className="mt-4 text-pretty text-muted-foreground">
            De la détection satellite au registre blockchain, chaque module renforce
            l&apos;intégrité de votre patrimoine foncier.
          </p>
        </div>

        <div className="mt-16 grid grid-cols-1 gap-px overflow-hidden rounded-2xl border border-border bg-border sm:grid-cols-2 lg:grid-cols-4">
          {modules.map((m, i) => (
            <article
              key={m.name}
              className="group relative bg-card p-6 transition-colors hover:bg-secondary"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-emerald/30 bg-emerald-soft text-emerald">
                  <m.icon className="h-5 w-5" />
                </div>
                <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                  Module {String(i + 1).padStart(2, "0")}
                </span>
              </div>
              <h3 className="mt-5 font-display text-xl font-medium tracking-tight text-foreground">
                {m.name}
              </h3>
              <p className="mt-1 text-xs text-emerald">{m.tagline}</p>
              <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
                {m.description}
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}

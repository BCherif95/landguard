import {
  Eye,
  Satellite,
  Link2,
  Scale,
  Users,
  ShieldCheck,
} from "lucide-react"

// One card per module of the cahier des charges (épics 1–4 + scellement).
const modules = [
  {
    icon: Eye,
    name: "Vision Live",
    tagline: "Jumeau numérique cadastral",
    description:
      "Carte hybride plein écran, conversion des coordonnées UTM de votre plan papier et curseur temporel pour rejouer l'historique visuel du terrain.",
  },
  {
    icon: ShieldCheck,
    name: "Certification Double Clé",
    tagline: "Certitude juridique",
    description:
      "Dépôt structuré selon la nomenclature malienne, instruction par des experts (réquisition aux Domaines) et Certificat de Vigilance à QR code dynamique.",
  },
  {
    icon: Satellite,
    name: "Surveillance active",
    tagline: "Alerte intrusion",
    description:
      "Analyse des écarts pixels insensible aux saisons, alerte au-delà de 75 % de confiance d'intrusion, notification push, e-mail et SMS simultanés.",
  },
  {
    icon: Scale,
    name: "Dossier de preuve",
    tagline: "Preuves opposables",
    description:
      "Export PDF officiel : identité certifiée, coordonnées exactes, images satellites avant/après horodatées — prêt à remettre à un huissier à Bamako.",
  },
  {
    icon: Users,
    name: "Conseil de Famille",
    tagline: "Successions protégées",
    description:
      "Héritiers associés au même titre, vote collectif sur le plan de succession, notifications partagées : aucun membre ne peut agir en cachette.",
  },
  {
    icon: Link2,
    name: "Scellement cryptographique",
    tagline: "Chaîne de validation",
    description:
      "Empreinte SHA-256 de chaque document, ancrage en chaîne de hachage : toute modification rompt la chaîne de validation.",
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

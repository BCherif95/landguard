import { Brain, Satellite, Hexagon } from "lucide-react"

const pillars = [
  {
    icon: Satellite,
    title: "Œil satellite",
    eyebrow: "01 — Imagerie",
    description:
      "Acquisition d'imagerie satellite et suivi visuel du terrain dans le temps, pour constater tout changement sur une parcelle surveillée.",
    bullets: [
      "Surveillance continue du territoire",
      "Historique visuel du terrain",
      "Clichés horodatés et traçables",
    ],
  },
  {
    icon: Brain,
    title: "Cerveau IA",
    eyebrow: "02 — Intelligence",
    description:
      "Analyse automatisée au service des agents et des propriétaires — lecture assistée des titres et signalement des changements suspects, toujours confirmés par un expert humain.",
    bullets: [
      "Détection de changements suspects",
      "Lecture assistée des titres fonciers",
      "Validation humaine systématique",
    ],
  },
  {
    icon: Hexagon,
    title: "Cœur blockchain",
    eyebrow: "03 — Confiance",
    description:
      "Chaque étape clé du cycle de vie d'une parcelle est ancrée dans un registre horodaté et infalsifiable, consultable à tout moment.",
    bullets: [
      "Empreinte SHA-256",
      "Ancrages horodatés",
      "Preuves vérifiables à tout moment",
    ],
  },
]

export function LandingPlatform() {
  return (
    <section
      id="plateforme"
      className="relative border-b border-border/40 py-24 sm:py-32"
    >
      <div className="mx-auto max-w-7xl px-6">
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-mono text-xs uppercase tracking-[0.22em] text-emerald">
            La plateforme
          </span>
          <h2 className="mt-4 font-display text-4xl font-medium tracking-tight text-balance text-foreground sm:text-5xl">
            Trois piliers, une seule infrastructure de confiance.
          </h2>
          <p className="mt-4 text-pretty text-muted-foreground">
            LA BOUSSOLE combine la précision satellite, l&apos;intelligence des modèles
            spécialisés foncier et l&apos;immutabilité de la blockchain.
          </p>
        </div>

        <div className="mt-16 grid gap-3 lg:grid-cols-3">
          {pillars.map((p) => (
            <article
              key={p.title}
              className="relative overflow-hidden rounded-2xl border border-border bg-card/60 p-6 transition-colors hover:bg-card"
            >
              <div
                aria-hidden
                className="absolute -right-12 -top-12 h-40 w-40 rounded-full opacity-20 blur-3xl"
                style={{ background: "var(--emerald)" }}
              />
              <span className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
                {p.eyebrow}
              </span>
              <div className="mt-3 flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-lg border border-emerald/30 bg-emerald-soft text-emerald">
                  <p.icon className="h-5 w-5" />
                </div>
                <h3 className="font-display text-2xl font-medium tracking-tight text-foreground">
                  {p.title}
                </h3>
              </div>
              <p className="mt-4 text-sm leading-relaxed text-muted-foreground">
                {p.description}
              </p>
              <ul className="mt-5 space-y-2">
                {p.bullets.map((b) => (
                  <li
                    key={b}
                    className="flex items-center gap-2 font-mono text-[11px] uppercase tracking-[0.16em] text-foreground/80"
                  >
                    <span className="h-1 w-1 rounded-full bg-emerald" />
                    {b}
                  </li>
                ))}
              </ul>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}

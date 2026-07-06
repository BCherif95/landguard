import { Satellite, BellRing, Hexagon, Scale } from "lucide-react"

// Deliberately qualitative: no figures are shown here until a real
// aggregation endpoint feeds them. An honest claim beats an invented number.
const commitments = [
  {
    icon: Satellite,
    title: "Surveillance satellite",
    description: "Suivi continu des parcelles enregistrées",
  },
  {
    icon: BellRing,
    title: "Alertes en temps réel",
    description: "Notification immédiate en cas d'activité suspecte",
  },
  {
    icon: Hexagon,
    title: "Preuves infalsifiables",
    description: "Ancrage blockchain des étapes clés du dossier",
  },
  {
    icon: Scale,
    title: "Conformité juridique",
    description: "Workflow aligné sur la réglementation foncière malienne",
  },
]

export function LandingStats() {
  return (
    <section className="border-b border-border/40 bg-card/30">
      <div className="mx-auto grid max-w-7xl grid-cols-2 gap-px bg-border md:grid-cols-4">
        {commitments.map((c) => (
          <div key={c.title} className="bg-background px-6 py-10 text-center">
            <c.icon className="mx-auto h-6 w-6 text-emerald" />
            <div className="mt-3 font-display text-lg font-medium tracking-tight text-foreground">
              {c.title}
            </div>
            <div className="mt-2 text-xs text-muted-foreground">
              {c.description}
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

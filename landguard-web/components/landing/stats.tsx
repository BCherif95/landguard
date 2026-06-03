const stats = [
  { value: "2 847", label: "Parcelles certifiées" },
  { value: "14", label: "Pays africains couverts" },
  { value: "99,8 %", label: "Détection IA — précision" },
  { value: "< 6 min", label: "Délai d'alerte moyen" },
]

export function LandingStats() {
  return (
    <section className="border-b border-border/40 bg-card/30">
      <div className="mx-auto grid max-w-7xl grid-cols-2 gap-px bg-border md:grid-cols-4">
        {stats.map((s) => (
          <div key={s.label} className="bg-background px-6 py-10 text-center">
            <div className="font-display text-4xl font-medium tracking-tight text-foreground sm:text-5xl">
              {s.value}
            </div>
            <div className="mt-2 font-mono text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
              {s.label}
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

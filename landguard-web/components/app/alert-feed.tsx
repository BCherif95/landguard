import { resolveSeverityMeta } from "@/lib/utils/safe-resolvers"
import type { AnomalyEvent } from "@/lib/types"
import { parcels } from "@/lib/mock-data"
import { cn } from "@/lib/utils"

interface AlertFeedProps {
  alerts: AnomalyEvent[]
  className?: string
}

const TYPE_LABEL: Record<string, string> = {
  construction: "Construction",
  earthworks: "Terrassement",
  vehicle: "Engin lourd",
  "illegal-occupation": "Occupation illégale",
  "boundary-shift": "Décalage de bornage",
  "vegetation-loss": "Perte de végétation",
}

export function AlertFeed({ alerts, className }: AlertFeedProps) {
  return (
    <div className={cn("rounded-xl border border-border bg-card/60", className)}>
      <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
        <div>
          <h3 className="font-display text-sm font-medium text-foreground">
            Flux d&apos;alertes IA
          </h3>
          <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {(alerts || []).length} événements actifs
          </p>
        </div>
        <span className="flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald">
          <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-emerald" />
          Temps réel
        </span>
      </div>
      <ul className="divide-y divide-border/60">
        {(alerts || []).map((a) => {
          const meta = resolveSeverityMeta(a.severity)
          const Icon = meta.icon
          const parcel = parcels.find((p) => p.id === a.parcelId)
          return (
            <li
              key={a.id}
              className="flex gap-3 px-4 py-3 transition-colors hover:bg-secondary/40"
            >
              <Icon
                className={cn("mt-0.5 h-4 w-4 shrink-0", meta.tone)}
              />
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <span
                    className={cn(
                      "font-mono text-[10px] uppercase tracking-[0.18em]",
                      meta.tone,
                    )}
                  >
                    {meta.label}
                  </span>
                  <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                    · {TYPE_LABEL[a.type] || a.type}
                  </span>
                  <span className="ml-auto font-mono text-[10px] text-muted-foreground">
                    {a.detectedAt}
                  </span>
                </div>
                <p className="mt-1 text-sm text-foreground">{a.description}</p>
                <div className="mt-1.5 flex items-center justify-between">
                  <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                    {parcel?.name || "Parcelle inconnue"} · {parcel?.region || "Secteur inconnu"}
                  </span>
                  <span className="font-mono text-[10px] text-emerald">
                    Confiance {a.confidence}%
                  </span>
                </div>
              </div>
            </li>
          )
        })}
      </ul>
    </div>
  )
}

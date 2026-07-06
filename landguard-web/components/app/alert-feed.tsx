import { resolveSeverityMeta, resolveMonitoringTypeMeta } from "@/lib/utils/safe-resolvers"
import type { MonitoringEvent } from "@/lib/api/types"
import type { Parcel } from "@/lib/api/parcels"
import { formatDateTime } from "@/lib/api/parcel-display"
import { cn } from "@/lib/utils"

interface AlertFeedProps {
  /** Real monitoring events from the API — most recent first. */
  events: MonitoringEvent[]
  /** Parcels visible to the user, used to label each event with its parcel. */
  parcels: Parcel[]
  className?: string
}

export function AlertFeed({ events, parcels, className }: AlertFeedProps) {
  return (
    <div className={cn("rounded-xl border border-border bg-card/60", className)}>
      <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
        <div>
          <h3 className="font-display text-sm font-medium text-foreground">
            Flux d&apos;alertes de surveillance
          </h3>
          <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {events.length} événement{events.length > 1 ? "s" : ""}
          </p>
        </div>
        <span className="flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald">
          <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-emerald" />
          Temps réel
        </span>
      </div>
      {events.length === 0 ? (
        <p className="px-4 py-8 text-center text-sm text-muted-foreground">
          Aucune alerte pour le moment. Vos parcelles ne présentent aucun événement
          de surveillance non traité.
        </p>
      ) : (
        <ul className="divide-y divide-border/60">
          {events.map((event) => {
            const severity = resolveSeverityMeta(event.severity)
            const type = resolveMonitoringTypeMeta(event.type)
            const SeverityIcon = severity.icon
            const parcel = parcels.find((p) => p.id === event.parcelId)
            return (
              <li
                key={event.id}
                className="flex gap-3 px-4 py-3 transition-colors hover:bg-secondary/40"
              >
                <SeverityIcon className={cn("mt-0.5 h-4 w-4 shrink-0", severity.tone)} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span
                      className={cn(
                        "font-mono text-[10px] uppercase tracking-[0.18em]",
                        severity.tone,
                      )}
                    >
                      {severity.label}
                    </span>
                    <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                      · {type.label}
                    </span>
                    <span className="ml-auto font-mono text-[10px] text-muted-foreground">
                      {formatDateTime(event.detectedAt)}
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-foreground">{event.description}</p>
                  <div className="mt-1.5 flex items-center justify-between">
                    <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                      {parcel ? `${parcel.name} · ${parcel.regionLabel}` : "Parcelle hors de votre registre"}
                    </span>
                    <span className="font-mono text-[10px] text-emerald">
                      Confiance {Math.round(event.confidenceScore)}%
                    </span>
                  </div>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

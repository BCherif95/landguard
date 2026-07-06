"use client"

import { useMemo } from "react"
import { Hexagon, Hash, Clock, Loader2 } from "lucide-react"
import { AppTopbar } from "@/components/app/app-topbar"
import { KpiCard } from "@/components/app/kpi-card"
import { PremiumEmptyState, ErrorState } from "@/components/app/empty-states"
import { useBlockchainRecords } from "@/lib/hooks/use-blockchain-records"
import { formatDateTime } from "@/lib/api/parcel-display"
import { AppErrorBoundary } from "@/components/app/error-boundary"

// French labels for the entity types the backend actually anchors.
const ENTITY_TYPE_LABEL: Record<string, string> = {
  SUCCESSION_PLAN: "Plan de succession",
  CERTIFICATION: "Certification",
}

function entityTypeLabel(entityType: string): string {
  return ENTITY_TYPE_LABEL[entityType.toUpperCase()] ?? entityType
}

export default function BlockchainPage() {
  const { data: records, isLoading, isError, refetch } = useBlockchainRecords({ limit: 100 })

  const latestAnchor = useMemo(() => {
    if (!records || records.length === 0) return null
    return records.reduce((latest, r) =>
      new Date(r.anchoredAt) > new Date(latest.anchoredAt) ? r : latest,
    )
  }, [records])

  const networks = useMemo(
    () => [...new Set((records ?? []).map((r) => r.network))],
    [records],
  )

  return (
    <AppErrorBoundary name="Blockchain">
      <AppTopbar
        title="Blockchain & Traçabilité"
        subtitle="Registre des ancrages · Empreintes horodatées"
      />
      <div className="flex-1 space-y-4 p-4 sm:p-6">
        {isLoading && (
          <div className="flex items-center justify-center p-16 text-muted-foreground">
            <Loader2 className="mr-2 h-5 w-5 animate-spin" />
            Chargement des ancrages…
          </div>
        )}

        {isError && (
          <ErrorState
            message="Impossible de charger le registre des ancrages. Vérifiez votre connexion puis réessayez."
            retry={() => refetch()}
          />
        )}

        {records && records.length === 0 && (
          <PremiumEmptyState
            icon={Hexagon}
            title="Aucun ancrage pour le moment"
            description="Aucune donnée n'a encore été ancrée sur la chaîne. Les ancrages apparaîtront ici dès qu'un plan de succession validé sera scellé."
          />
        )}

        {records && records.length > 0 && (
          <>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              <KpiCard
                label="Ancrages enregistrés"
                value={records.length.toLocaleString("fr-FR")}
                icon={Hexagon}
                accent="emerald"
              />
              <KpiCard
                label="Dernier ancrage"
                value={latestAnchor ? formatDateTime(latestAnchor.anchoredAt) : "—"}
                icon={Clock}
                accent="info"
              />
              <KpiCard
                label={networks.length > 1 ? "Réseaux" : "Réseau"}
                value={networks.join(", ") || "—"}
                icon={Hash}
                accent="gold"
              />
            </div>

            <div className="overflow-hidden rounded-xl border border-border bg-card/60">
              <div className="border-b border-border/60 px-4 py-3">
                <h2 className="font-display text-sm font-medium text-foreground">
                  Ancrages récents
                </h2>
                <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                  Empreintes SHA-256 telles qu&apos;enregistrées par la plateforme
                </p>
              </div>

              <ol className="relative px-6 py-6">
                <span
                  aria-hidden
                  className="absolute left-[2.4rem] top-6 bottom-6 w-px bg-gradient-to-b from-emerald/40 via-border to-emerald/40"
                />
                {records.map((record) => (
                  <li key={record.id} className="relative flex gap-4 pb-6 last:pb-0">
                    <div className="relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-emerald/30 bg-card text-emerald shadow-md">
                      <Hexagon className="h-4 w-4" />
                    </div>
                    <div className="min-w-0 flex-1 rounded-lg border border-border bg-background/40 p-4">
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-emerald">
                            {entityTypeLabel(record.entityType)}
                          </span>
                          <p className="mt-1 truncate font-mono text-xs text-muted-foreground">
                            Objet : {record.entityId}
                          </p>
                        </div>
                        <span className="shrink-0 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                          {formatDateTime(record.anchoredAt)}
                        </span>
                      </div>
                      <div className="mt-3 flex flex-wrap items-center justify-between gap-2 border-t border-border/60 pt-3">
                        <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                          Réseau : {record.network} · Transaction : {record.transactionId.slice(0, 12)}…
                        </span>
                        <code className="max-w-full truncate rounded bg-secondary px-2 py-0.5 font-mono text-[10px] text-emerald">
                          {record.hash}
                        </code>
                      </div>
                    </div>
                  </li>
                ))}
              </ol>
            </div>
          </>
        )}
      </div>
    </AppErrorBoundary>
  )
}

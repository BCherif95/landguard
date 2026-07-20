"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import {
  ShieldCheck,
  Satellite,
  Banknote,
  Eye,
  Loader2,
  MapPinned,
} from "lucide-react"
import dynamic from "next/dynamic"
import { AppTopbar } from "@/components/app/app-topbar"
import { KpiCard } from "@/components/app/kpi-card"
import { RiskGauge } from "@/components/app/risk-gauge"
import { AlertFeed } from "@/components/app/alert-feed"
import { ParcelSummary } from "@/components/app/parcel-summary"
import { MovementChart } from "@/components/app/movement-chart"
import { ParcelDetailSheet } from "@/components/app/parcel-detail-sheet"
import { PremiumEmptyState, ErrorState } from "@/components/app/empty-states"
import { Button } from "@/components/ui/button"
import { useParcels } from "@/lib/hooks/use-parcels"
import { useMonitoringEvents } from "@/lib/hooks/use-monitoring-events"
import { useMonitoringStream } from "@/lib/hooks/use-monitoring-stream"
import { useParcelUIStore } from "@/lib/store/parcel-ui.store"
import { formatXof } from "@/lib/api/parcel-display"
import { AppErrorBoundary } from "@/components/app/error-boundary"

// Leaflet touches `window`; the map must never render on the server.
const ParcelMap = dynamic(
  () => import("@/components/map/parcel-map").then((m) => m.ParcelMap),
  { ssr: false },
)

export default function VisionLivePage() {
  const { data: parcels, isLoading, isError, refetch } = useParcels({ limit: 200 })
  const { events, isLoading: eventsLoading } = useMonitoringEvents()
  useMonitoringStream()

  const [selectedId, setSelectedId] = useState<string | null>(null)
  const openDetails = useParcelUIStore((s) => s.openDetails)

  useEffect(() => {
    if (!selectedId && parcels && parcels.length > 0) {
      setSelectedId(parcels[0].id)
    }
  }, [parcels, selectedId])

  const selected = useMemo(
    () => parcels?.find((p) => p.id === selectedId) ?? null,
    [parcels, selectedId],
  )

  const unresolvedEvents = useMemo(() => events.filter((e) => !e.resolved), [events])

  const averageTrustScore = useMemo(() => {
    if (!parcels || parcels.length === 0) return null
    return Math.round(parcels.reduce((sum, p) => sum + p.trustScore, 0) / parcels.length)
  }, [parcels])

  const totalEstimatedValue = useMemo(
    () => (parcels ?? []).reduce((sum, p) => sum + (p.estimatedValueXof || 0), 0),
    [parcels],
  )

  // Real 7-day series: monitoring events grouped by day of detection.
  const weeklyEventSeries = useMemo(() => {
    const days: { label: string; value: number }[] = []
    const dayFormat = new Intl.DateTimeFormat("fr-FR", { weekday: "short" })
    for (let i = 6; i >= 0; i--) {
      const day = new Date()
      day.setHours(0, 0, 0, 0)
      day.setDate(day.getDate() - i)
      const next = new Date(day)
      next.setDate(day.getDate() + 1)
      const count = events.filter((e) => {
        const at = new Date(e.detectedAt)
        return at >= day && at < next
      }).length
      days.push({ label: dayFormat.format(day), value: count })
    }
    return days
  }, [events])

  if (isLoading) {
    return (
      <AppErrorBoundary name="Tableau de bord">
        <AppTopbar title="Tableau de bord" subtitle="Votre sécurité foncière en un coup d'œil · Temps réel" />
        <div className="flex flex-1 items-center justify-center p-16 text-muted-foreground">
          <Loader2 className="mr-2 h-5 w-5 animate-spin" />
          Chargement de vos parcelles…
        </div>
      </AppErrorBoundary>
    )
  }

  if (isError) {
    return (
      <AppErrorBoundary name="Tableau de bord">
        <AppTopbar title="Tableau de bord" subtitle="Votre sécurité foncière en un coup d'œil · Temps réel" />
        <div className="p-4 sm:p-6">
          <ErrorState
            message="Impossible de charger vos parcelles. Vérifiez votre connexion puis réessayez."
            retry={() => refetch()}
          />
        </div>
      </AppErrorBoundary>
    )
  }

  if (!parcels || parcels.length === 0) {
    return (
      <AppErrorBoundary name="Tableau de bord">
        <AppTopbar title="Tableau de bord" subtitle="Votre sécurité foncière en un coup d'œil · Temps réel" />
        <div className="p-4 sm:p-6">
          <PremiumEmptyState
            icon={MapPinned}
            title="Aucune parcelle enregistrée"
            description="Ajoutez votre première parcelle pour démarrer la surveillance satellite et recevoir des alertes en temps réel."
            action={
              <Button asChild className="bg-emerald text-primary-foreground hover:bg-emerald/90">
                <Link href="/registry">Enregistrer une parcelle</Link>
              </Button>
            }
          />
        </div>
      </AppErrorBoundary>
    )
  }

  return (
    <AppErrorBoundary name="Tableau de bord">
      <AppTopbar
        title="Tableau de bord"
        subtitle="Votre sécurité foncière en un coup d'œil · Temps réel"
      />

      <div className="flex-1 space-y-4 p-4 sm:p-6">
        {/* KPI ribbon — every figure below is computed from the user's real data. */}
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <KpiCard
            label="Parcelles enregistrées"
            value={parcels.length.toLocaleString("fr-FR")}
            icon={Eye}
            accent="emerald"
          />
          <KpiCard
            label="Alertes non résolues"
            value={eventsLoading ? "…" : unresolvedEvents.length.toLocaleString("fr-FR")}
            icon={Satellite}
            accent={unresolvedEvents.length > 0 ? "danger" : "emerald"}
          />
          <KpiCard
            label="Score de confiance moyen"
            value={averageTrustScore === null ? "—" : `${averageTrustScore}/100`}
            icon={ShieldCheck}
            accent="emerald"
          />
          <KpiCard
            label="Valeur totale estimée"
            value={formatXof(totalEstimatedValue)}
            icon={Banknote}
            accent="gold"
          />
        </div>

        {/* Map + side rail */}
        <div className="grid gap-4 lg:grid-cols-[1fr_360px]">
          <div className="space-y-4">
            <div className="overflow-hidden rounded-xl border border-border bg-card/60">
              <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
                <div className="flex items-center gap-2">
                  <h2 className="font-display text-sm font-medium text-foreground">
                    Carte de vos parcelles
                  </h2>
                  <span className="rounded border border-emerald/30 bg-emerald-soft px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-[0.18em] text-emerald">
                    LIVE
                  </span>
                </div>
                <Button
                  asChild
                  variant="outline"
                  size="sm"
                  className="h-8 border-border bg-background/40 text-xs hover:bg-secondary"
                >
                  <Link href="/map">Ouvrir la carte complète</Link>
                </Button>
              </div>
              <div className="aspect-[16/9]">
                <ParcelMap
                  parcels={parcels}
                  selectedId={selectedId}
                  onSelect={setSelectedId}
                />
              </div>
            </div>

            <MovementChart
              data={weeklyEventSeries}
              title="Événements de surveillance — 7 derniers jours"
              unit="Événements"
            />
          </div>

          {/* Side rail */}
          <div className="space-y-4">
            {selected && (
              <>
                <ParcelSummary parcel={selected} />
                <RiskGauge score={selected.riskScore} />
                <Button
                  onClick={() => openDetails(selected.id)}
                  className="w-full bg-emerald text-xs text-primary-foreground hover:bg-emerald/90"
                >
                  Voir le dossier complet
                </Button>
              </>
            )}
          </div>
        </div>

        <AlertFeed events={unresolvedEvents} parcels={parcels} />
      </div>

      <ParcelDetailSheet />
    </AppErrorBoundary>
  )
}

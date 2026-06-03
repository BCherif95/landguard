"use client"

import { useState } from "react"
import {
  ShieldCheck,
  Satellite,
  Banknote,
  Clock,
  Eye,
  Layers,
  History,
  Maximize2,
} from "lucide-react"
import { AppTopbar } from "@/components/app/app-topbar"
import { SatelliteMap } from "@/components/satellite/satellite-map"
import { KpiCard } from "@/components/app/kpi-card"
import { RiskGauge } from "@/components/app/risk-gauge"
import { AlertFeed } from "@/components/app/alert-feed"
import { ActivityTimeline } from "@/components/app/activity-timeline"
import { ParcelSummary } from "@/components/app/parcel-summary"
import { MovementChart } from "@/components/app/movement-chart"
import { Button } from "@/components/ui/button"
import { parcels, anomalies, activity } from "@/lib/mock-data"

import { AppErrorBoundary } from "@/components/app/error-boundary"

export default function VisionLivePage() {
  const [selectedId, setSelectedId] = useState<string>(parcels[0]?.id || "")
  const selected = parcels.find((p) => p.id === selectedId) || parcels[0]

  if (!selected) {
    return (
      <AppErrorBoundary name="Tableau de bord">
         {/* ... render an empty state or something minimal */}
         <div className="p-8 text-center text-muted-foreground">Aucune donnée disponible.</div>
      </AppErrorBoundary>
    )
  }

  return (
    <AppErrorBoundary name="Tableau de bord">
      <AppTopbar
        title="Vision Live"
        subtitle="Centre de commandement foncier · Temps réel"
      />
      {/* ... rest of the component */}

      <div className="flex-1 space-y-4 p-4 sm:p-6">
        {/* KPI ribbon */}
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <KpiCard
            label="Parcelles surveillées"
            value="2 847"
            delta="+12 cette semaine"
            trend="up"
            icon={Eye}
            accent="emerald"
            sparkline={[10, 12, 11, 14, 18, 22, 24, 28]}
          />
          <KpiCard
            label="Alertes critiques"
            value="3"
            delta="−1 vs hier"
            trend="down"
            icon={Satellite}
            accent="danger"
            sparkline={[5, 4, 6, 4, 5, 3, 4, 3]}
          />
          <KpiCard
            label="Score de confiance"
            value="96%"
            delta="+0,4 pts"
            trend="up"
            icon={ShieldCheck}
            accent="emerald"
            sparkline={[92, 93, 92, 94, 95, 95, 96, 96]}
          />
          <KpiCard
            label="Valeur sous gestion"
            value="2,4 Md FCFA"
            delta="+4,1% MoM"
            trend="up"
            icon={Banknote}
            accent="gold"
            sparkline={[2, 2.1, 2.05, 2.2, 2.25, 2.3, 2.35, 2.4]}
          />
        </div>

        {/* Map + side rail */}
        <div className="grid gap-4 lg:grid-cols-[1fr_360px]">
          <div className="space-y-4">
            {/* Satellite map card */}
            <div className="overflow-hidden rounded-xl border border-border bg-card/60">
              <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
                <div className="flex items-center gap-2">
                  <h2 className="font-display text-sm font-medium text-foreground">
                    Carte satellite — Afrique de l&apos;Ouest
                  </h2>
                  <span className="rounded border border-emerald/30 bg-emerald-soft px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-[0.18em] text-emerald">
                    LIVE
                  </span>
                </div>
                <div className="flex items-center gap-1">
                  <ToolBtn icon={Layers} label="Calques" />
                  <ToolBtn icon={History} label="Historique" />
                  <ToolBtn icon={Clock} label="Time-lapse" />
                  <ToolBtn icon={Maximize2} label="Plein écran" />
                </div>
              </div>
              <SatelliteMap
                parcels={parcels}
                anomalies={anomalies}
                selectedParcelId={selectedId}
                onSelectParcel={setSelectedId}
                intensity="high"
                className="aspect-[16/9] rounded-none border-0"
              />
              {/* Time-lapse scrubber */}
              <div className="border-t border-border/60 p-3">
                <div className="flex items-center justify-between font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                  <span>Time-lapse · 12 derniers mois</span>
                  <span className="text-emerald">12 mars 2026 · 11:42</span>
                </div>
                <div className="mt-2 h-2 rounded-full bg-secondary">
                  <div
                    className="relative h-full rounded-full bg-gradient-to-r from-emerald via-gold to-danger"
                    style={{ width: "78%" }}
                  >
                    <span className="absolute right-0 top-1/2 h-3.5 w-3.5 -translate-y-1/2 translate-x-1/2 rounded-full border-2 border-card bg-foreground shadow-md" />
                  </div>
                </div>
                <div className="mt-2 flex items-center justify-between font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                  <span>Mars 2025</span>
                  <span>Sept. 2025</span>
                  <span>Mars 2026</span>
                </div>
              </div>
            </div>

            <MovementChart />
          </div>

          {/* Side rail */}
          <div className="space-y-4">
            <ParcelSummary parcel={selected} />
            <RiskGauge score={selected.riskScore} />
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant="outline"
                className="h-10 border-border bg-card/60 text-xs hover:bg-secondary"
              >
                Exporter preuve
              </Button>
              <Button className="h-10 bg-emerald text-xs text-primary-foreground hover:bg-emerald/90">
                Verrouiller la parcelle
              </Button>
            </div>
          </div>
        </div>

        {/* Alerts + Activity */}
        <div className="grid gap-4 lg:grid-cols-2">
          <AlertFeed alerts={anomalies} />
          <ActivityTimeline entries={activity} />
        </div>
      </div>
    </AppErrorBoundary>
  )
}

function ToolBtn({
  icon: Icon,
  label,
}: {
  icon: typeof Layers
  label: string
}) {
  return (
    <button
      type="button"
      className="flex h-8 items-center gap-1.5 rounded-md border border-border bg-card px-2 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
      aria-label={label}
    >
      <Icon className="h-3 w-3" />
      <span className="hidden sm:inline">{label}</span>
    </button>
  )
}

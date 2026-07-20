"use client"

import { useEffect, useMemo, useRef, useState } from "react"
import Link from "next/link"
import { motion } from "framer-motion"
import {
  Layers,
  Maximize2,
  Ruler,
  LocateFixed,
  Filter,
  Sparkles,
  Loader2,
  MapPinned,
  History,
} from "lucide-react"
import L from "leaflet"
import { ParcelMap, DEFAULT_LAYERS, type ParcelMapLayers } from "@/components/map/parcel-map"
import { useParcels } from "@/lib/hooks/use-parcels"
import { useMonitoringEvents } from "@/lib/hooks/use-monitoring-events"
import { useSatelliteSnapshots } from "@/lib/hooks/use-satellite-snapshots"
import type { Parcel, ParcelStatus, RiskLevel } from "@/lib/api/parcels"
import { cn } from "@/lib/utils"
import { Slider } from "@/components/ui/slider"
import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { formatXof, formatDateTime } from "@/lib/api/parcel-display"

import { resolveStatusMeta, resolveRiskMeta } from "@/lib/utils/safe-resolvers"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { useParcelUIStore } from "@/lib/store/parcel-ui.store"
import { ParcelDetailSheet } from "@/components/app/parcel-detail-sheet"
import { toast } from "sonner"

const RISK_RANK: Record<RiskLevel, number> = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }

const RISK_FILTER_LABEL: Record<"ALL" | RiskLevel, string> = {
  ALL: "Tous niveaux de risque",
  LOW: "Risque ≥ Faible",
  MEDIUM: "Risque ≥ Modéré",
  HIGH: "Risque ≥ Élevé",
  CRITICAL: "Risque critique uniquement",
}

const STATUS_FILTER_OPTIONS: { value: "ALL" | ParcelStatus; label: string }[] = [
  { value: "ALL", label: "Tous les statuts" },
  { value: "SUBMITTED", label: "Soumises" },
  { value: "UNDER_VERIFICATION", label: "En vérification" },
  { value: "CERTIFIED", label: "Certifiées" },
  { value: "TITLE_ISSUED", label: "Titre émis" },
  { value: "DISPUTED", label: "En litige" },
]

const LAYER_LABELS: { id: keyof ParcelMapLayers; label: string }[] = [
  { id: "satellite", label: "Imagerie satellite" },
  { id: "labels", label: "Noms des lieux" },
  { id: "parcels", label: "Parcelles" },
  { id: "events", label: "Événements de surveillance" },
]

function formatDistance(meters: number): string {
  if (meters >= 1000) return `${(meters / 1000).toFixed(2)} km`
  return `${Math.round(meters)} m`
}

export default function CartePage() {
  const { data: parcels, isLoading, isError } = useParcels({ limit: 200 })
  const { events } = useMonitoringEvents()
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const openDetails = useParcelUIStore((s) => s.openDetails)

  const mapRef = useRef<L.Map | null>(null)

  const [layers, setLayers] = useState<ParcelMapLayers>(DEFAULT_LAYERS)
  const [measureActive, setMeasureActive] = useState(false)
  const [measuredDistance, setMeasuredDistance] = useState<number | null>(null)
  const [riskFilter, setRiskFilter] = useState<"ALL" | RiskLevel>("ALL")
  const [statusFilter, setStatusFilter] = useState<"ALL" | ParcelStatus>("ALL")

  const filteredParcels = useMemo(() => {
    let list = parcels ?? []
    if (riskFilter !== "ALL") {
      list = list.filter((p) => RISK_RANK[p.riskLevel] >= RISK_RANK[riskFilter])
    }
    if (statusFilter !== "ALL") {
      list = list.filter((p) => p.status === statusFilter)
    }
    return list
  }, [parcels, riskFilter, statusFilter])

  const selected: Parcel | null = useMemo(() => {
    if (!filteredParcels.length || !selectedId) return null
    return filteredParcels.find((p) => p.id === selectedId) ?? null
  }, [filteredParcels, selectedId])

  useEffect(() => {
    if ((!selectedId || !filteredParcels.some((p) => p.id === selectedId)) && filteredParcels.length > 0) {
      setSelectedId(filteredParcels[0].id)
    }
  }, [filteredParcels, selectedId])

  const filterSummary = [
    RISK_FILTER_LABEL[riskFilter],
    STATUS_FILTER_OPTIONS.find((o) => o.value === statusFilter)?.label ?? "",
  ]
    .filter(Boolean)
    .join(" · ")

  const handleLocate = () => {
    if (!navigator.geolocation) {
      toast.error("La géolocalisation n'est pas prise en charge par votre navigateur.")
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        mapRef.current?.flyTo([position.coords.latitude, position.coords.longitude], 15, {
          duration: 1.2,
        })
      },
      () => toast.error("Position introuvable. Autorisez la géolocalisation puis réessayez."),
    )
  }

  const handleFitParcels = () => {
    const map = mapRef.current
    if (!map || filteredParcels.length === 0) return
    const points = filteredParcels
      .filter((p) => p.centroid)
      .map((p) => [p.centroid.latitude, p.centroid.longitude] as [number, number])
    if (points.length === 0) return
    map.fitBounds(L.latLngBounds(points), { padding: [60, 60] })
  }

  return (
    <AppErrorBoundary name="Carte Interactive">
      <div className="relative h-svh w-full overflow-hidden bg-navy">
        <div className="absolute inset-0">
          <ParcelMap
            parcels={filteredParcels}
            selectedId={selectedId}
            onSelect={setSelectedId}
            layers={layers}
            events={events}
            measureActive={measureActive}
            onMeasure={setMeasuredDistance}
            onMapReady={(map) => {
              mapRef.current = map
            }}
          />
        </div>

        <ParcelDetailSheet />

        {/* Top toolbar */}
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="absolute left-4 right-4 top-4 z-20 flex flex-wrap items-center gap-2"
        >
          <div className="flex items-center gap-1 rounded-lg border border-border bg-card/95 p-1 backdrop-blur-xl shadow-card-lg">
            <button
              type="button"
              title="Mesurer une distance (deux clics sur la carte)"
              aria-pressed={measureActive}
              onClick={() => setMeasureActive((v) => !v)}
              className={cn(
                "flex h-9 items-center gap-1.5 rounded px-2.5 text-xs transition-colors",
                measureActive
                  ? "bg-emerald text-primary-foreground"
                  : "text-muted-foreground hover:bg-secondary hover:text-foreground",
              )}
            >
              <Ruler className="h-4 w-4" />
              Mesurer
            </button>
            <button
              type="button"
              title="Recentrer la carte sur votre position"
              onClick={handleLocate}
              className="flex h-9 items-center gap-1.5 rounded px-2.5 text-xs text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
            >
              <LocateFixed className="h-4 w-4" />
              Localiser
            </button>
          </div>

          {measureActive && (
            <div className="rounded-lg border border-emerald/40 bg-emerald-soft px-3 py-2 text-xs text-emerald shadow-card">
              {measuredDistance !== null
                ? `Distance : ${formatDistance(measuredDistance)}`
                : "Cliquez deux points sur la carte pour mesurer."}
            </div>
          )}

          {/* Real filters */}
          <Popover>
            <PopoverTrigger asChild>
              <button
                type="button"
                className="flex items-center gap-2 rounded-lg border border-border bg-card/95 px-3 py-2 text-xs text-muted-foreground backdrop-blur-xl shadow-card-lg transition-colors hover:bg-secondary hover:text-foreground"
              >
                <Filter className="h-3.5 w-3.5" />
                <span>Filtres : {filterSummary}</span>
              </button>
            </PopoverTrigger>
            <PopoverContent align="start" className="w-72 space-y-3">
              <div className="space-y-1.5">
                <p className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                  Niveau de risque minimal
                </p>
                <Select value={riskFilter} onValueChange={(v) => setRiskFilter(v as "ALL" | RiskLevel)}>
                  <SelectTrigger aria-label="Filtrer par niveau de risque">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {(Object.keys(RISK_FILTER_LABEL) as Array<"ALL" | RiskLevel>).map((value) => (
                      <SelectItem key={value} value={value}>
                        {RISK_FILTER_LABEL[value]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <p className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                  Statut des parcelles
                </p>
                <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as "ALL" | ParcelStatus)}>
                  <SelectTrigger aria-label="Filtrer par statut">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {STATUS_FILTER_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </PopoverContent>
          </Popover>

          <div className="ml-auto flex items-center gap-2">
            <button
              type="button"
              onClick={handleFitParcels}
              className="flex h-9 items-center gap-1.5 rounded-lg border border-border bg-card/95 px-3 text-xs text-muted-foreground backdrop-blur-xl shadow-card-lg transition-colors hover:bg-secondary hover:text-foreground"
              title="Recadrer la vue sur les parcelles affichées"
            >
              <Maximize2 className="h-4 w-4" />
              Recadrer
            </button>
          </div>
        </motion.div>

        {/* Layers panel — controlled, actually shows/hides map layers */}
        <motion.div
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          className="absolute left-4 top-20 z-20 w-64 rounded-xl border border-border bg-card/95 p-3 backdrop-blur-xl shadow-card-lg"
        >
          <div className="flex items-center gap-2 px-1 pb-2 font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
            <Layers className="h-3 w-3" />
            Couches
          </div>
          <ul className="space-y-1">
            {LAYER_LABELS.map((layer) => (
              <li
                key={layer.id}
                className="flex items-center justify-between rounded px-2 py-1.5 text-xs text-foreground hover:bg-secondary"
              >
                <label className="flex w-full cursor-pointer items-center gap-2">
                  <input
                    type="checkbox"
                    checked={layers[layer.id]}
                    onChange={(e) =>
                      setLayers((current) => ({ ...current, [layer.id]: e.target.checked }))
                    }
                    className="h-3 w-3 rounded border-input bg-transparent accent-emerald"
                  />
                  {layer.label}
                </label>
              </li>
            ))}
          </ul>
        </motion.div>

        {/* Selection panel */}
        <motion.aside
          initial={{ x: 20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          className="absolute right-4 top-20 z-20 w-80 rounded-xl border border-border bg-card/95 p-4 backdrop-blur-xl shadow-card-lg"
        >
          {isLoading && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
              Chargement des parcelles…
            </div>
          )}
          {isError && (
            <div className="text-xs text-danger">
              Erreur de chargement des parcelles. Vérifiez votre connexion puis rechargez la page.
            </div>
          )}
          {!isLoading && !isError && filteredParcels.length === 0 && (parcels?.length ?? 0) > 0 && (
            <p className="text-xs text-muted-foreground">
              Aucune parcelle ne correspond aux filtres sélectionnés.
            </p>
          )}
          {selected && (
            <div className="space-y-3 text-foreground">
              <div className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
                {selected.reference}
              </div>
              <div>
                <div className="font-display text-lg font-medium">{selected.name}</div>
                <div className="text-xs text-muted-foreground">{selected.regionLabel}</div>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <Stat label="Surface" value={`${selected.areaHectares} ha`} />
                <Stat label="Valeur" value={formatXof(selected.estimatedValueXof)} />
                <Stat label="Confiance" value={`${selected.trustScore} / 100`} />
                <Stat label="Risque" value={`${selected.riskScore} / 100`} />
              </div>

              <div className="flex items-center gap-2">
                <span
                  className={cn(
                    "rounded px-2 py-1 font-mono text-[10px] uppercase tracking-[0.18em]",
                    resolveRiskMeta(selected.riskLevel).tone,
                  )}
                >
                  Risque {resolveRiskMeta(selected.riskLevel).label}
                </span>
                <span
                  className={cn(
                    "rounded border border-border px-2 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground",
                    resolveStatusMeta(selected.status).border,
                  )}
                >
                  {resolveStatusMeta(selected.status).label}
                </span>
              </div>

              {/* Faithful synthesis: states only what the API actually returns. */}
              <div className="rounded-lg border border-border bg-secondary p-3 text-xs">
                <div className="mb-1 flex items-center gap-1.5 text-emerald">
                  <Sparkles className="h-3 w-3" />
                  <span className="font-medium">Synthèse</span>
                </div>
                <p className="text-muted-foreground">
                  Statut : {resolveStatusMeta(selected.status).label}. Score de confiance :{" "}
                  {selected.trustScore}/100. Score de risque : {selected.riskScore}/100.
                </p>
              </div>

              <div className="flex flex-col gap-2">
                <div className="text-[10px] text-muted-foreground/70">
                  Propriétaire : <span className="text-muted-foreground">{selected.ownerLabel}</span>
                </div>
                <button
                  type="button"
                  onClick={() => openDetails(selected.id)}
                  className="mt-2 w-full rounded-lg bg-primary py-2 text-xs font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  Voir dossier complet →
                </button>
              </div>
            </div>
          )}
        </motion.aside>

        {/* Empty state — no parcels at all */}
        {!isLoading && !isError && (parcels?.length ?? 0) === 0 && (
          <div className="absolute inset-0 z-30 flex items-center justify-center bg-background/70 backdrop-blur-sm">
            <div className="mx-4 max-w-md rounded-2xl border border-border bg-card/95 p-8 text-center">
              <MapPinned className="mx-auto h-10 w-10 text-emerald" />
              <h2 className="mt-4 font-display text-xl text-foreground">
                Aucune parcelle enregistrée pour le moment
              </h2>
              <p className="mt-2 text-sm text-muted-foreground">
                Ajoutez votre première parcelle pour commencer la surveillance satellite.
              </p>
              <Button asChild className="mt-6 bg-emerald text-primary-foreground hover:bg-emerald/90">
                <Link href="/registry">Enregistrer une parcelle</Link>
              </Button>
            </div>
          </div>
        )}

        {/* Bottom bar: count + snapshot timeline */}
        <div className="absolute bottom-4 left-4 right-4 z-20 flex flex-wrap items-end gap-3">
          <div className="rounded-lg border border-border bg-card/95 px-3 py-2 font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground backdrop-blur-xl shadow-card-lg">
            {filteredParcels.length} parcelle{filteredParcels.length > 1 ? "s" : ""}
          </div>
          {selected && <SnapshotTimeline parcelId={selected.id} />}
        </div>
      </div>
    </AppErrorBoundary>
  )
}

/**
 * Time slider over the real satellite snapshots stored for the parcel.
 * When history is sparse, it says so honestly instead of faking continuity.
 */
function SnapshotTimeline({ parcelId }: { parcelId: string }) {
  const { data: snapshots, isLoading } = useSatelliteSnapshots(parcelId)
  const [index, setIndex] = useState(0)

  useEffect(() => {
    setIndex(snapshots && snapshots.length > 0 ? snapshots.length - 1 : 0)
  }, [parcelId, snapshots?.length])

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 rounded-lg border border-border bg-card/95 px-3 py-2 text-[10px] text-muted-foreground backdrop-blur-xl shadow-card-lg">
        <Loader2 className="h-3 w-3 animate-spin" />
        Chargement de l&apos;historique satellite…
      </div>
    )
  }

  if (!snapshots || snapshots.length === 0) {
    return (
      <div className="flex items-center gap-2 rounded-lg border border-border bg-card/95 px-3 py-2 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground backdrop-blur-xl shadow-card-lg">
        <History className="h-3 w-3" />
        Aucun cliché historique pour cette parcelle
      </div>
    )
  }

  const current = snapshots[Math.min(index, snapshots.length - 1)]

  return (
    <div className="min-w-[280px] flex-1 rounded-xl border border-border bg-card/95 p-3 backdrop-blur-xl shadow-card-lg sm:max-w-xl">
      <div className="flex items-center justify-between font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
        <span className="flex items-center gap-1.5">
          <History className="h-3 w-3" />
          Historique satellite
        </span>
        <span className="text-emerald">{formatDateTime(current.capturedAt)}</span>
      </div>

      {snapshots.length === 1 ? (
        <p className="mt-2 text-[10px] text-muted-foreground">
          Historique limité — 1 seul cliché disponible.
        </p>
      ) : (
        <>
          <Slider
            className="mt-3"
            min={0}
            max={snapshots.length - 1}
            step={1}
            value={[Math.min(index, snapshots.length - 1)]}
            onValueChange={([value]) => setIndex(value)}
            aria-label="Sélectionner un cliché historique"
          />
          <div className="mt-2 flex items-center justify-between font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground/70">
            <span>{formatDateTime(snapshots[0].capturedAt)}</span>
            <span>{formatDateTime(snapshots[snapshots.length - 1].capturedAt)}</span>
          </div>
          {snapshots.length < 5 && (
            <p className="mt-1 text-[10px] text-muted-foreground">
              Historique limité — {snapshots.length} clichés disponibles.
            </p>
          )}
        </>
      )}

      <div className="mt-2 flex items-center gap-3 text-[10px] text-muted-foreground">
        <span>Score de mouvement : {current.movementScore}</span>
        <span>Score d&apos;anomalie : {current.anomalyScore}</span>
        {current.imageUrl && (
          <a
            href={current.imageUrl}
            target="_blank"
            rel="noreferrer"
            className="ml-auto text-emerald underline-offset-2 hover:underline"
          >
            Voir le cliché
          </a>
        )}
      </div>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-border bg-secondary px-2.5 py-1.5">
      <div className="font-mono text-[9px] uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </div>
      <div className="mt-0.5 text-sm font-medium">{value}</div>
    </div>
  )
}

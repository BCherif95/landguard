"use client"

import { useEffect, useMemo, useRef, useState } from "react"
import { motion } from "framer-motion"
import {
  Layers,
  Maximize2,
  Compass,
  Ruler,
  Pencil,
  Search,
  Filter,
  Download,
  Sparkles,
  ZoomIn,
  ZoomOut,
  Loader2,
} from "lucide-react"
import { ParcelMap } from "@/components/map/parcel-map"
import { useParcels } from "@/lib/hooks/use-parcels"
import type { Parcel, ParcelStatus, RiskLevel } from "@/lib/api/parcels"
import { cn } from "@/lib/utils"

import { resolveStatusMeta, resolveRiskMeta } from "@/lib/utils/safe-resolvers"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { useParcelUIStore } from "@/lib/store/parcel-ui.store"
import { ParcelDetailSheet } from "@/components/app/parcel-detail-sheet"

const tools = [
  { icon: Compass, label: "Boussole" },
  { icon: Ruler, label: "Mesurer" },
  { icon: Pencil, label: "Dessiner" },
  { icon: Search, label: "Localiser" },
]

const layerControls = [
  { id: "satellite", label: "Imagerie Sentinel-2", on: true },
  { id: "parcels", label: "Parcelles certifiées", on: true },
  { id: "anomalies", label: "Anomalies IA", on: true },
  { id: "heatmap", label: "Heatmap de risque", on: false },
  { id: "cadastre", label: "Cadastre officiel 2024", on: true },
  { id: "hydro", label: "Réseau hydrographique", on: true },
]

function formatXof(amount: number) {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "XOF",
    maximumFractionDigits: 0,
  }).format(amount)
}

export default function CartePage() {
  const { data: parcels, isLoading, isError } = useParcels({ limit: 200 })
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const openDetails = useParcelUIStore((s) => s.openDetails)

  const selected: Parcel | null = useMemo(() => {
    if (!parcels || !selectedId) return null
    return parcels.find((p) => p.id === selectedId) ?? null
  }, [parcels, selectedId])

  // ... (effects stay same)
  useEffect(() => {
    if (!selectedId && parcels && parcels.length > 0) {
      setSelectedId(parcels[0].id)
    }
  }, [parcels, selectedId])

  return (
    <AppErrorBoundary name="Carte Interactive">
      <div className="relative -m-4 h-[calc(100vh-4rem)] overflow-hidden bg-[#070d18] sm:-m-6">
        <div className="absolute inset-0">
          <ParcelMap
            parcels={parcels ?? []}
            selectedId={selectedId}
            onSelect={setSelectedId}
          />
        </div>

        <ParcelDetailSheet />

        {/* Top toolbar */}
        {/* ... (toolbar remains same) */}
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="absolute left-4 right-4 top-4 z-20 flex flex-wrap items-center gap-2"
        >
          <div className="flex items-center gap-1 rounded-lg border border-white/10 bg-black/60 p-1 backdrop-blur-xl">
            {tools.map((tool) => (
              <button
                key={tool.label}
                type="button"
                title={tool.label}
                className="flex h-9 w-9 items-center justify-center rounded text-white/70 transition-colors hover:bg-white/10 hover:text-white"
              >
                <tool.icon className="h-4 w-4" />
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2 rounded-lg border border-white/10 bg-black/60 px-3 py-2 text-xs text-white/70 backdrop-blur-xl">
            <Filter className="h-3.5 w-3.5" />
            <span>Filtres : Toutes parcelles · Risque ≥ Faible</span>
          </div>

          <div className="ml-auto flex items-center gap-2">
            <button
              type="button"
              className="flex items-center gap-1.5 rounded-lg border border-white/10 bg-black/60 px-3 py-2 text-xs text-white/70 backdrop-blur-xl transition-colors hover:bg-black/80 hover:text-white"
            >
              <Download className="h-3.5 w-3.5" />
              Exporter
            </button>
            <button
              type="button"
              className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/10 bg-black/60 text-white/70 backdrop-blur-xl transition-colors hover:bg-black/80 hover:text-white"
              title="Recalibrer la vue"
            >
              <Maximize2 className="h-4 w-4" />
            </button>
          </div>
        </motion.div>

        {/* Layers panel */}
        {/* ... (layers remain same) */}
        <motion.div
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          className="absolute left-4 top-20 z-20 w-64 rounded-xl border border-white/10 bg-black/70 p-3 backdrop-blur-xl"
        >
          <div className="flex items-center gap-2 px-1 pb-2 font-mono text-[10px] uppercase tracking-[0.22em] text-white/60">
            <Layers className="h-3 w-3" />
            Couches
          </div>
          <ul className="space-y-1">
            {layerControls.map((l) => (
              <li
                key={l.id}
                className="flex items-center justify-between rounded px-2 py-1.5 text-xs text-white/80 hover:bg-white/5"
              >
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    defaultChecked={l.on}
                    className="h-3 w-3 rounded border-white/20 bg-transparent accent-emerald"
                  />
                  {l.label}
                </label>
              </li>
            ))}
          </ul>
        </motion.div>

        {/* Selection panel */}
        <motion.aside
          initial={{ x: 20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          className="absolute right-4 top-20 z-20 w-80 rounded-xl border border-white/10 bg-black/70 p-4 backdrop-blur-xl"
        >
          {isLoading && (
            <div className="flex items-center gap-2 text-xs text-white/60">
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
              Chargement des parcelles…
            </div>
          )}
          {isError && (
            <div className="text-xs text-danger">Erreur de chargement des parcelles.</div>
          )}
          {selected && (
            <div className="space-y-3 text-white">
              <div className="font-mono text-[10px] uppercase tracking-[0.22em] text-white/50">
                {selected.reference}
              </div>
              <div>
                <div className="font-display text-lg font-medium">{selected.name}</div>
                <div className="text-xs text-white/60">{selected.regionLabel}</div>
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
                    "rounded border border-white/10 px-2 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-white/70",
                    resolveStatusMeta(selected.status).border,
                  )}
                >
                  {resolveStatusMeta(selected.status).label}
                </span>
              </div>

              <div className="rounded-lg border border-white/10 bg-white/5 p-3 text-xs">
                <div className="mb-1 flex items-center gap-1.5 text-emerald">
                  <Sparkles className="h-3 w-3" />
                  <span className="font-medium">Synthèse IA</span>
                </div>
                <p className="text-white/70">
                  Parcelle {resolveStatusMeta(selected.status).label.toLowerCase()} avec un score de
                  confiance de {selected.trustScore}/100. Surface vérifiée par recoupement
                  cadastral et vue Sentinel-2.
                </p>
              </div>

              <div className="flex flex-col gap-2">
                <div className="text-[10px] text-white/40">
                  Propriétaire : <span className="text-white/70">{selected.ownerLabel}</span>
                </div>
                <button
                  type="button"
                  onClick={() => openDetails(selected.id)}
                  className="mt-2 w-full rounded-lg bg-white/10 py-2 text-xs font-medium text-white transition-colors hover:bg-white/20"
                >
                  Voir dossier complet →
                </button>
              </div>
            </div>
          )}
        </motion.aside>

        {/* Bottom-left count */}
        <div className="absolute bottom-4 left-4 z-20 flex items-center gap-2">
          <div className="rounded-lg border border-white/10 bg-black/70 px-3 py-2 font-mono text-[10px] uppercase tracking-[0.22em] text-white/70 backdrop-blur-xl">
            {parcels?.length ?? 0} parcelle{(parcels?.length ?? 0) > 1 ? "s" : ""}
          </div>
        </div>
      </div>
    </AppErrorBoundary>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-white/10 bg-white/5 px-2.5 py-1.5">
      <div className="font-mono text-[9px] uppercase tracking-[0.18em] text-white/50">
        {label}
      </div>
      <div className="mt-0.5 text-sm font-medium">{value}</div>
    </div>
  )
}

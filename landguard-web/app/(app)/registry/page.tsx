"use client"

import { useMemo, useState } from "react"
import {
  FolderLock,
  Plus,
  Filter,
  QrCode,
  ScanLine,
  FileCheck2,
  ShieldCheck,
  Loader2,
} from "lucide-react"
import { AppTopbar } from "@/components/app/app-topbar"
import { Button } from "@/components/ui/button"
import { StatusPill, RiskBadge } from "@/components/app/pills"
import { KpiCard } from "@/components/app/kpi-card"
import { useParcels } from "@/lib/hooks/use-parcels"
import { formatXof } from "@/lib/api/parcel-display"
import type { ParcelStatus } from "@/lib/api/parcels"
import { ParcelDetailSheet } from "@/components/app/parcel-detail-sheet"
import { useParcelUIStore } from "@/lib/store/parcel-ui.store"
import { useRouter } from "next/navigation"
import { normalizePolygon } from "@/lib/utils/geometry-guards"
import { NoResultsState, ErrorState } from "@/components/app/empty-states"
import { Skeleton } from "@/components/ui/skeleton"

const STATUS_FILTERS: { value: ParcelStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "Tous les statuts" },
  { value: "DRAFT", label: "Brouillon" },
  { value: "SUBMITTED", label: "Soumis" },
  { value: "UNDER_VERIFICATION", label: "En cours" },
  { value: "CERTIFIED", label: "Certifiées" },
  { value: "TITLE_ISSUED", label: "TF Émis" },
  { value: "DISPUTED", label: "Litige" },
]

export default function RegistrePage() {
  const { data: parcels, isLoading, isError } = useParcels({ limit: 200 })
  const [statusFilter, setStatusFilter] = useState<ParcelStatus | "ALL">("ALL")
  const router = useRouter()
  const openDetails = useParcelUIStore((s) => s.openDetails)

  const filtered = useMemo(() => {
    if (!parcels) return []
    if (statusFilter === "ALL") return parcels
    return parcels.filter((p) => p.status === statusFilter)
  }, [parcels, statusFilter])

  const stats = useMemo(() => {
    const list = parcels ?? []
    const certified = list.filter((p) => ["CERTIFIED", "TITLE_ISSUED"].includes(p.status)).length
    const disputed = list.filter((p) => p.status === "DISPUTED").length
    return {
      total: list.length,
      certifiedRatio: list.length === 0 ? 0 : Math.round((certified / list.length) * 100),
      disputed,
    }
  }, [parcels])

  return (
    <>
      <AppTopbar
        title="Registre foncier numérique"
        subtitle="Titres · OCR IA · Certification blockchain"
      />
      <div className="flex-1 space-y-4 p-4 sm:p-6">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <KpiCard
            label="Parcelles enregistrées"
            value={stats.total.toLocaleString("fr-FR")}
            delta="depuis le registre"
            trend="up"
            icon={FolderLock}
            accent="emerald"
          />
          <KpiCard
            label="Taux de certification"
            value={`${stats.certifiedRatio}%`}
            delta="OCR IA + manuelle"
            trend="up"
            icon={FileCheck2}
            accent="emerald"
          />
          <KpiCard
            label="Litiges actifs"
            value={String(stats.disputed)}
            delta="à arbitrer"
            trend="flat"
            icon={ShieldCheck}
            accent="danger"
          />
          <KpiCard
            label="QR scans"
            value="412"
            delta="vérifications terrain"
            trend="up"
            icon={QrCode}
            accent="gold"
          />
        </div>

        {/* Toolbar */}
        <div className="flex flex-wrap items-center gap-2 rounded-2xl border border-border bg-card p-2.5 shadow-card">
          <div className="flex flex-wrap items-center gap-1 rounded-xl border border-border bg-secondary p-1 text-[10px] uppercase font-bold tracking-wider">
            {STATUS_FILTERS.map((f) => (
              <button
                key={f.value}
                type="button"
                onClick={() => setStatusFilter(f.value)}
                className={
                  statusFilter === f.value
                    ? "rounded-lg px-2.5 py-1.5 bg-primary text-primary-foreground shadow-sm"
                    : "rounded-lg px-2.5 py-1.5 text-muted-foreground hover:bg-card hover:text-foreground transition-colors"
                }
              >
                {f.label}
              </button>
            ))}
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="h-9 gap-2 text-muted-foreground hover:bg-secondary hover:text-foreground"
          >
            <Filter className="h-4 w-4" />
            Filtres avancés
          </Button>
          <Button
            variant="ghost"
            size="sm"
            className="h-9 gap-2 text-muted-foreground hover:bg-secondary hover:text-foreground"
          >
            <ScanLine className="h-4 w-4" />
            Toutes régions
          </Button>
          <div className="ml-auto flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              className="h-9 gap-2"
            >
              <QrCode className="h-4 w-4" />
              Scanner QR
            </Button>
            <Button
              size="sm"
              onClick={() => router.push("/registry/new")}
              className="h-9 gap-2 shadow-sm"
            >
              <Plus className="h-4 w-4" />
              Enregistrer un terrain
            </Button>
          </div>
        </div>

        <ParcelDetailSheet />

        {isLoading && (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {[...Array(6)].map((_, i) => (
              <Skeleton key={i} className="h-64 rounded-xl" />
            ))}
          </div>
        )}
        {isError && (
          <ErrorState retry={() => window.location.reload()} />
        )}
        {parcels && filtered.length === 0 && !isLoading && (
          <NoResultsState />
        )}

        {filtered.length > 0 && (
          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {filtered.map((p) => {
              const norm = normalizePolygon(p.geometry?.coordinates?.[0])

              return (
                <article
                  key={p.id}
                  className="group relative overflow-hidden rounded-2xl border border-border bg-card shadow-card transition-all duration-300 hover:-translate-y-0.5 hover:border-emerald/40 hover:shadow-card-lg"
                >
                  <div
                    className="relative h-32 w-full"
                    style={{
                      background: `
                        radial-gradient(ellipse 60% 40% at 35% 45%, color-mix(in oklab, var(--emerald) 18%, transparent) 0%, transparent 60%),
                        radial-gradient(ellipse 50% 30% at 70% 60%, color-mix(in oklab, var(--gold) 14%, transparent) 0%, transparent 55%),
                        linear-gradient(160deg, #07101a 0%, #0c1b2a 50%, #081522 100%)
                      `,
                    }}
                  >
                    <svg viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 h-full w-full opacity-60">
                      {norm ? (
                        <polygon
                          points={norm}
                          fill="color-mix(in oklab, var(--emerald) 20%, transparent)"
                          stroke="var(--emerald)"
                          strokeWidth="0.4"
                          vectorEffect="non-scaling-stroke"
                        />
                      ) : (
                        <rect x="25" y="25" width="50" height="50" rx="4" fill="white" fillOpacity="0.05" stroke="white" strokeOpacity="0.1" strokeDasharray="2 2" />
                      )}
                    </svg>
                    <div className="absolute right-2 top-2">
                      <StatusPill status={p.status} />
                    </div>
                  </div>

                  <div className="p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <h3 className="truncate font-display text-base font-medium tracking-tight text-foreground group-hover:text-emerald transition-colors">
                          {p.name}
                        </h3>
                        <p className="truncate font-mono text-[11px] uppercase tracking-[0.16em] text-muted-foreground">
                          {p.reference}
                        </p>
                      </div>
                      <RiskBadge risk={p.riskLevel} score={p.riskScore} />
                    </div>

                    <dl className="mt-3 grid grid-cols-2 gap-2 text-xs">
                      <Row label="Propriétaire" value={p.ownerLabel} />
                      <Row label="Région" value={p.regionLabel} />
                      <Row label="Superficie" value={`${p.areaHectares} ha`} />
                      <Row
                        label="Valeur"
                        value={formatXof(p.estimatedValueXof)}
                        emphasis
                      />
                    </dl>

                    <div className="mt-4 flex items-center justify-between border-t border-border/60 pt-3">
                      <span className="font-mono text-[10px] text-muted-foreground uppercase tracking-wider font-bold">
                        Confiance {p.trustScore}/100
                      </span>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => openDetails(p.id)}
                        className="h-8 px-4 text-[10px] text-emerald bg-emerald/5 hover:bg-emerald/10 hover:text-emerald rounded-lg font-bold uppercase tracking-widest transition-all"
                      >
                        Ouvrir →
                      </Button>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </div>
    </>
  )
}

function Row({
  label,
  value,
  emphasis,
}: {
  label: string
  value: string
  emphasis?: boolean
}) {
  return (
    <div>
      <dt className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </dt>
      <dd
        className={
          emphasis
            ? "mt-0.5 truncate text-emerald font-bold"
            : "mt-0.5 truncate text-foreground"
        }
      >
        {value}
      </dd>
    </div>
  )
}

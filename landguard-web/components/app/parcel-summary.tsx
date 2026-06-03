import {
  MapPin,
  ShieldCheck,
  AlertTriangle,
  Hash,
  Calendar,
  Banknote,
  FileText,
} from "lucide-react"
import type { Parcel } from "@/lib/types"
import { formatXof } from "@/lib/mock-data"
import { StatusPill, RiskBadge } from "./pills"

interface ParcelSummaryProps {
  parcel: Parcel
}

export function ParcelSummary({ parcel }: ParcelSummaryProps) {
  return (
    <div className="rounded-xl border border-border bg-card/60">
      <div className="flex items-start justify-between gap-3 border-b border-border/60 p-4">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <h3 className="font-display text-base font-medium tracking-tight text-foreground">
              {parcel.name}
            </h3>
            <StatusPill status={parcel.status} />
          </div>
          <p className="mt-1 flex items-center gap-1.5 font-mono text-[11px] uppercase tracking-[0.16em] text-muted-foreground">
            <MapPin className="h-3 w-3" />
            {parcel.region}
          </p>
        </div>
        <RiskBadge risk={parcel.risk} score={parcel.riskScore} />
      </div>

      <dl className="grid grid-cols-2 divide-x divide-border/60 border-b border-border/60">
        <Stat
          icon={Banknote}
          label="Valeur estimée"
          value={formatXof(parcel.estimatedValue)}
          accent
        />
        <Stat
          icon={ShieldCheck}
          label="Score confiance"
          value={`${parcel.trustScore}/100`}
        />
      </dl>

      <dl className="space-y-2 p-4 text-sm">
        <Row icon={Hash} label="Référence Cadastrale" value={parcel.reference} mono />
        {parcel.titleNumber && (
          <Row icon={FileText} label="Numéro de Titre Foncier" value={parcel.titleNumber} mono accent />
        )}
        <Row icon={Hash} label="Empreinte Blockchain" value={parcel.blockchainHash} mono />
        <Row icon={Calendar} label="Dernière vérification" value={parcel.lastVerifiedAt} />
        <Row icon={MapPin} label="Superficie" value={`${parcel.area} ha`} />
        <Row
          icon={AlertTriangle}
          label="Coordonnées GPS"
          value={parcel.coordinates ? `${parcel.coordinates.lat.toFixed(4)}°, ${parcel.coordinates.lng.toFixed(4)}°` : "Données GPS indisponibles"}
          mono
        />
      </dl>
    </div>
  )
}

function Stat({
  icon: Icon,
  label,
  value,
  accent,
}: {
  icon: typeof MapPin
  label: string
  value: string
  accent?: boolean
}) {
  return (
    <div className="p-4">
      <div className="flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
        <Icon className="h-3 w-3" />
        {label}
      </div>
      <div
        className={`mt-1.5 font-display text-lg font-medium tracking-tight ${
          accent ? "text-emerald" : "text-foreground"
        }`}
      >
        {value}
      </div>
    </div>
  )
}

function Row({
  icon: Icon,
  label,
  value,
  mono,
  accent,
}: {
  icon: typeof MapPin
  label: string
  value: string
  mono?: boolean
  accent?: boolean
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="flex items-center gap-2 text-xs text-muted-foreground">
        <Icon className="h-3 w-3" />
        {label}
      </span>
      <span className={`truncate ${accent ? "text-emerald font-bold" : "text-foreground"} ${mono ? "font-mono text-xs" : "text-sm"}`}>
        {value}
      </span>
    </div>
  )
}

import type { ParcelStatus, RiskLevel } from "@/lib/api/parcels"
import {
  ShieldCheck,
  AlertTriangle,
  Clock,
  FileCheck2,
  FileSignature,
  CheckCircle2,
  Ban,
  Archive,
  AlertOctagon,
  Info,
  Hash,
  ArrowLeftRight,
  Edit3,
  Coins,
  ScanLine,
  FileText,
  Link2,
  ClipboardCheck,
  type LucideIcon
} from "lucide-react"

// Light-SaaS badge tones (cahier des charges §5.1) — soft tinted fills,
// legible saturated text and hairline borders on white/technical-gray surfaces.
// Emerald = conformité, Rouge = anomalie, Bleu marine = confiance, Gris = neutre.

export interface StatusMeta {
  label: string
  tone: string
  bg: string
  border: string
  text: string
  icon: LucideIcon
}

const DEFAULT_STATUS_META: StatusMeta = {
  label: "Inconnu",
  tone: "border-slate-200 bg-slate-50 text-slate-500",
  bg: "bg-slate-50",
  border: "border-slate-200",
  text: "text-slate-500",
  icon: Info
}

const STATUS_METADATA: Record<ParcelStatus, StatusMeta> = {
  DRAFT: {
    label: "Brouillon",
    tone: "border-slate-200 bg-slate-50 text-slate-600",
    bg: "bg-slate-50", border: "border-slate-200", text: "text-slate-600",
    icon: FileSignature
  },
  SUBMITTED: {
    label: "Soumis",
    tone: "border-blue-200 bg-blue-50 text-blue-700",
    bg: "bg-blue-50", border: "border-blue-200", text: "text-blue-700",
    icon: Clock
  },
  UNDER_SURVEY: {
    label: "Arpentage",
    tone: "border-amber-200 bg-amber-50 text-amber-700",
    bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-700",
    icon: ScanLine
  },
  UNDER_VERIFICATION: {
    label: "Vérification",
    tone: "border-sky-200 bg-sky-50 text-sky-700",
    bg: "bg-sky-50", border: "border-sky-200", text: "text-sky-700",
    icon: ShieldCheck
  },
  UNDER_NOTARY_REVIEW: {
    label: "Notariat",
    tone: "border-violet-200 bg-violet-50 text-violet-700",
    bg: "bg-violet-50", border: "border-violet-200", text: "text-violet-700",
    icon: FileSignature
  },
  UNDER_ADMIN_REVIEW: {
    label: "Examen Admin",
    tone: "border-amber-200 bg-amber-50 text-amber-800",
    bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-800",
    icon: Info
  },
  CERTIFIED: {
    label: "Certifié",
    tone: "border-emerald-200 bg-emerald-50 text-emerald-700",
    bg: "bg-emerald-50", border: "border-emerald-200", text: "text-emerald-700",
    icon: CheckCircle2
  },
  TITLE_ISSUED: {
    label: "TF Émis",
    tone: "border-emerald-300 bg-emerald-100 text-emerald-800",
    bg: "bg-emerald-100", border: "border-emerald-300", text: "text-emerald-800",
    icon: FileCheck2
  },
  DISPUTED: {
    label: "Litige",
    tone: "border-red-200 bg-red-50 text-red-700",
    bg: "bg-red-50", border: "border-red-200", text: "text-red-700",
    icon: AlertTriangle
  },
  REJECTED: {
    label: "Rejeté",
    tone: "border-slate-200 bg-slate-100 text-slate-500",
    bg: "bg-slate-100", border: "border-slate-200", text: "text-slate-500",
    icon: Ban
  },
  ARCHIVED: {
    label: "Archivé",
    tone: "border-slate-200 bg-slate-50 text-slate-400",
    bg: "bg-slate-50", border: "border-slate-200", text: "text-slate-400",
    icon: Archive
  },
}

export function resolveStatusMeta(status?: ParcelStatus | string | null): StatusMeta {
  if (!status) return DEFAULT_STATUS_META
  const s = status.toString().toUpperCase()
  return STATUS_METADATA[s as ParcelStatus] || {
    ...DEFAULT_STATUS_META,
    label: typeof status === 'string' ? status : DEFAULT_STATUS_META.label
  }
}

export interface RiskMeta {
  label: string
  tone: string
  bg: string
  border: string
  text: string
  icon: LucideIcon
}

const DEFAULT_RISK_META: RiskMeta = {
  label: "Non évalué",
  tone: "border-slate-200 bg-slate-50 text-slate-500",
  bg: "bg-slate-50",
  border: "border-slate-200",
  text: "text-slate-500",
  icon: Info
}

const RISK_METADATA: Record<RiskLevel, RiskMeta> = {
  LOW: {
    label: "Faible",
    tone: "border-emerald-200 bg-emerald-50 text-emerald-700",
    bg: "bg-emerald-50", border: "border-emerald-200", text: "text-emerald-700",
    icon: ShieldCheck
  },
  MEDIUM: {
    label: "Modéré",
    tone: "border-amber-200 bg-amber-50 text-amber-700",
    bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-700",
    icon: Info
  },
  HIGH: {
    label: "Élevé",
    tone: "border-orange-200 bg-orange-50 text-orange-700",
    bg: "bg-orange-50", border: "border-orange-200", text: "text-orange-700",
    icon: AlertTriangle
  },
  CRITICAL: {
    label: "Critique",
    tone: "border-red-200 bg-red-50 text-red-700",
    bg: "bg-red-50", border: "border-red-200", text: "text-red-700",
    icon: AlertOctagon
  },
}

export function resolveRiskMeta(risk?: RiskLevel | string | null): RiskMeta {
  if (!risk) return DEFAULT_RISK_META
  const r = risk.toString().toUpperCase()
  return RISK_METADATA[r as RiskLevel] || {
    ...DEFAULT_RISK_META,
    label: typeof risk === 'string' ? risk : DEFAULT_RISK_META.label
  }
}

/** Activity Types Meta */

export type ActivityType = "verification" | "transfer" | "alert" | "document" | "blockchain" | "inspection"

export interface ActivityMeta {
  label: string
  tone: string
  icon: LucideIcon
}

const DEFAULT_ACTIVITY_META: ActivityMeta = {
  label: "Activité",
  tone: "text-slate-600 bg-slate-100",
  icon: Info
}

const ACTIVITY_METADATA: Record<ActivityType, ActivityMeta> = {
  verification: { label: "Vérification", tone: "text-emerald-700 bg-emerald-50", icon: ShieldCheck },
  transfer: { label: "Transfert", tone: "text-blue-700 bg-blue-50", icon: ArrowLeftRight },
  alert: { label: "Alerte", tone: "text-red-700 bg-red-50", icon: AlertTriangle },
  document: { label: "Document", tone: "text-slate-600 bg-slate-100", icon: FileText },
  blockchain: { label: "Blockchain", tone: "text-amber-700 bg-amber-50", icon: Link2 },
  inspection: { label: "Inspection", tone: "text-sky-700 bg-sky-50", icon: ClipboardCheck },
}

export function resolveActivityMeta(type?: string | null): ActivityMeta {
  if (!type) return DEFAULT_ACTIVITY_META
  return ACTIVITY_METADATA[type as ActivityType] || {
    ...DEFAULT_ACTIVITY_META,
    label: type
  }
}

/** Anomaly/Alert Severity Meta */

export type SeverityType = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

export interface SeverityMeta {
  label: string
  tone: string
  icon: LucideIcon
}

const DEFAULT_SEVERITY_META: SeverityMeta = {
  label: "Note",
  tone: "text-muted-foreground",
  icon: Info
}

const SEVERITY_METADATA: Record<SeverityType, SeverityMeta> = {
  LOW: { label: "Faible", tone: "text-blue-600", icon: Info },
  MEDIUM: { label: "Moyen", tone: "text-amber-600", icon: Info },
  HIGH: { label: "Élevé", tone: "text-orange-600", icon: AlertTriangle },
  CRITICAL: { label: "Critique", tone: "text-red-600", icon: AlertOctagon },
}

export function resolveSeverityMeta(severity?: string | null): SeverityMeta {
  if (!severity) return DEFAULT_SEVERITY_META
  const s = severity.toUpperCase()
  return SEVERITY_METADATA[s as SeverityType] || {
    ...DEFAULT_SEVERITY_META,
    label: severity
  }
}

/** Monitoring Event Type Meta */

export type MonitoringEventType =
  | "CONSTRUCTION"
  | "EXCAVATION"
  | "VEHICLE"
  | "ILLEGAL_OCCUPATION"
  | "FIRE"
  | "CROWD_ACTIVITY"
  | "VEGETATION_CLEARING"
  | "UNKNOWN"

export interface MonitoringTypeMeta {
  label: string
  icon: LucideIcon
}

const DEFAULT_MONITORING_TYPE_META: MonitoringTypeMeta = {
  label: "Inconnu",
  icon: Info
}

const MONITORING_TYPE_METADATA: Record<MonitoringEventType, MonitoringTypeMeta> = {
  CONSTRUCTION: { label: "Construction", icon: FileSignature },
  EXCAVATION: { label: "Terrassement", icon: ScanLine },
  VEHICLE: { label: "Engin Lourd", icon: ShieldCheck },
  ILLEGAL_OCCUPATION: { label: "Occupation Illégale", icon: AlertTriangle },
  FIRE: { label: "Feu / Incendie", icon: AlertOctagon },
  CROWD_ACTIVITY: { label: "Activité de Foule", icon: Info },
  VEGETATION_CLEARING: { label: "Déforestation", icon: ScanLine },
  UNKNOWN: { label: "Inconnu", icon: Info },
}

export function resolveMonitoringTypeMeta(type?: string | null): MonitoringTypeMeta {
  if (!type) return DEFAULT_MONITORING_TYPE_META
  const t = type.toUpperCase()
  return MONITORING_TYPE_METADATA[t as MonitoringEventType] || {
    ...DEFAULT_MONITORING_TYPE_META,
    label: type
  }
}

/** Blockchain Events Meta */

export type BlockchainEventType = "registration" | "transfer" | "verification" | "update" | "certification"

export interface BlockchainEventMeta {
  label: string
  tone: string
  icon: LucideIcon
}

const DEFAULT_EVENT_META: BlockchainEventMeta = {
  label: "Événement",
  tone: "text-muted-foreground",
  icon: Hash
}

const EVENT_METADATA: Record<BlockchainEventType, BlockchainEventMeta> = {
  registration: { label: "Inscription", tone: "text-blue-600", icon: FileCheck2 },
  transfer: { label: "Transfert", tone: "text-amber-600", icon: ArrowLeftRight },
  verification: { label: "Vérification", tone: "text-emerald-600", icon: ShieldCheck },
  update: { label: "Mise à jour", tone: "text-slate-600", icon: Edit3 },
  certification: { label: "Certification", tone: "text-emerald-600", icon: Coins },
}

export function resolveBlockchainEventMeta(type?: string | null): BlockchainEventMeta {
  if (!type) return DEFAULT_EVENT_META
  return EVENT_METADATA[type as BlockchainEventType] || {
    ...DEFAULT_EVENT_META,
    label: type
  }
}

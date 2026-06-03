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
  tone: "border-zinc-500/30 bg-zinc-500/10 text-zinc-400",
  bg: "bg-zinc-500/10",
  border: "border-zinc-500/30",
  text: "text-zinc-400",
  icon: Info
}

const STATUS_METADATA: Record<ParcelStatus, StatusMeta> = {
  DRAFT: { 
    label: "Brouillon", 
    tone: "border-zinc-500/30 bg-zinc-500/10 text-zinc-400",
    bg: "bg-zinc-500/10", border: "border-zinc-500/30", text: "text-zinc-400",
    icon: FileSignature
  },
  SUBMITTED: { 
    label: "Soumis", 
    tone: "border-blue-500/30 bg-blue-500/10 text-blue-400",
    bg: "bg-blue-500/10", border: "border-blue-500/30", text: "text-blue-400",
    icon: Clock
  },
  UNDER_SURVEY: { 
    label: "Arpentage", 
    tone: "border-orange-500/30 bg-orange-500/10 text-orange-400",
    bg: "bg-orange-500/10", border: "border-orange-500/30", text: "text-orange-400",
    icon: ScanLine
  },
  UNDER_VERIFICATION: { 
    label: "Vérification", 
    tone: "border-blue-400/30 bg-blue-400/10 text-blue-400",
    bg: "bg-blue-400/10", border: "border-blue-400/30", text: "text-blue-400",
    icon: ShieldCheck
  },
  UNDER_NOTARY_REVIEW: { 
    label: "Notariat", 
    tone: "border-purple-500/30 bg-purple-500/10 text-purple-400",
    bg: "bg-purple-500/10", border: "border-purple-500/30", text: "text-purple-400",
    icon: FileSignature
  },
  UNDER_ADMIN_REVIEW: { 
    label: "Examen Admin", 
    tone: "border-yellow-500/40 bg-yellow-500/15 text-yellow-500",
    bg: "bg-yellow-500/15", border: "border-yellow-500/40", text: "text-yellow-500",
    icon: Info
  },
  CERTIFIED: { 
    label: "Certifié", 
    tone: "border-emerald/30 bg-emerald/10 text-emerald",
    bg: "bg-emerald/10", border: "border-emerald/30", text: "text-emerald",
    icon: CheckCircle2
  },
  TITLE_ISSUED: { 
    label: "TF Émis", 
    tone: "border-emerald/50 bg-emerald/20 text-emerald shadow-[0_0_10px_rgba(16,185,129,0.2)]",
    bg: "bg-emerald/20", border: "border-emerald/50", text: "text-emerald",
    icon: FileCheck2
  },
  DISPUTED: { 
    label: "Litige", 
    tone: "border-red-500/30 bg-red-500/15 text-red-500",
    bg: "bg-red-500/15", border: "border-red-500/30", text: "text-red-500",
    icon: AlertTriangle
  },
  REJECTED: { 
    label: "Rejeté", 
    tone: "border-zinc-700 bg-zinc-800 text-zinc-500",
    bg: "bg-zinc-800", border: "border-zinc-700", text: "text-zinc-500",
    icon: Ban
  },
  ARCHIVED: { 
    label: "Archivé", 
    tone: "border-zinc-800 bg-zinc-900 text-zinc-600",
    bg: "bg-zinc-900", border: "border-zinc-800", text: "text-zinc-600",
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
  tone: "border-zinc-500/30 bg-zinc-500/10 text-zinc-400",
  bg: "bg-zinc-500/10",
  border: "border-zinc-500/30",
  text: "text-zinc-400",
  icon: Info
}

const RISK_METADATA: Record<RiskLevel, RiskMeta> = {
  LOW: { 
    label: "Faible", 
    tone: "border-emerald/30 bg-emerald/10 text-emerald",
    bg: "bg-emerald/10", border: "border-emerald/30", text: "text-emerald",
    icon: ShieldCheck
  },
  MEDIUM: { 
    label: "Modéré", 
    tone: "border-yellow-500/40 bg-yellow-500/15 text-yellow-500",
    bg: "bg-yellow-500/15", border: "border-yellow-500/40", text: "text-yellow-500",
    icon: Info
  },
  HIGH: { 
    label: "Élevé", 
    tone: "border-orange-500/40 bg-orange-500/15 text-orange-500",
    bg: "bg-orange-500/15", border: "border-orange-500/40", text: "text-orange-500",
    icon: AlertTriangle
  },
  CRITICAL: { 
    label: "Critique", 
    tone: "border-red-500/40 bg-red-500/15 text-red-500",
    bg: "bg-red-500/15", border: "border-red-500/40", text: "text-red-500",
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
  tone: "text-foreground bg-secondary",
  icon: Info
}

const ACTIVITY_METADATA: Record<ActivityType, ActivityMeta> = {
  verification: { label: "Vérification", tone: "text-emerald bg-emerald/10", icon: ShieldCheck },
  transfer: { label: "Transfert", tone: "text-blue-400 bg-blue-400/10", icon: ArrowLeftRight },
  alert: { label: "Alerte", tone: "text-red-500 bg-red-500/10", icon: AlertTriangle },
  document: { label: "Document", tone: "text-foreground bg-secondary", icon: FileText },
  blockchain: { label: "Blockchain", tone: "text-yellow-500 bg-yellow-500/10", icon: Link2 },
  inspection: { label: "Inspection", tone: "text-blue-400 bg-blue-400/10", icon: ClipboardCheck },
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
  LOW: { label: "Faible", tone: "text-blue-400", icon: Info },
  MEDIUM: { label: "Moyen", tone: "text-yellow-500", icon: Info },
  HIGH: { label: "Élevé", tone: "text-orange-500", icon: AlertTriangle },
  CRITICAL: { label: "Critique", tone: "text-red-500", icon: AlertOctagon },
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
  registration: { label: "Inscription", tone: "text-info", icon: FileCheck2 },
  transfer: { label: "Transfert", tone: "text-gold", icon: ArrowLeftRight },
  verification: { label: "Vérification", tone: "text-emerald", icon: ShieldCheck },
  update: { label: "Mise à jour", tone: "text-foreground", icon: Edit3 },
  certification: { label: "Certification", tone: "text-emerald", icon: Coins },
}

export function resolveBlockchainEventMeta(type?: string | null): BlockchainEventMeta {
  if (!type) return DEFAULT_EVENT_META
  return EVENT_METADATA[type as BlockchainEventType] || {
    ...DEFAULT_EVENT_META,
    label: type
  }
}

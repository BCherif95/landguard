// Adapters between the backend Parcel enum casing and the existing UI pill components
// (which were generated against the lowercase legacy types in lib/types.ts).

import type {
  ParcelStatus as ApiStatus,
  RiskLevel as ApiRisk,
} from "@/lib/api/parcels"
import type {
  ParcelStatus as LegacyStatus,
  RiskLevel as LegacyRisk,
} from "@/lib/types"

const STATUS: Record<ApiStatus, LegacyStatus> = {
  DRAFT: "draft",
  SUBMITTED: "submitted",
  UNDER_SURVEY: "under_survey",
  UNDER_VERIFICATION: "under_verification",
  UNDER_NOTARY_REVIEW: "under_notary_review",
  UNDER_ADMIN_REVIEW: "under_admin_review",
  CERTIFIED: "certified",
  TITLE_ISSUED: "title_issued",
  DISPUTED: "disputed",
  REJECTED: "rejected",
  ARCHIVED: "archived",
}

const RISK: Record<ApiRisk, LegacyRisk> = {
  LOW: "low",
  MEDIUM: "medium",
  HIGH: "high",
  CRITICAL: "critical",
}

export const toLegacyStatus = (s: ApiStatus): LegacyStatus => STATUS[s]
export const toLegacyRisk = (r: ApiRisk): LegacyRisk => RISK[r]

export function formatXof(amount: number): string {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "XOF",
    maximumFractionDigits: 0,
  }).format(amount)
}

export function formatDate(iso: string): string {
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "long" }).format(new Date(iso))
}

export function formatDateTime(iso: string): string {
  return new Intl.DateTimeFormat("fr-FR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(iso))
}

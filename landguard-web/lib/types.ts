// Domain types for LA BOUSSOLE — code in English, all UI strings localized in French.

export type ParcelStatus = 
  | "draft" 
  | "submitted" 
  | "under_survey" 
  | "under_verification" 
  | "under_notary_review" 
  | "under_admin_review" 
  | "certified" 
  | "title_issued" 
  | "disputed" 
  | "rejected" 
  | "archived"
export type RiskLevel = "low" | "medium" | "high" | "critical"
export type AlertSeverity = "info" | "warning" | "critical"

export interface GeoPoint {
  lat: number
  lng: number
}

export interface Parcel {
  id: string
  reference: string
  name: string
  owner: string
  region: string
  area: number // hectares
  status: ParcelStatus
  risk: RiskLevel
  riskScore: number // 0..100
  trustScore: number // 0..100
  estimatedValue: number // XOF
  lastVerifiedAt: string
  blockchainHash: string
  titleNumber?: string | null
  coordinates: GeoPoint
  /** Polygon coordinates in normalized 0..1 space for the stylized satellite view. */
  polygon: { x: number; y: number }[]
}

export interface AnomalyEvent {
  id: string
  parcelId: string
  type:
    | "construction"
    | "earthworks"
    | "vehicle"
    | "illegal-occupation"
    | "boundary-shift"
    | "vegetation-loss"
  severity: AlertSeverity
  detectedAt: string
  confidence: number // 0..100
  position: { x: number; y: number } // normalized
  description: string
}

export interface ActivityEntry {
  id: string
  parcelId: string
  type:
    | "verification"
    | "transfer"
    | "alert"
    | "document"
    | "blockchain"
    | "inspection"
  actor: string
  message: string
  timestamp: string
}

export interface BlockchainEvent {
  id: string
  parcelId: string
  hash: string
  type: "registration" | "transfer" | "verification" | "update" | "certification"
  actor: string
  timestamp: string
  blockNumber: number
  description: string
}

export interface Heir {
  id: string
  name: string
  relation: string
  share: number // percentage
  validated: boolean
  avatarColor: string
}

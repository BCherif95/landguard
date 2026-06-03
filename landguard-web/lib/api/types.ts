// Shared transport types — mirrored from the backend AuthResponse / ApiError shape.

import { Parcel } from "./parcels"

export type Role = "CITIZEN" | "OFFICER" | "LEGAL" | "BANKER" | "ADMIN"
export type UserStatus = "PENDING" | "ACTIVE" | "DISABLED" | "LOCKED"

export interface AuthenticatedUser {
  id: string
  email: string
  fullName: string
  role: Role
  status: UserStatus
  createdAt: string
  lastLoginAt: string | null
}

export interface AuthResponse {
  user: AuthenticatedUser | null
  accessToken: string
  accessTokenExpiresAt: string
  refreshToken: string
  refreshTokenExpiresAt: string
}

export interface RegisterPayload {
  email: string
  fullName: string
  password: string
  role?: Role
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RefreshPayload {
  refreshToken: string
}

export type MonitoringEventType = 
  | "CONSTRUCTION"
  | "EXCAVATION"
  | "VEHICLE"
  | "ILLEGAL_OCCUPATION"
  | "FIRE"
  | "CROWD_ACTIVITY"
  | "VEGETATION_CLEARING"
  | "UNKNOWN"

export type MonitoringSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

export type MonitoringSource = "SATELLITE" | "DRONE" | "IoT_SENSOR" | "COMMUNITY_REPORT" | "AI_DETECTION"

export interface MonitoringEvent {
  id: string
  parcelId: string
  type: MonitoringEventType
  severity: MonitoringSeverity
  detectedAt: string
  confidenceScore: number
  longitude: number
  latitude: number
  description: string
  source: MonitoringSource
  imageUrl: string | null
  resolved: boolean
}

export interface SatelliteSnapshot {
  id: string
  parcelId: string
  capturedAt: string
  imageUrl: string
  movementScore: number
  anomalyScore: number
  metadata: string | null
}

export type SuccessionStatus = "DRAFT" | "IN_VOTING" | "VALIDATED" | "ANCHORED" | "TRANSFERRED" | "REJECTED"

export interface Heir {
  id: string
  fullName: string
  relation: string
  sharePercentage: number
  validated: boolean
}

export interface SuccessionPlan {
  id: string
  parcelId: string
  status: SuccessionStatus
  blockchainHash: string | null
  heirs: Heir[]
  createdAt: string
  updatedAt: string
}

export type SuccessionAuditType = 
  | "PLAN_CREATED"
  | "HEIR_ADDED"
  | "HEIR_REMOVED"
  | "SUBMITTED_FOR_VOTING"
  | "VOTE_CAST"
  | "PLAN_VALIDATED"
  | "PLAN_REJECTED"
  | "BLOCKCHAIN_ANCHORED"
  | "OWNERSHIP_TRANSFERRED"
  | "ASSET_TRANSFERRED"

export interface SuccessionAuditEvent {
  id: string
  successionPlanId: string
  type: SuccessionAuditType
  actor: string
  createdAt: string
  metadata: Record<string, string>
}

export type DisputeSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"
export type DisputeStatus = "OPEN" | "UNDER_INVESTIGATION" | "MEDIATION" | "RESOLVED" | "CLOSED"

export interface LegalDispute {
  id: string
  parcelId: string
  reason: string
  severity: DisputeSeverity
  status: DisputeStatus
  openedAt: string
  resolvedAt: string | null
}

export interface LegalDossier {
  parcel: Parcel | null
  heirs: Heir[]
  auditTrail: SuccessionAuditEvent[]
  blockchainHash: string | null
  generatedAt: string
  qrCodeContent: string
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  code: string
  message: string
  path: string
  errors: Array<{ field: string; message: string }>
}

export class ApiError extends Error {
  status: number
  code: string
  fieldErrors: Array<{ field: string; message: string }>

  constructor(body: ApiErrorBody) {
    super(body.message)
    this.status = body.status
    this.code = body.code
    this.fieldErrors = body.errors ?? []
  }
}

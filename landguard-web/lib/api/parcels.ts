import { api } from "./client"
import { isValidCadastralReference } from "../domain/cadastral-reference"

/**
 * Validates RegisterParcelPayload before sending to API.
 * Ensures contract alignment with backend CadastralReference validation.
 */
export function validateParcelPayload(payload: RegisterParcelPayload): void {
  if (!payload.reference) {
    throw new Error('Cadastral reference is required')
  }

  if (!isValidCadastralReference(payload.reference)) {
    throw new Error(
        `Invalid cadastral reference format: ${payload.reference}. ` +
        'Expected format: XXX-YY-YYYY-NNNNNN (e.g., BSL-ML-2024-654321)'
    )
  }

  if (!payload.name || payload.name.length < 2) {
    throw new Error('Parcel name must be at least 2 characters')
  }

  if (!payload.geometry || !payload.geometry.coordinates) {
    throw new Error('Parcel geometry is required')
  }

  if (!Array.isArray(payload.documents)) {
    throw new Error('Documents must be an array')
  }
}

export type ParcelStatus =
    | "DRAFT"
    | "SUBMITTED"
    | "UNDER_SURVEY"
    | "UNDER_VERIFICATION"
    | "UNDER_NOTARY_REVIEW"
    | "UNDER_ADMIN_REVIEW"
    | "CERTIFIED"
    | "TITLE_ISSUED"
    | "DISPUTED"
    | "REJECTED"
    | "ARCHIVED"
export type RiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

/** GeoJSON Polygon object. */
export interface PolygonGeometry {
  type: "Polygon"
  coordinates: number[][][]
}

export interface ParcelCentroid {
  longitude: number
  latitude: number
}

export interface Parcel {
  id: string
  reference: string
  name: string
  regionLabel: string
  ownerLabel: string
  ownerUserId: string | null
  areaHectares: string // BigDecimal serialised as string
  estimatedValueXof: number
  status: ParcelStatus
  riskLevel: RiskLevel
  riskScore: number
  trustScore: number
  titleNumber: string | null
  geometry: PolygonGeometry
  centroid: ParcelCentroid
  createdAt: string
  updatedAt: string
  lastVerifiedAt: string | null
}

export interface LandDocument {
  type: 'TF' | 'PLAN' | 'ID' | 'CESSION' | 'TAX'
  label: string
  storageKey: string
  fileName: string
}

export interface RegisterParcelPayload {
  reference: string
  name: string
  regionLabel: string
  ownerLabel: string
  areaHectares: number
  estimatedValueXof: number
  geometry: PolygonGeometry
  documents: LandDocument[]
}

export interface UploadDocumentResponse {
  id: string
  storageKey: string
  fileName: string
  uploadedAt: string
}

export const parcelsApi = {
  list: async (params?: { ownerUserId?: string; limit?: number }): Promise<Parcel[]> => {
    const { data } = await api.get<Parcel[]>("/parcels", { params })
    return data
  },

  get: async (id: string): Promise<Parcel> => {
    const { data } = await api.get<Parcel>(`/parcels/${id}`)
    return data
  },

  register: async (payload: RegisterParcelPayload): Promise<Parcel> => {
    validateParcelPayload(payload)
    const { data } = await api.post<Parcel>("/parcels", payload)
    return data
  },

  verify: async (id: string, newTrustScore: number): Promise<Parcel> => {
    const { data } = await api.post<Parcel>(`/parcels/${id}/verify`, { newTrustScore })
    return data
  },

  transition: async (id: string, status: ParcelStatus): Promise<Parcel> => {
    const { data } = await api.post<Parcel>(`/parcels/${id}/transition`, null, {
      params: { status }
    })
    return data
  },

  issueTitle: async (id: string): Promise<Parcel> => {
    const { data } = await api.post<Parcel>(`/parcels/${id}/issue-title`)
    return data
  },

  getTitlePdfUrl: (id: string): string => {
    return `${api.defaults.baseURL}/parcels/${id}/title-pdf`
  },

  uploadDocument: async (file: File): Promise<UploadDocumentResponse> => {
    const formData = new FormData()
    formData.append("file", file)
    const { data } = await api.post<UploadDocumentResponse>("/documents/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    })
    return data
  },
}
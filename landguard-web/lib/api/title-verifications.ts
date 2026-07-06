import { api } from "./client"

/** Mirror of the backend VerificationStatus enum (Malian title workflow). */
export type VerificationStatus =
  | "DRAFT"
  | "PENDING_VERIFICATION"
  | "DOMAIN_CONTROL"
  | "TF_VERIFIED"
  | "TF_REJECTED"
  | "DISPUTE_SIGNALED"
  | "PENDING_COMPLEMENT"
  | "CERTIFIED"

export interface ReportedLandTitle {
  tfNumber: string
  volume: string
  folio: string
  conservationOffice: string
  issueDate: string
  ownerName: string
  areaHectares: number
  location: string
  status: string
}

export interface TitleRequisition {
  requisitionNumber: string
  requisitionDate: string
  domainOffice: string
  verifierName: string
  verifierRole: string
  verificationNotes: string
  authenticityConfirmed: boolean
  conflictDetected: boolean
  litigationDetected: boolean
  rejectionReason: string | null
}

export interface TitleVerificationCase {
  id: string
  parcelId: string
  caseReference: string
  status: VerificationStatus
  reportedTitle: ReportedLandTitle
  requisition: TitleRequisition | null
  initiatedBy: string
  createdAt: string
  updatedAt: string
  certifiedAt: string | null
  certifiedBy: string | null
}

export interface OpenCasePayload {
  parcelId: string
  caseReference: string
  reportedTitle: ReportedLandTitle
}

export const titleVerificationsApi = {
  listByParcel: async (parcelId: string): Promise<TitleVerificationCase[]> => {
    const { data } = await api.get<TitleVerificationCase[]>("/title-verifications", {
      params: { parcelId },
    })
    return data
  },

  get: async (id: string): Promise<TitleVerificationCase> => {
    const { data } = await api.get<TitleVerificationCase>(`/title-verifications/${id}`)
    return data
  },

  openCase: async (payload: OpenCasePayload): Promise<TitleVerificationCase> => {
    const { data } = await api.post<TitleVerificationCase>("/title-verifications", payload)
    return data
  },

  submit: async (id: string): Promise<void> => {
    await api.post(`/title-verifications/${id}/submit`)
  },

  startDomainControl: async (id: string): Promise<void> => {
    await api.post(`/title-verifications/${id}/start-domain-control`)
  },

  recordRequisition: async (id: string, requisition: TitleRequisition): Promise<void> => {
    await api.post(`/title-verifications/${id}/requisition`, requisition)
  },

  certify: async (id: string): Promise<void> => {
    await api.post(`/title-verifications/${id}/certify`)
  },
}

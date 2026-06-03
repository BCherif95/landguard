import { api } from "./client"
import { SuccessionPlan, SuccessionAuditEvent } from "./types"

export const heritageApi = {
  listPlans: async (): Promise<SuccessionPlan[]> => {
    const { data } = await api.get<SuccessionPlan[]>("/heritage/plans")
    return data
  },

  getPlanByParcel: async (parcelId: string): Promise<SuccessionPlan> => {
    const { data } = await api.get<SuccessionPlan>(`/heritage/plans/parcel/${parcelId}`)
    return data
  },

  createPlan: async (parcelId: string): Promise<SuccessionPlan> => {
    const { data } = await api.post<SuccessionPlan>(`/heritage/plans`, null, { params: { parcelId } })
    return data
  },

  addHeir: async (planId: string, payload: { fullName: string, relation: string, sharePercentage: number }): Promise<SuccessionPlan> => {
    const { data } = await api.post<SuccessionPlan>(`/heritage/plans/${planId}/heirs`, payload)
    return data
  },

  submitForVoting: async (planId: string): Promise<void> => {
    await api.post(`/heritage/plans/${planId}/submit`)
  },

  vote: async (planId: string, payload: { heirId: string, approved: boolean }): Promise<void> => {
    await api.post(`/heritage/plans/${planId}/vote`, payload)
  },

  anchor: async (planId: string): Promise<void> => {
    await api.post(`/heritage/plans/${planId}/anchor`)
  },

  transfer: async (planId: string): Promise<void> => {
    await api.post(`/heritage/plans/${planId}/transfer`)
  },

  getAuditTrail: async (planId: string): Promise<SuccessionAuditEvent[]> => {
    // This might need a new endpoint in the backend or we can fetch it via general audit trail
    // For now, let's assume there's an endpoint or we add it
    const { data } = await api.get<SuccessionAuditEvent[]>(`/heritage/plans/${planId}/audit`)
    return data
  }
}

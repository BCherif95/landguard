import { api } from "./client"
import { LegalDispute, LegalDossier } from "./types"

export const legalApi = {
  listDisputes: async (): Promise<LegalDispute[]> => {
    const { data } = await api.get<LegalDispute[]>("/legal/disputes")
    return data
  },

  openDispute: async (payload: { parcelId: string, reason: string, severity: string }): Promise<string> => {
    const { data } = await api.post<string>("/legal/disputes", payload)
    return data
  },

  resolveDispute: async (id: string): Promise<void> => {
    await api.post(`/legal/disputes/${id}/resolve`)
  },

  generateProof: async (parcelId: string): Promise<LegalDossier> => {
    const { data } = await api.post<LegalDossier>("/legal/generate-proof", null, { params: { parcelId } })
    return data
  }
}

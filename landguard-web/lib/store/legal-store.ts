import { create } from "zustand"
import { LegalDispute } from "../api/types"

interface LegalState {
  disputes: LegalDispute[]
  loading: boolean
  
  setDisputes: (disputes: LegalDispute[]) => void
  addDispute: (dispute: LegalDispute) => void
  updateDispute: (dispute: LegalDispute) => void
  setLoading: (loading: boolean) => void
}

export const useLegalStore = create<LegalState>((set) => ({
  disputes: [],
  loading: false,

  setDisputes: (disputes) => set({ disputes }),
  addDispute: (dispute) => set((state) => ({ disputes: [...state.disputes, dispute] })),
  updateDispute: (dispute) => set((state) => ({
    disputes: state.disputes.map(d => d.id === dispute.id ? dispute : d)
  })),
  setLoading: (loading) => set({ loading }),
}))

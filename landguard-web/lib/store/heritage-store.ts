import { create } from "zustand"
import { SuccessionPlan, SuccessionAuditEvent } from "../api/types"

interface HeritageState {
  plans: SuccessionPlan[]
  selectedPlanId: string | null
  auditTrail: Record<string, SuccessionAuditEvent[]>
  loading: boolean
  
  setPlans: (plans: SuccessionPlan[]) => void
  addPlan: (plan: SuccessionPlan) => void
  updatePlan: (plan: SuccessionPlan) => void
  setSelectedPlan: (planId: string | null) => void
  setAuditTrail: (planId: string, events: SuccessionAuditEvent[]) => void
  setLoading: (loading: boolean) => void
}

export const useHeritageStore = create<HeritageState>((set) => ({
  plans: [],
  selectedPlanId: null,
  auditTrail: {},
  loading: false,

  setPlans: (plans) => set({ plans }),
  addPlan: (plan) => set((state) => ({ plans: [...state.plans, plan] })),
  updatePlan: (plan) => set((state) => ({
    plans: state.plans.map(p => p.id === plan.id ? plan : p)
  })),
  setSelectedPlan: (selectedPlanId) => set({ selectedPlanId }),
  setAuditTrail: (planId, events) => set((state) => ({
    auditTrail: { ...state.auditTrail, [planId]: events }
  })),
  setLoading: (loading) => set({ loading }),
}))

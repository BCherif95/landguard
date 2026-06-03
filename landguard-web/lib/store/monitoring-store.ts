import { create } from "zustand"
import { MonitoringEvent, MonitoringSeverity } from "../api/types"

interface MonitoringState {
  events: MonitoringEvent[]
  selectedEventId: string | null
  filters: {
    parcelId: string | null
    severity: MonitoringSeverity | null
  }
  setEvents: (events: MonitoringEvent[]) => void
  addEvent: (event: MonitoringEvent) => void
  resolveEvent: (eventId: string) => void
  setSelectedEvent: (eventId: string | null) => void
  setFilters: (filters: Partial<MonitoringState["filters"]>) => void
}

export const useMonitoringStore = create<MonitoringState>((set) => ({
  events: [],
  selectedEventId: null,
  filters: {
    parcelId: null,
    severity: null,
  },
  setEvents: (events) => set({ events }),
  addEvent: (event) => set((state) => ({ 
    events: [event, ...state.events].slice(0, 100) // Keep last 100
  })),
  resolveEvent: (eventId) => set((state) => ({
    events: state.events.map(e => e.id === eventId ? { ...e, resolved: true } : e)
  })),
  setSelectedEvent: (selectedEventId) => set({ selectedEventId }),
  setFilters: (newFilters) => set((state) => ({ 
    filters: { ...state.filters, ...newFilters } 
  })),
}))

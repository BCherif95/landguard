import { api } from "./client"
import { MonitoringEvent, MonitoringSeverity, SatelliteSnapshot } from "./types"

export const monitoringApi = {
  listEvents: async (params?: { parcelId?: string; severity?: MonitoringSeverity }) => {
    const { data } = await api.get<MonitoringEvent[]>("/monitoring/events", { params })
    return data
  },

  resolveEvent: async (id: string) => {
    await api.post<void>(`/monitoring/events/${id}/resolve`, {})
  },

  listSnapshots: async (params?: { parcelId?: string }) => {
    const { data } = await api.get<SatelliteSnapshot[]>("/monitoring/snapshots", { params })
    return data
  },

  getStreamUrl: () => {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1"
    return `${baseUrl}/monitoring/stream`
  }
}

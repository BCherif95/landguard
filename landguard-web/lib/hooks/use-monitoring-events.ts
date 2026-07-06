"use client"

import { useMemo } from "react"
import { useQuery } from "@tanstack/react-query"
import { monitoringApi } from "@/lib/api/monitoring"
import { useMonitoringStore } from "@/lib/store/monitoring-store"
import type { MonitoringEvent, MonitoringSeverity } from "@/lib/api/types"

/**
 * Monitoring events fetched from the API, merged with any live events received
 * over the SSE stream since the page loaded (deduplicated by id, newest first).
 */
export function useMonitoringEvents(
  params?: { parcelId?: string; severity?: MonitoringSeverity },
  options?: { enabled?: boolean },
) {
  const query = useQuery({
    queryKey: ["monitoring", "events", params ?? null] as const,
    queryFn: () => monitoringApi.listEvents(params),
    enabled: options?.enabled ?? true,
  })

  const liveEvents = useMonitoringStore((s) => s.events)

  const events = useMemo<MonitoringEvent[]>(() => {
    const fetched = query.data ?? []
    const relevantLive = params?.parcelId
      ? liveEvents.filter((e) => e.parcelId === params.parcelId)
      : liveEvents
    const merged = new Map<string, MonitoringEvent>()
    for (const event of [...relevantLive, ...fetched]) {
      if (!merged.has(event.id)) merged.set(event.id, event)
    }
    return [...merged.values()].sort(
      (a, b) => new Date(b.detectedAt).getTime() - new Date(a.detectedAt).getTime(),
    )
  }, [query.data, liveEvents, params?.parcelId])

  return { ...query, events }
}

"use client"

import { useQuery } from "@tanstack/react-query"
import { monitoringApi } from "@/lib/api/monitoring"

/** Historical satellite snapshots of a parcel, oldest first. */
export function useSatelliteSnapshots(parcelId: string | null) {
  return useQuery({
    queryKey: ["monitoring", "snapshots", parcelId] as const,
    queryFn: async () => {
      const snapshots = await monitoringApi.listSnapshots({ parcelId: parcelId! })
      return [...snapshots].sort(
        (a, b) => new Date(a.capturedAt).getTime() - new Date(b.capturedAt).getTime(),
      )
    },
    enabled: !!parcelId,
  })
}

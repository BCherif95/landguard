"use client"

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { parcelsApi, type RegisterParcelPayload, type ParcelStatus } from "@/lib/api/parcels"

const KEYS = {
  list: (params?: { limit?: number }) =>
    ["parcels", "list", params ?? null] as const,
  detail: (id: string) => ["parcels", "detail", id] as const,
}

export function useParcels(params?: { limit?: number }) {
  return useQuery({
    queryKey: KEYS.list(params),
    queryFn: () => parcelsApi.list(params),
  })
}

export function useParcel(id: string | null | undefined) {
  return useQuery({
    queryKey: id ? KEYS.detail(id) : ["parcels", "detail", "noop"],
    queryFn: () => parcelsApi.get(id as string),
    enabled: Boolean(id),
  })
}

export function useRegisterParcel() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: RegisterParcelPayload) => parcelsApi.register(payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["parcels"] })
    },
  })
}

export function useVerifyParcel() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, newTrustScore }: { id: string; newTrustScore: number }) =>
      parcelsApi.verify(id, newTrustScore),
    onSuccess: (parcel) => {
      qc.invalidateQueries({ queryKey: ["parcels"] })
      qc.setQueryData(KEYS.detail(parcel.id), parcel)
    },
  })
}

export function useTransitionStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: ParcelStatus }) =>
      parcelsApi.transition(id, status),
    onSuccess: (parcel) => {
      qc.invalidateQueries({ queryKey: ["parcels"] })
      qc.setQueryData(KEYS.detail(parcel.id), parcel)
    },
  })
}

export function useIssueTitle() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => parcelsApi.issueTitle(id),
    onSuccess: (parcel) => {
      qc.invalidateQueries({ queryKey: ["parcels"] })
      qc.setQueryData(KEYS.detail(parcel.id), parcel)
    },
  })
}

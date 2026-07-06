"use client"

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  titleVerificationsApi,
  type OpenCasePayload,
  type TitleRequisition,
} from "@/lib/api/title-verifications"

const KEYS = {
  byParcel: (parcelId: string) => ["title-verifications", "by-parcel", parcelId] as const,
}

/** The (at most one) verification case attached to a parcel, or null. */
export function useTitleVerificationCase(parcelId: string | null) {
  return useQuery({
    queryKey: KEYS.byParcel(parcelId ?? "none"),
    queryFn: async () => {
      const cases = await titleVerificationsApi.listByParcel(parcelId!)
      return cases[0] ?? null
    },
    enabled: !!parcelId,
  })
}

export function useOpenVerificationCase() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: OpenCasePayload) => titleVerificationsApi.openCase(payload),
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: KEYS.byParcel(created.parcelId) })
    },
  })
}

function useCaseAction(action: (id: string) => Promise<void>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: string; parcelId: string }) => action(id),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: KEYS.byParcel(variables.parcelId) })
    },
  })
}

export function useSubmitVerificationCase() {
  return useCaseAction((id) => titleVerificationsApi.submit(id))
}

export function useStartDomainControl() {
  return useCaseAction((id) => titleVerificationsApi.startDomainControl(id))
}

export function useCertifyVerificationCase() {
  return useCaseAction((id) => titleVerificationsApi.certify(id))
}

export function useRecordRequisition() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, requisition }: { id: string; parcelId: string; requisition: TitleRequisition }) =>
      titleVerificationsApi.recordRequisition(id, requisition),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: KEYS.byParcel(variables.parcelId) })
    },
  })
}

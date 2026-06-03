import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { heritageApi } from "../api/heritage"
import { useHeritageStore } from "../store/heritage-store"
import { useEffect } from "react"

export function useHeritagePlans() {
  const setPlans = useHeritageStore((state) => state.setPlans)
  
  const query = useQuery({
    queryKey: ["heritage-plans"],
    queryFn: heritageApi.listPlans,
  })

  useEffect(() => {
    if (query.data) {
      setPlans(query.data)
    }
  }, [query.data, setPlans])

  return query
}

export function useHeritagePlan(parcelId: string | null) {
  const queryClient = useQueryClient()
  const setSelectedPlan = useHeritageStore((state) => state.setSelectedPlan)

  const query = useQuery({
    queryKey: ["heritage-plan", parcelId],
    queryFn: () => parcelId ? heritageApi.getPlanByParcel(parcelId) : Promise.reject("No parcel ID"),
    enabled: !!parcelId,
  })

  useEffect(() => {
    if (query.data) {
      setSelectedPlan(query.data.id)
    }
  }, [query.data, setSelectedPlan])

  return query
}

export function useHeritageMutations() {
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: heritageApi.createPlan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
    },
  })

  const addHeirMutation = useMutation({
    mutationFn: ({ planId, payload }: { planId: string, payload: { fullName: string, relation: string, sharePercentage: number } }) => heritageApi.addHeir(planId, payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
      queryClient.invalidateQueries({ queryKey: ["heritage-plan"] })
    },
  })

  const submitMutation = useMutation({
    mutationFn: heritageApi.submitForVoting,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
      queryClient.invalidateQueries({ queryKey: ["heritage-plan"] })
    },
  })

  const voteMutation = useMutation({
    mutationFn: ({ planId, payload }: { planId: string, payload: { heirId: string, approved: boolean } }) => 
      heritageApi.vote(planId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
      queryClient.invalidateQueries({ queryKey: ["heritage-plan"] })
    },
  })

  const anchorMutation = useMutation({
    mutationFn: heritageApi.anchor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
      queryClient.invalidateQueries({ queryKey: ["heritage-plan"] })
    },
  })

  const transferMutation = useMutation({
    mutationFn: heritageApi.transfer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["heritage-plans"] })
      queryClient.invalidateQueries({ queryKey: ["heritage-plan"] })
      queryClient.invalidateQueries({ queryKey: ["parcels"] })
    },
  })

  return {
    createPlan: createMutation.mutateAsync,
    addHeir: addHeirMutation.mutateAsync,
    submitForVoting: submitMutation.mutateAsync,
    vote: voteMutation.mutateAsync,
    anchor: anchorMutation.mutateAsync,
    transfer: transferMutation.mutateAsync,
    isPending: createMutation.isPending || addHeirMutation.isPending || submitMutation.isPending || 
               voteMutation.isPending || anchorMutation.isPending || transferMutation.isPending
  }
}

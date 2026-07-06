"use client"

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  notificationsApi,
  type UpdateNotificationPreferencesPayload,
} from "../api/notifications"
import { ApiError } from "../api/types"
import { toast } from "sonner"

const PREFERENCES_KEY = ["notification-preferences"] as const

export function useNotificationPreferences() {
  const qc = useQueryClient()

  const query = useQuery({
    queryKey: PREFERENCES_KEY,
    queryFn: notificationsApi.getPreferences,
  })

  const mutation = useMutation({
    mutationFn: (payload: UpdateNotificationPreferencesPayload) =>
      notificationsApi.updatePreferences(payload),
    onSuccess: (preferences) => {
      qc.setQueryData(PREFERENCES_KEY, preferences)
      toast.success("Préférences d'alerte enregistrées.")
    },
    onError: (error) => {
      toast.error(
        error instanceof ApiError
          ? error.message
          : "Impossible d'enregistrer vos préférences. Veuillez réessayer.",
      )
    },
  })

  return {
    preferences: query.data,
    isLoading: query.isLoading,
    save: mutation.mutate,
    isSaving: mutation.isPending,
  }
}

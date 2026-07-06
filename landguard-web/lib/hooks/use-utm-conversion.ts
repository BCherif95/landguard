"use client"

import { useMutation } from "@tanstack/react-query"
import { geoApi, type UtmToGeoJsonPayload } from "../api/geo"
import type { PolygonGeometry } from "../api/parcels"
import { ApiError } from "../api/types"
import { toast } from "sonner"

export function useUtmConversion() {
  const mutation = useMutation({
    mutationFn: (payload: UtmToGeoJsonPayload) => geoApi.utmToGeoJson(payload),
  })

  const convert = async (payload: UtmToGeoJsonPayload): Promise<PolygonGeometry | null> => {
    try {
      return await mutation.mutateAsync(payload)
    } catch (error) {
      toast.error(
        "Impossible de convertir ces coordonnées. Vérifiez la zone UTM et les valeurs saisies.",
        {
          description: error instanceof ApiError ? error.message : undefined,
        },
      )
      return null
    }
  }

  return {
    convert,
    isLoading: mutation.isPending,
    error: mutation.error,
  }
}

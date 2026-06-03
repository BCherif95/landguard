"use client"

import { useVerifyParcel as useVerifyParcelMutation } from "./use-parcels";
import { toast } from "sonner";

export function useVerifyParcel() {
  const verifyMutation = useVerifyParcelMutation();

  const verify = async (id: string, newTrustScore: number) => {
    try {
      await verifyMutation.mutateAsync({ id, newTrustScore });
      toast.success("Parcelle vérifiée avec succès.");
    } catch (error) {
      toast.error("Échec de la vérification.");
      throw error;
    }
  };

  return {
    verify,
    isLoading: verifyMutation.isPending,
    error: verifyMutation.error,
  };
}

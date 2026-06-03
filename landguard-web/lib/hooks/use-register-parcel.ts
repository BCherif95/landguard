"use client"

import { useRegisterParcel as useRegisterParcelMutation } from "./use-parcels";
import { useRegistrationFlowStore } from "../store/registration-flow.store";
import { toast } from "sonner";

export function useRegisterParcel() {
  const registerMutation = useRegisterParcelMutation();
  const { formData, reset, closeDrawer } = useRegistrationFlowStore();

  const register = async () => {
    if (!formData.geometry) {
      toast.error("Veuillez définir la géométrie du terrain.");
      return;
    }

    const payload = {
      ...formData,
      documents: formData.documents
        .filter(doc => doc.status === 'UPLOADED')
        .map(doc => ({
          type: doc.type,
          label: doc.label,
          storageKey: doc.storageKey!,
          fileName: doc.fileName!
        }))
    };

    try {
      await registerMutation.mutateAsync(payload as any);
      toast.success("Parcelle enregistrée avec succès au registre.");
      reset();
      closeDrawer();
    } catch (error) {
      toast.error("Échec de l'enregistrement. Vérifiez les informations.");
      throw error;
    }
  };

  return {
    register,
    isLoading: registerMutation.isPending,
    error: registerMutation.error,
  };
}

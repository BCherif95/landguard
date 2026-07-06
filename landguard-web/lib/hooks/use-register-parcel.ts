"use client"

import { useRouter } from "next/navigation";
import { useRegisterParcel as useRegisterParcelMutation } from "./use-parcels";
import { useRegistrationFlowStore } from "../store/registration-flow.store";
import { useParcelUIStore } from "../store/parcel-ui.store";
import type { RegisterParcelPayload } from "../api/parcels";
import { toast } from "sonner";

export function useRegisterParcel() {
  const router = useRouter();
  const registerMutation = useRegisterParcelMutation();
  const { formData, reset, closeDrawer } = useRegistrationFlowStore();
  const openDetails = useParcelUIStore((s) => s.openDetails);

  const register = async () => {
    if (!formData.geometry) {
      toast.error("Veuillez définir la géométrie du terrain.");
      return;
    }

    const payload: RegisterParcelPayload = {
      reference: formData.reference,
      name: formData.name,
      regionLabel: formData.regionLabel,
      ownerLabel: formData.ownerLabel,
      areaHectares: formData.areaHectares,
      estimatedValueXof: formData.estimatedValueXof,
      geometry: formData.geometry,
      documents: formData.documents
        .filter((doc) => doc.status === "UPLOADED" && doc.storageKey && doc.fileName)
        .map((doc) => ({
          type: doc.type,
          label: doc.label,
          storageKey: doc.storageKey!,
          fileName: doc.fileName!,
        })),
    };

    try {
      const created = await registerMutation.mutateAsync(payload);
      toast.success("Parcelle enregistrée avec succès au registre.");
      reset();
      closeDrawer();
      // Land the user on the register with the new parcel's full record open,
      // never on a neutral page.
      router.push("/registre");
      openDetails(created.id);
      return created;
    } catch (error) {
      toast.error("L'enregistrement a échoué. Vérifiez votre connexion et réessayez.");
      throw error;
    }
  };

  return {
    register,
    isLoading: registerMutation.isPending,
    error: registerMutation.error,
  };
}

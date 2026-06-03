"use client"

import { useParcel } from "./use-parcels";
import { useParcelUIStore } from "../store/parcel-ui.store";

export function useParcelDetails() {
  const { selectedParcelId, isDetailsOpen, activeTab, closeDetails, setActiveTab } = useParcelUIStore();
  const { data: parcel, isLoading, error } = useParcel(selectedParcelId);

  return {
    parcel,
    isLoading,
    error,
    isOpen: isDetailsOpen,
    activeTab,
    close: closeDetails,
    setTab: setActiveTab,
  };
}

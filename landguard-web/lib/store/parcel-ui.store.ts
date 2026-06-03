import { create } from 'zustand';

interface ParcelUIState {
  selectedParcelId: string | null;
  isDetailsOpen: boolean;
  activeTab: string;
  setSelectedParcel: (id: string | null) => void;
  openDetails: (id: string) => void;
  closeDetails: () => void;
  setActiveTab: (tab: string) => void;
}

export const useParcelUIStore = create<ParcelUIState>((set) => ({
  selectedParcelId: null,
  isDetailsOpen: false,
  activeTab: 'overview',
  setSelectedParcel: (id) => set({ selectedParcelId: id }),
  openDetails: (id) => set({ selectedParcelId: id, isDetailsOpen: true, activeTab: 'overview' }),
  closeDetails: () => set({ isDetailsOpen: false, selectedParcelId: null }),
  setActiveTab: (tab) => set({ activeTab: tab }),
}));

import { create } from 'zustand';
import { PolygonGeometry } from '../api/parcels';
import { generateCadastralReference } from '../domain/cadastral-reference';

interface RegistrationStep {
  id: number;
  label: string;
}

export const REGISTRATION_STEPS: RegistrationStep[] = [
  { id: 1, label: 'Informations' },
  { id: 2, label: 'Géographie' },
  { id: 3, label: 'Documents' },
  { id: 4, label: 'Validation' },
  { id: 5, label: 'Confirmation' },
];

export interface LandDocument {
  type: 'TF' | 'PLAN' | 'ID' | 'CESSION' | 'TAX';
  label: string;
  status: 'PENDING' | 'UPLOADED' | 'VERIFIED';
  fileName?: string;
  storageKey?: string;
  file?: File;
}

interface RegistrationFlowState {
  currentStep: number;
  isOpen: boolean;
  formData: {
    reference: string;
    name: string;
    regionLabel: string;
    ownerLabel: string;
    areaHectares: number;
    estimatedValueXof: number;
    geometry?: PolygonGeometry;
    documents: LandDocument[];
  };
  setStep: (step: number) => void;
  nextStep: () => void;
  prevStep: () => void;
  openDrawer: () => void;
  closeDrawer: () => void;
  updateFormData: (data: Partial<RegistrationFlowState['formData']>) => void;
  updateDocument: (type: LandDocument['type'], data: Partial<LandDocument>) => void;
  reset: () => void;
}

const DEFAULT_DOCUMENTS: LandDocument[] = [
  { type: 'TF', label: 'Titre Foncier', status: 'PENDING' },
  { type: 'PLAN', label: 'Plan de bornage', status: 'PENDING' },
  { type: 'ID', label: 'Pièce d\'identité (NINA)', status: 'PENDING' },
  { type: 'CESSION', label: 'Acte de cession', status: 'PENDING' },
];

const createInitialFormData = () => ({
  reference: generateCadastralReference(),
  name: '',
  regionLabel: '',
  ownerLabel: '',
  areaHectares: 1.0,
  estimatedValueXof: 10000000,
  documents: [...DEFAULT_DOCUMENTS],
});

export const useRegistrationFlowStore = create<RegistrationFlowState>((set) => ({
  currentStep: 1,
  isOpen: false,
  formData: createInitialFormData(),
  setStep: (step) => set({ currentStep: step }),
  nextStep: () => set((state) => ({ currentStep: Math.min(state.currentStep + 1, REGISTRATION_STEPS.length) })),
  prevStep: () => set((state) => ({ currentStep: Math.max(state.currentStep - 1, 1) })),
  openDrawer: () => set({ isOpen: true }),
  closeDrawer: () => set({ isOpen: false }),
  updateFormData: (data) => set((state) => ({ formData: { ...state.formData, ...data } })),
  updateDocument: (type, data) => set((state) => ({
    formData: {
      ...state.formData,
      documents: state.formData.documents.map(doc => doc.type === type ? { ...doc, ...data } : doc)
    }
  })),
  reset: () => set({
    currentStep: 1,
    formData: createInitialFormData(),
  }),
}));
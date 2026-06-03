import type {
  Parcel,
  AnomalyEvent,
  ActivityEntry,
  BlockchainEvent,
  Heir,
} from "./types"

// All UI strings here are FRENCH (user-facing). Code/identifiers remain English.

export const parcels: Parcel[] = [
  {
    id: "p-001",
    reference: "BSL-ML-2024-00187",
    name: "Domaine de Kati Sud",
    owner: "Famille Diarra",
    region: "Koulikoro, Mali",
    area: 4.8,
    status: "certified",
    risk: "low",
    riskScore: 12,
    trustScore: 96,
    estimatedValue: 184_500_000,
    lastVerifiedAt: "Il y a 2 heures",
    blockchainHash: "0x7af3…b21c",
    coordinates: { lat: 12.7406, lng: -8.0708 },
    polygon: [
      { x: 0.18, y: 0.32 },
      { x: 0.34, y: 0.28 },
      { x: 0.38, y: 0.46 },
      { x: 0.22, y: 0.5 },
    ],
  },
  {
    id: "p-002",
    reference: "BSL-SN-2024-00091",
    name: "Parcelle Almadies",
    owner: "Mme Aïcha Ndiaye",
    region: "Dakar, Sénégal",
    area: 0.6,
    status: "certified",
    risk: "high",
    riskScore: 74,
    trustScore: 68,
    estimatedValue: 92_000_000,
    lastVerifiedAt: "Il y a 18 minutes",
    blockchainHash: "0x91ee…4d77",
    coordinates: { lat: 14.7435, lng: -17.5193 },
    polygon: [
      { x: 0.5, y: 0.18 },
      { x: 0.66, y: 0.22 },
      { x: 0.68, y: 0.36 },
      { x: 0.52, y: 0.34 },
    ],
  },
  {
    id: "p-003",
    reference: "BSL-CI-2024-00342",
    name: "Plantation Bingerville",
    owner: "Coopérative Akwaba",
    region: "Abidjan, Côte d'Ivoire",
    area: 12.3,
    status: "disputed",
    risk: "critical",
    riskScore: 88,
    trustScore: 42,
    estimatedValue: 410_000_000,
    lastVerifiedAt: "Il y a 6 minutes",
    blockchainHash: "0x3c12…ae09",
    coordinates: { lat: 5.3548, lng: -3.8967 },
    polygon: [
      { x: 0.62, y: 0.58 },
      { x: 0.84, y: 0.52 },
      { x: 0.88, y: 0.78 },
      { x: 0.66, y: 0.82 },
    ],
  },
  {
    id: "p-004",
    reference: "BSL-BF-2024-00056",
    name: "Terrain Ouaga 2000",
    owner: "M. Issa Compaoré",
    region: "Ouagadougou, Burkina Faso",
    area: 0.3,
    status: "certified",
    risk: "low",
    riskScore: 8,
    trustScore: 98,
    estimatedValue: 38_000_000,
    lastVerifiedAt: "Il y a 1 jour",
    blockchainHash: "0xaa01…ff45",
    coordinates: { lat: 12.3357, lng: -1.5417 },
    polygon: [
      { x: 0.14, y: 0.66 },
      { x: 0.28, y: 0.64 },
      { x: 0.3, y: 0.78 },
      { x: 0.16, y: 0.8 },
    ],
  },
  {
    id: "p-005",
    reference: "BSL-TG-2024-00120",
    name: "Domaine Agbalépédogan",
    owner: "Héritage Kpodzro",
    region: "Lomé, Togo",
    area: 2.1,
    status: "submitted",
    risk: "medium",
    riskScore: 46,
    trustScore: 78,
    estimatedValue: 64_000_000,
    lastVerifiedAt: "Il y a 4 heures",
    blockchainHash: "0x5b8d…1234",
    coordinates: { lat: 6.1725, lng: 1.2314 },
    polygon: [
      { x: 0.42, y: 0.62 },
      { x: 0.54, y: 0.6 },
      { x: 0.56, y: 0.72 },
      { x: 0.44, y: 0.74 },
    ],
  },
]

export const anomalies: AnomalyEvent[] = [
  {
    id: "a-001",
    parcelId: "p-003",
    type: "construction",
    severity: "critical",
    detectedAt: "Il y a 6 min",
    confidence: 94,
    position: { x: 0.74, y: 0.66 },
    description: "Construction non déclarée détectée — fondations en cours.",
  },
  {
    id: "a-002",
    parcelId: "p-002",
    type: "vehicle",
    severity: "warning",
    detectedAt: "Il y a 18 min",
    confidence: 81,
    position: { x: 0.6, y: 0.28 },
    description: "Engin lourd identifié sur le périmètre nord.",
  },
  {
    id: "a-003",
    parcelId: "p-005",
    type: "earthworks",
    severity: "warning",
    detectedAt: "Il y a 1 h",
    confidence: 73,
    position: { x: 0.5, y: 0.68 },
    description: "Mouvements de terrassement signalés à la frontière sud-est.",
  },
  {
    id: "a-004",
    parcelId: "p-001",
    type: "boundary-shift",
    severity: "info",
    detectedAt: "Il y a 2 h",
    confidence: 62,
    position: { x: 0.3, y: 0.42 },
    description: "Variation mineure de bornage — analyse en cours.",
  },
]

export const activity: ActivityEntry[] = [
  {
    id: "ac-001",
    parcelId: "p-003",
    type: "alert",
    actor: "IA Surveillance",
    message:
      "Activité critique détectée sur Plantation Bingerville. Construction non autorisée.",
    timestamp: "11:42",
  },
  {
    id: "ac-002",
    parcelId: "p-002",
    type: "verification",
    actor: "Notaire Maître Sow",
    message: "Vérification cadastrale validée pour Parcelle Almadies.",
    timestamp: "10:18",
  },
  {
    id: "ac-003",
    parcelId: "p-001",
    type: "blockchain",
    actor: "Réseau Boussole",
    message: "Hash de certification ancré — bloc #284 119.",
    timestamp: "09:05",
  },
  {
    id: "ac-004",
    parcelId: "p-004",
    type: "document",
    actor: "OCR Boussole",
    message: "Titre foncier scanné et authentifié — score 99/100.",
    timestamp: "Hier",
  },
  {
    id: "ac-005",
    parcelId: "p-005",
    type: "inspection",
    actor: "Agent cadastral",
    message: "Visite terrain programmée pour le 14 mars.",
    timestamp: "Hier",
  },
]

export const blockchainEvents: BlockchainEvent[] = [
  {
    id: "b-001",
    parcelId: "p-001",
    hash: "0x7af3a91c8d2fb0e7f4a5c89b21c",
    type: "certification",
    actor: "Famille Diarra",
    timestamp: "12 mars 2026 — 09:05",
    blockNumber: 284_119,
    description: "Certification numérique ancrée sur la chaîne Boussole.",
  },
  {
    id: "b-002",
    parcelId: "p-002",
    hash: "0x91ee44d77ab3cc12fe09a4d77",
    type: "verification",
    actor: "Maître Sow, Notaire",
    timestamp: "11 mars 2026 — 16:42",
    blockNumber: 283_881,
    description: "Vérification cadastrale signée par autorité notariale.",
  },
  {
    id: "b-003",
    parcelId: "p-001",
    hash: "0x6ac1d92f4b1c8a7e5f09b88e",
    type: "transfer",
    actor: "Famille Diarra",
    timestamp: "5 mars 2026 — 14:11",
    blockNumber: 281_204,
    description: "Transfert partiel à hauteur de 12% vers héritier validé.",
  },
  {
    id: "b-004",
    parcelId: "p-003",
    hash: "0x3c1244ae09f0e1d8b2c5a7f3",
    type: "registration",
    actor: "Coopérative Akwaba",
    timestamp: "1er mars 2026 — 08:30",
    blockNumber: 279_044,
    description: "Inscription initiale au registre foncier numérique.",
  },
  {
    id: "b-005",
    parcelId: "p-004",
    hash: "0xaa01ff45d2c3b9e7a4180cdd",
    type: "update",
    actor: "Agent cadastral BF",
    timestamp: "26 février 2026 — 11:09",
    blockNumber: 276_512,
    description: "Mise à jour des coordonnées GPS — précision 0,4 m.",
  },
]

export const heirs: Heir[] = [
  {
    id: "h-001",
    name: "Mariam Diarra",
    relation: "Fille aînée",
    share: 35,
    validated: true,
    avatarColor: "var(--emerald)",
  },
  {
    id: "h-002",
    name: "Boubacar Diarra",
    relation: "Fils",
    share: 30,
    validated: true,
    avatarColor: "var(--gold)",
  },
  {
    id: "h-003",
    name: "Awa Diarra",
    relation: "Fille",
    share: 25,
    validated: true,
    avatarColor: "var(--info)",
  },
  {
    id: "h-004",
    name: "Sékou Diarra",
    relation: "Fils cadet",
    share: 10,
    validated: false,
    avatarColor: "var(--warning)",
  },
]

/** Monthly AI-derived valuation trajectory (XOF). */
export const valuationHistory: { month: string; value: number }[] = [
  { month: "Mai 25", value: 84_000_000 },
  { month: "Juin", value: 85_200_000 },
  { month: "Juil", value: 86_400_000 },
  { month: "Août", value: 88_100_000 },
  { month: "Sept", value: 89_700_000 },
  { month: "Oct", value: 91_500_000 },
  { month: "Nov", value: 93_200_000 },
  { month: "Déc 25", value: 95_400_000 },
  { month: "Jan 26", value: 97_800_000 },
  { month: "Fév", value: 100_200_000 },
  { month: "Mar", value: 103_500_000 },
  { month: "Avr", value: 106_800_000 },
  { month: "Mai 26", value: 110_400_000 },
]

/** UEMOA bank partners offering credit secured by tokenized parcels. */
export const bankPartners: { name: string; code: string; rate: number; ltv: number }[] = [
  { name: "Société Générale CI", code: "SGC", rate: 6.4, ltv: 65 },
  { name: "Ecobank", code: "ECO", rate: 6.9, ltv: 60 },
  { name: "NSIA Banque", code: "NSI", rate: 7.2, ltv: 70 },
  { name: "BICICI", code: "BIC", rate: 6.8, ltv: 65 },
  { name: "Orabank", code: "ORA", rate: 7.5, ltv: 55 },
]

/** Format an XOF currency amount in French short form. */
export function formatXof(value: number): string {
  if (value >= 1_000_000_000) {
    return `${(value / 1_000_000_000).toFixed(2)} Md FCFA`
  }
  if (value >= 1_000_000) {
    return `${(value / 1_000_000).toFixed(1)} M FCFA`
  }
  if (value >= 1_000) {
    return `${(value / 1_000).toFixed(0)} k FCFA`
  }
  return `${value} FCFA`
}

import { api } from "./client"

/** Anchored blockchain record as persisted by the backend. */
export interface BlockchainRecord {
  id: string
  entityType: string
  entityId: string
  hash: string
  previousHash: string | null
  anchoredAt: string
  network: string
  transactionId: string
}

export const blockchainApi = {
  listRecords: async (params?: { entityId?: string; limit?: number }): Promise<BlockchainRecord[]> => {
    const { data } = await api.get<BlockchainRecord[]>("/blockchain/records", { params })
    return data
  },
}

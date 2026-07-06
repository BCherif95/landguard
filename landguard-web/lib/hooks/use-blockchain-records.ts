"use client"

import { useQuery } from "@tanstack/react-query"
import { blockchainApi } from "@/lib/api/blockchain"

export function useBlockchainRecords(
  params?: { entityId?: string; limit?: number },
  options?: { enabled?: boolean },
) {
  return useQuery({
    queryKey: ["blockchain", "records", params ?? null] as const,
    queryFn: () => blockchainApi.listRecords(params),
    enabled: options?.enabled ?? true,
  })
}

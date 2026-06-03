import { cn } from "@/lib/utils"
import { resolveStatusMeta, resolveRiskMeta } from "@/lib/utils/safe-resolvers"
import type { ParcelStatus, RiskLevel } from "@/lib/api/parcels"

export function StatusPill({ status }: { status?: ParcelStatus | string | null }) {
  const meta = resolveStatusMeta(status)
  const Icon = meta.icon

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-md border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.16em] transition-colors",
        meta.tone,
      )}
    >
      <Icon className="h-3 w-3 opacity-80" />
      {meta.label}
    </span>
  )
}

export function RiskBadge({ risk, score }: { risk?: RiskLevel | string | null; score?: number }) {
  const meta = resolveRiskMeta(risk)
  const Icon = meta.icon

  return (
    <div
      className={cn(
        "flex shrink-0 flex-col items-end rounded-lg border px-2 py-1 transition-all",
        meta.tone,
      )}
    >
      <div className="flex items-center gap-1 font-mono text-[9px] uppercase tracking-[0.18em] opacity-70">
        <Icon className="h-2.5 w-2.5" />
        Risque
      </div>
      <div className="font-display text-sm font-medium leading-tight">
        {meta.label}
        {score !== undefined && (
          <span className="ml-1 font-mono text-[10px] opacity-60">
            {score}
          </span>
        )}
      </div>
    </div>
  )
}

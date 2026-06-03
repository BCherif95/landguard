import { resolveSeverityMeta } from "@/lib/utils/safe-resolvers"
import { cn } from "@/lib/utils"

interface AlertSeverityBadgeProps {
  severity?: string | null
  className?: string
}

export function AlertSeverityBadge({ severity, className }: AlertSeverityBadgeProps) {
  const meta = resolveSeverityMeta(severity)

  return (
    <span
      className={cn(
        "inline-flex items-center rounded border px-1.5 py-0.5 font-mono text-[9px] font-bold uppercase tracking-wider transition-colors",
        meta.tone,
        severity === 'CRITICAL' && "animate-pulse shadow-[0_0_10px_rgba(239,68,68,0.3)]",
        className
      )}
    >
      {meta.label}
    </span>
  )
}

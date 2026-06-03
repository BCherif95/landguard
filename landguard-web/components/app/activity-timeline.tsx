import { resolveActivityMeta } from "@/lib/utils/safe-resolvers"
import type { ActivityEntry } from "@/lib/types"
import { cn } from "@/lib/utils"

interface ActivityTimelineProps {
  entries: ActivityEntry[]
  className?: string
}

export function ActivityTimeline({ entries, className }: ActivityTimelineProps) {
  return (
    <div className={cn("rounded-xl border border-border bg-card/60", className)}>
      <div className="border-b border-border/60 px-4 py-3">
        <h3 className="font-display text-sm font-medium text-foreground">
          Activité récente
        </h3>
        <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Journal opérationnel
        </p>
      </div>
      <ol className="relative px-4 py-4">
        <span
          aria-hidden
          className="absolute left-[1.6rem] top-4 bottom-4 w-px bg-border"
        />
        {(entries || []).map((e) => {
          const meta = resolveActivityMeta(e.type)
          const Icon = meta.icon
          return (
            <li key={e.id} className="relative flex gap-3 pb-4 last:pb-0">
              <div
                className={cn(
                  "relative z-10 flex h-7 w-7 shrink-0 items-center justify-center rounded-full ring-4 ring-card transition-all",
                  meta.tone,
                )}
              >
                <Icon className="h-3.5 w-3.5" />
              </div>
              <div className="min-w-0 flex-1 pt-0.5">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-medium text-foreground">
                    {e.actor}
                  </span>
                  <span className="font-mono text-[10px] text-muted-foreground">
                    {e.timestamp}
                  </span>
                </div>
                <p className="mt-0.5 text-sm leading-snug text-muted-foreground">
                  {e.message}
                </p>
              </div>
            </li>
          )
        })}
      </ol>
    </div>
  )
}

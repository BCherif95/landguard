import { type LucideIcon, TrendingUp, TrendingDown } from "lucide-react"
import { cn } from "@/lib/utils"

interface KpiCardProps {
  label: string
  value: string
  delta?: string
  trend?: "up" | "down" | "flat"
  icon?: LucideIcon
  accent?: "emerald" | "gold" | "danger" | "info"
  sparkline?: number[]
}

const ACCENT_COLOR: Record<NonNullable<KpiCardProps["accent"]>, string> = {
  emerald: "var(--emerald)",
  gold: "var(--gold)",
  danger: "var(--danger)",
  info: "var(--info)",
}

export function KpiCard({
  label,
  value,
  delta,
  trend = "flat",
  icon: Icon,
  accent = "emerald",
  sparkline,
}: KpiCardProps) {
  const color = ACCENT_COLOR[accent]
  const TrendIcon = trend === "down" ? TrendingDown : TrendingUp

  return (
    <div className="relative overflow-hidden rounded-xl border border-border bg-card/60 p-4">
      <div className="flex items-center justify-between">
        <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          {label}
        </span>
        {Icon && (
          <div
            className="flex h-7 w-7 items-center justify-center rounded-md border border-border"
            style={{
              background: `color-mix(in oklab, ${color} 14%, transparent)`,
              color,
            }}
          >
            <Icon className="h-3.5 w-3.5" />
          </div>
        )}
      </div>
      <div className="mt-3 flex items-end justify-between gap-3">
        <div>
          <div className="font-display text-3xl font-medium tracking-tight text-foreground">
            {value}
          </div>
          {delta && (
            <div
              className={cn(
                "mt-1 flex items-center gap-1 text-xs",
                trend === "up" && "text-emerald",
                trend === "down" && "text-danger",
                trend === "flat" && "text-muted-foreground",
              )}
            >
              {trend !== "flat" && <TrendIcon className="h-3 w-3" />}
              {delta}
            </div>
          )}
        </div>
        {sparkline && sparkline.length > 1 && (
          <Sparkline values={sparkline} color={color} />
        )}
      </div>
    </div>
  )
}

function Sparkline({ values, color }: { values: number[]; color: string }) {
  const max = Math.max(...values)
  const min = Math.min(...values)
  const range = max - min || 1
  const w = 80
  const h = 32
  const step = w / (values.length - 1)
  const path = values
    .map((v, i) => {
      const x = i * step
      const y = h - ((v - min) / range) * h
      return `${i === 0 ? "M" : "L"}${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(" ")

  return (
    <svg width={w} height={h} className="shrink-0" aria-hidden>
      <defs>
        <linearGradient id={`sg-${color}`} x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.3" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={`${path} L${w},${h} L0,${h} Z`} fill={`url(#sg-${color})`} />
      <path d={path} fill="none" stroke={color} strokeWidth="1.5" />
    </svg>
  )
}

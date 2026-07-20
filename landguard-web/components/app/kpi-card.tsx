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
    <div className="group relative overflow-hidden rounded-2xl border border-border bg-card p-5 shadow-card transition-all duration-300 hover:-translate-y-0.5 hover:shadow-card-md">
      <span
        aria-hidden
        className="absolute inset-x-0 top-0 h-0.5 opacity-70"
        style={{ background: `linear-gradient(90deg, ${color}, transparent)` }}
      />
      <div className="flex items-center justify-between">
        <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          {label}
        </span>
        {Icon && (
          <div
            className="flex h-9 w-9 items-center justify-center rounded-xl transition-transform duration-300 group-hover:scale-105"
            style={{
              background: `color-mix(in oklab, ${color} 12%, transparent)`,
              color,
            }}
          >
            <Icon className="h-4 w-4" />
          </div>
        )}
      </div>
      <div className="mt-3 flex items-end justify-between gap-3">
        <div>
          <div className="font-display text-3xl font-semibold tracking-tight text-foreground tabular-nums">
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

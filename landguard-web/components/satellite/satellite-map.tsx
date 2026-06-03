"use client"

import { motion } from "framer-motion"
import { cn } from "@/lib/utils"
import type { Parcel, AnomalyEvent } from "@/lib/types"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { resolveRiskMeta } from "@/lib/utils/safe-resolvers"

interface SatelliteMapProps {
  parcels: Parcel[]
  anomalies?: AnomalyEvent[]
  selectedParcelId?: string | null
  onSelectParcel?: (id: string) => void
  className?: string
  /** Show extra HUD layers (radar, heatmap, scanline). */
  intensity?: "calm" | "normal" | "high"
}

const RISK_COLOR: Record<Parcel["risk"], string> = {
  low: "var(--emerald)",
  medium: "var(--gold)",
  high: "var(--warning)",
  critical: "var(--danger)",
}

const SEVERITY_COLOR: Record<AnomalyEvent["severity"], string> = {
  info: "var(--info)",
  warning: "var(--warning)",
  critical: "var(--danger)",
}

/**
 * Stylized satellite "command-center" map.
 * Renders parcels as polygons over a generated terrain composition with
 * graticule, radar sweep, anomaly pings and a scan line — Palantir/ESRI vibe.
 */
export function SatelliteMap({
  parcels,
  anomalies = [],
  selectedParcelId,
  onSelectParcel,
  className,
  intensity = "normal",
}: SatelliteMapProps) {
  return (
    <AppErrorBoundary name="Carte Satellite Stylisée">
      <div
        className={cn(
          "relative isolate overflow-hidden rounded-xl border border-border bg-carbon",
          className,
        )}
      >
        {/* ... */}
      {/* Base satellite "imagery" — generated via layered radial gradients */}
      <div
        aria-hidden
        className="absolute inset-0"
        style={{
          background: `
            radial-gradient(ellipse 60% 40% at 25% 35%, color-mix(in oklab, var(--emerald) 18%, transparent) 0%, transparent 60%),
            radial-gradient(ellipse 50% 30% at 75% 65%, color-mix(in oklab, var(--gold) 14%, transparent) 0%, transparent 55%),
            radial-gradient(ellipse 80% 60% at 50% 50%, color-mix(in oklab, #1a3a4a 60%, transparent) 0%, transparent 70%),
            linear-gradient(160deg, #07101a 0%, #0c1b2a 50%, #081522 100%)
          `,
        }}
      />

      {/* Topographic noise layer */}
      <div
        aria-hidden
        className="absolute inset-0 mix-blend-soft-light opacity-50"
        style={{
          backgroundImage: `
            repeating-radial-gradient(circle at 22% 38%, transparent 0 12px, color-mix(in oklab, var(--emerald) 12%, transparent) 12px 13px),
            repeating-radial-gradient(circle at 70% 60%, transparent 0 18px, color-mix(in oklab, var(--gold) 10%, transparent) 18px 19px),
            repeating-radial-gradient(circle at 50% 50%, transparent 0 26px, color-mix(in oklab, #ffffff 4%, transparent) 26px 27px)
          `,
        }}
      />

      {/* Graticule grid */}
      <div aria-hidden className="absolute inset-0 bg-grid opacity-40" />
      <div aria-hidden className="absolute inset-0 bg-grid-fine opacity-30" />

      {/* Radar sweep */}
      {intensity !== "calm" && (
        <div
          aria-hidden
          className="absolute left-1/2 top-1/2 h-[140%] w-[140%] -translate-x-1/2 -translate-y-1/2 animate-radar"
          style={{
            background:
              "conic-gradient(from 0deg, transparent 0deg, color-mix(in oklab, var(--emerald) 18%, transparent) 30deg, transparent 60deg)",
            mixBlendMode: "screen",
          }}
        />
      )}

      {/* Scan line */}
      {intensity === "high" && (
        <div
          aria-hidden
          className="pointer-events-none absolute inset-x-0 h-12 animate-scan"
          style={{
            background:
              "linear-gradient(to bottom, transparent, color-mix(in oklab, var(--emerald) 22%, transparent), transparent)",
          }}
        />
      )}

      {/* SVG overlay for parcels and anomalies */}
      <svg
        viewBox="0 0 100 100"
        preserveAspectRatio="none"
        className="absolute inset-0 h-full w-full"
        role="img"
        aria-label="Vue cartographique satellite des parcelles surveillées"
      >
        <defs>
          <linearGradient id="parcelFill" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="var(--emerald)" stopOpacity="0.35" />
            <stop offset="100%" stopColor="var(--emerald)" stopOpacity="0.1" />
          </linearGradient>
          <pattern
            id="hatch"
            patternUnits="userSpaceOnUse"
            width="2"
            height="2"
            patternTransform="rotate(45)"
          >
            <line
              x1="0"
              y1="0"
              x2="0"
              y2="2"
              stroke="var(--danger)"
              strokeWidth="0.6"
              strokeOpacity="0.55"
            />
          </pattern>
        </defs>

        {parcels.map((p) => {
          const isSelected = p.id === selectedParcelId
          const riskMeta = resolveRiskMeta(p.risk)
          const stroke = RISK_COLOR[p.risk as keyof typeof RISK_COLOR] || "var(--emerald)"
          
          if (!p.polygon || p.polygon.length === 0) return null

          const points = p.polygon
            .map((pt) => `${(pt.x || 0) * 100},${(pt.y || 0) * 100}`)
            .join(" ")
          
          const polygonLength = p.polygon.length || 1
          const cx =
            (p.polygon.reduce((acc, pt) => acc + (pt.x || 0), 0) / polygonLength) *
            100
          const cy =
            (p.polygon.reduce((acc, pt) => acc + (pt.y || 0), 0) / polygonLength) *
            100
          
          return (
            <g
              key={p.id}
              className="cursor-pointer transition-opacity"
              onClick={() => onSelectParcel?.(p.id)}
            >
              <polygon
                points={points}
                fill={p.risk === "critical" ? "url(#hatch)" : "url(#parcelFill)"}
                stroke={stroke}
                strokeWidth={isSelected ? 0.6 : 0.35}
                strokeOpacity={isSelected ? 1 : 0.85}
                vectorEffect="non-scaling-stroke"
                style={{
                  filter: isSelected
                    ? `drop-shadow(0 0 6px ${stroke})`
                    : undefined,
                }}
              />
              {/* Centroid marker */}
              <circle
                cx={cx}
                cy={cy}
                r="0.6"
                fill={stroke}
                vectorEffect="non-scaling-stroke"
              />
              {/* Reference label */}
              <text
                x={cx}
                y={cy - 1.6}
                fontSize="1.6"
                fill="var(--foreground)"
                fontFamily="var(--font-mono)"
                textAnchor="middle"
                opacity="0.85"
              >
                {p.reference.split("-").slice(-1)[0]}
              </text>
            </g>
          )
        })}

        {/* Anomaly pings */}
        {anomalies.map((a) => (
          <g key={a.id}>
            <circle
              cx={a.position.x * 100}
              cy={a.position.y * 100}
              r="1.2"
              fill={SEVERITY_COLOR[a.severity]}
              vectorEffect="non-scaling-stroke"
            />
            <circle
              cx={a.position.x * 100}
              cy={a.position.y * 100}
              r="2.5"
              fill="none"
              stroke={SEVERITY_COLOR[a.severity]}
              strokeWidth="0.3"
              vectorEffect="non-scaling-stroke"
              opacity="0.7"
            >
              <animate
                attributeName="r"
                values="1.5;5"
                dur="2.4s"
                repeatCount="indefinite"
              />
              <animate
                attributeName="opacity"
                values="0.7;0"
                dur="2.4s"
                repeatCount="indefinite"
              />
            </circle>
          </g>
        ))}
      </svg>

      {/* Coordinate readout HUD */}
      <div className="pointer-events-none absolute inset-0 flex flex-col">
        <div className="flex items-start justify-between p-4">
          <div className="rounded-md border border-emerald/30 bg-background/40 px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald backdrop-blur">
            <motion.span
              animate={{ opacity: [1, 0.4, 1] }}
              transition={{ duration: 1.6, repeat: Infinity }}
              className="mr-1.5 inline-block h-1.5 w-1.5 rounded-full bg-emerald align-middle"
            />
            FLUX SATELLITE — TEMPS RÉEL
          </div>
          <div className="rounded-md border border-border bg-background/40 px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground backdrop-blur">
            12°44&apos;26&quot;N · 8°04&apos;15&quot;W
          </div>
        </div>
        <div className="mt-auto flex items-end justify-between p-4 font-mono text-[10px] uppercase tracking-[0.16em] text-muted-foreground">
          <div className="flex items-center gap-3">
            <span className="rounded border border-border bg-background/40 px-2 py-1 backdrop-blur">
              ZOOM 1:24 000
            </span>
            <span className="rounded border border-border bg-background/40 px-2 py-1 backdrop-blur">
              SENTINEL-2 · 10 m
            </span>
          </div>
          <span className="rounded border border-border bg-background/40 px-2 py-1 backdrop-blur">
            {parcels.length} parcelles · {anomalies.length} anomalies
          </span>
        </div>
      </div>

      {/* Vignette */}
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse at center, transparent 55%, color-mix(in oklab, #000 65%, transparent) 100%)",
        }}
      />
    </div>
    </AppErrorBoundary>
  )
}

"use client"

import { motion } from "framer-motion"

interface RiskGaugeProps {
  score: number // 0..100
  label?: string
}

export function RiskGauge({ score, label = "Niveau de risque" }: RiskGaugeProps) {
  const clamped = Math.max(0, Math.min(100, score))
  const angle = (clamped / 100) * 180 - 90
  const color =
    clamped < 30
      ? "var(--emerald)"
      : clamped < 60
        ? "var(--gold)"
        : clamped < 80
          ? "var(--warning)"
          : "var(--danger)"
  const verdict =
    clamped < 30
      ? "Faible"
      : clamped < 60
        ? "Modéré"
        : clamped < 80
          ? "Élevé"
          : "Critique"

  return (
    <div className="rounded-xl border border-border bg-card/60 p-4">
      <div className="flex items-center justify-between">
        <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          {label}
        </span>
        <span
          className="rounded-md px-2 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em]"
          style={{
            background: `color-mix(in oklab, ${color} 14%, transparent)`,
            color,
          }}
        >
          {verdict}
        </span>
      </div>

      <div className="relative mx-auto mt-3 aspect-[2/1] max-w-[240px]">
        <svg viewBox="0 0 200 110" className="h-full w-full">
          {/* Track */}
          <path
            d="M 14 100 A 86 86 0 0 1 186 100"
            fill="none"
            stroke="color-mix(in oklab, var(--foreground) 8%, transparent)"
            strokeWidth="14"
            strokeLinecap="round"
          />
          {/* Filled arc */}
          <motion.path
            d="M 14 100 A 86 86 0 0 1 186 100"
            fill="none"
            stroke={color}
            strokeWidth="14"
            strokeLinecap="round"
            initial={{ pathLength: 0 }}
            animate={{ pathLength: clamped / 100 }}
            transition={{ duration: 1, ease: "easeOut" }}
            style={{
              filter: `drop-shadow(0 0 8px color-mix(in oklab, ${color} 50%, transparent))`,
            }}
          />
          {/* Tick marks */}
          {Array.from({ length: 11 }).map((_, i) => {
            const a = (i / 10) * Math.PI - Math.PI
            const x1 = 100 + Math.cos(a) * 70
            const y1 = 100 + Math.sin(a) * 70
            const x2 = 100 + Math.cos(a) * 64
            const y2 = 100 + Math.sin(a) * 64
            return (
              <line
                key={i}
                x1={x1}
                y1={y1}
                x2={x2}
                y2={y2}
                stroke="color-mix(in oklab, var(--foreground) 25%, transparent)"
                strokeWidth="1"
              />
            )
          })}
          {/* Needle */}
          <motion.g
            initial={{ rotate: -90 }}
            animate={{ rotate: angle }}
            transition={{ duration: 1, ease: "easeOut" }}
            style={{ transformOrigin: "100px 100px" }}
          >
            <line
              x1="100"
              y1="100"
              x2="100"
              y2="28"
              stroke="var(--foreground)"
              strokeWidth="2"
              strokeLinecap="round"
            />
            <circle cx="100" cy="100" r="4" fill="var(--foreground)" />
          </motion.g>
        </svg>
        <div className="absolute inset-x-0 bottom-1 text-center">
          <div className="font-display text-3xl font-medium tracking-tight text-foreground">
            {clamped}
            <span className="text-base text-muted-foreground">/100</span>
          </div>
        </div>
      </div>
    </div>
  )
}

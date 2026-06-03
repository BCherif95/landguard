"use client"

import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts"

interface MovementChartProps {
  data?: Array<{ label: string; value: number }>
  unit?: string
  title?: string
}

const defaultData = [
  { day: "Lun", mouvements: 4, alertes: 0 },
  { day: "Mar", mouvements: 6, alertes: 1 },
  { day: "Mer", mouvements: 9, alertes: 1 },
  { day: "Jeu", mouvements: 7, alertes: 2 },
  { day: "Ven", mouvements: 12, alertes: 3 },
  { day: "Sam", mouvements: 18, alertes: 4 },
  { day: "Dim", mouvements: 14, alertes: 3 },
]

export function MovementChart({ data, unit, title }: MovementChartProps) {
  const chartData = data ? data.map(d => ({ day: d.label, value: d.value })) : defaultData

  return (
    <div className="rounded-xl border border-border bg-card/60 p-4">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-display text-sm font-medium text-foreground">
            {title || "Score de mouvement — 7 derniers jours"}
          </h3>
          <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            {unit ? `Unité : ${unit}` : "Détection IA cumulative"}
          </p>
        </div>
        <div className="flex gap-3 text-[11px]">
          <Legend color="var(--emerald)" label={data ? "Valeur" : "Mouvements"} />
          {!data && <Legend color="var(--gold)" label="Alertes" />}
        </div>
      </div>
      <div className="mt-4 h-44 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 8, right: 4, left: -16, bottom: 0 }}>
            <defs>
              <linearGradient id="grEm" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="var(--emerald)" stopOpacity={0.4} />
                <stop offset="100%" stopColor="var(--emerald)" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="grGd" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="var(--gold)" stopOpacity={0.45} />
                <stop offset="100%" stopColor="var(--gold)" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid
              stroke="color-mix(in oklab, var(--foreground) 8%, transparent)"
              strokeDasharray="3 3"
              vertical={false}
            />
            <XAxis
              dataKey="day"
              tickLine={false}
              axisLine={false}
              stroke="var(--muted-foreground)"
              fontSize={11}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              stroke="var(--muted-foreground)"
              fontSize={11}
              width={28}
            />
            <Tooltip
              cursor={{ stroke: "var(--emerald)", strokeWidth: 1, strokeDasharray: "3 3" }}
              contentStyle={{
                background: "var(--card)",
                border: "1px solid var(--border)",
                borderRadius: 10,
                fontSize: 12,
              }}
              labelStyle={{ color: "var(--muted-foreground)", fontSize: 11 }}
            />
            <Area
              type="monotone"
              dataKey={data ? "value" : "mouvements"}
              stroke="var(--emerald)"
              fill="url(#grEm)"
              strokeWidth={2}
            />
            {!data && (
              <Area
                type="monotone"
                dataKey="alertes"
                stroke="var(--gold)"
                fill="url(#grGd)"
                strokeWidth={2}
              />
            )}
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

function Legend({ color, label }: { color: string; label: string }) {
  return (
    <span className="flex items-center gap-1.5 font-mono uppercase tracking-[0.16em] text-muted-foreground">
      <span
        className="h-2 w-2 rounded-full"
        style={{ background: color, boxShadow: `0 0 8px ${color}` }}
      />
      {label}
    </span>
  )
}

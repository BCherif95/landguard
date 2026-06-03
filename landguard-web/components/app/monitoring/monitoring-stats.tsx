import { useMonitoringStore } from "@/lib/store/monitoring-store"
import { Card } from "@/components/ui/card"
import { AlertTriangle, CheckCircle, Eye, Zap } from "lucide-react"

export function MonitoringStats() {
  const { events } = useMonitoringStore()
  const evs = events || []
  
  const critical = evs.filter(e => e.severity === 'CRITICAL').length
  const high = evs.filter(e => e.severity === 'HIGH').length
  const resolved = evs.filter(e => e.resolved).length

  const stats = [
    { label: "Anomalies Critiques", value: critical, icon: AlertTriangle, color: "text-red-500", bg: "bg-red-500/10" },
    { label: "Événements à Haut Risque", value: high, icon: Zap, color: "text-orange-500", bg: "bg-orange-500/10" },
    { label: "Résolus Aujourd'hui", value: resolved, icon: CheckCircle, color: "text-green-500", bg: "bg-green-500/10" },
    { label: "Moniteurs Actifs", value: 12, icon: Eye, color: "text-primary", bg: "bg-primary/10" },
  ]

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, i) => (
        <Card key={i} className="p-4 bg-background/50 backdrop-blur-md border-primary/10">
          <div className="flex items-center gap-3">
            <div className={`p-2 rounded-lg ${stat.bg}`}>
              <stat.icon className={`w-5 h-5 ${stat.color}`} />
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold">
                {stat.label}
              </p>
              <p className="text-2xl font-black font-mono">
                {stat.value.toString().padStart(2, '0')}
              </p>
            </div>
          </div>
        </Card>
      ))}
    </div>
  )
}

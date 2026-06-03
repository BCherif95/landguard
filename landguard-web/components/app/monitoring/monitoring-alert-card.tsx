import { MonitoringEvent } from "@/lib/api/types"
import { Card } from "@/components/ui/card"
import { AlertSeverityBadge } from "./alert-severity-badge"
import { formatDistanceToNow } from "date-fns"
import { MapPin, ShieldCheck } from "lucide-react"
import { cn } from "@/lib/utils"
import { resolveMonitoringTypeMeta, resolveSeverityMeta } from "@/lib/utils/safe-resolvers"

interface MonitoringAlertCardProps {
  event: MonitoringEvent
  isSelected?: boolean
  onClick?: () => void
}

export function MonitoringAlertCard({ event, isSelected, onClick }: MonitoringAlertCardProps) {
  const typeMeta = resolveMonitoringTypeMeta(event.type)
  const severityMeta = resolveSeverityMeta(event.severity)
  const Icon = typeMeta.icon

  return (
    <Card 
      className={cn(
        "p-3 cursor-pointer transition-all hover:border-primary/50 group",
        isSelected ? "border-primary bg-primary/5 shadow-[0_0_15px_rgba(59,130,246,0.1)]" : "bg-card/50",
        event.resolved && "opacity-60"
      )}
      onClick={onClick}
    >
      <div className="flex justify-between items-start mb-2">
        <div className="flex items-center gap-2">
          <div className={cn(
            "p-1.5 rounded-md transition-colors",
            severityMeta.tone.includes('red') ? "bg-red-500/20 text-red-500" : "bg-primary/20 text-primary"
          )}>
            <Icon className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-semibold group-hover:text-primary transition-colors">
              {typeMeta.label}
            </h4>
            <span className="text-[10px] text-muted-foreground uppercase tracking-wider font-mono">
              ID: {event.id?.slice(0, 8) ?? "???"}
            </span>
          </div>
        </div>
        <AlertSeverityBadge severity={event.severity} />
      </div>

      <p className="text-xs text-muted-foreground line-clamp-2 mb-3 leading-relaxed">
        {event.description}
      </p>

      <div className="flex items-center justify-between mt-auto pt-2 border-t border-border/50">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 text-[10px] text-muted-foreground">
            <MapPin className="w-3 h-3 text-primary" />
            <span>{event.longitude?.toFixed(4) ?? "0.0000"}, {event.latitude?.toFixed(4) ?? "0.0000"}</span>
          </div>
          {event.resolved && (
            <div className="flex items-center gap-1 text-[10px] text-green-500">
              <ShieldCheck className="w-3 h-3" />
              <span>Résolu</span>
            </div>
          )}
        </div>
        <span className="text-[10px] text-muted-foreground font-mono">
          {event.detectedAt ? formatDistanceToNow(new Date(event.detectedAt), { addSuffix: true }) : "Date inconnue"}
        </span>
      </div>
    </Card>
  )
}

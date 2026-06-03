import { useMonitoringStore } from "@/lib/store/monitoring-store"
import { MonitoringAlertCard } from "./monitoring-alert-card"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Activity, Radio } from "lucide-react"
import { NoResultsState } from "@/components/app/empty-states"

export function MonitoringLiveFeed() {
  const { events, selectedEventId, setSelectedEvent } = useMonitoringStore()

  return (
    <div className="flex flex-col h-full bg-background/40 backdrop-blur-xl border rounded-xl overflow-hidden shadow-2xl">
      <div className="p-4 border-b flex items-center justify-between bg-card/30">
        <div className="flex items-center gap-2">
          <div className="relative">
            <Radio className="w-5 h-5 text-primary animate-pulse" />
            <span className="absolute -top-1 -right-1 flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
            </span>
          </div>
          <h3 className="font-bold text-sm tracking-tight uppercase">Flux de Renseignement Live</h3>
        </div>
        <div className="flex items-center gap-2 bg-primary/10 px-2 py-0.5 rounded-full border border-primary/20">
          <Activity className="w-3 h-3 text-primary" />
          <span className="text-[10px] font-bold text-primary">{(events || []).length} ACTIFS</span>
        </div>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-4 space-y-4">
          {(!events || events.length === 0) ? (
            <div className="py-12">
              <NoResultsState />
            </div>
          ) : (
            events.map((event) => (
              <MonitoringAlertCard
                key={event.id}
                event={event}
                isSelected={selectedEventId === event.id}
                onClick={() => setSelectedEvent(event.id)}
              />
            ))
          )}
        </div>
      </ScrollArea>
      
      <div className="p-3 border-t bg-card/20 text-center">
        <span className="text-[10px] text-muted-foreground uppercase tracking-[0.2em] font-mono">
          Système : Surveillance LandGuard V3 Active
        </span>
      </div>
    </div>
  )
}

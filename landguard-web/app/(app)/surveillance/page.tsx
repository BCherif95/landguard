"use client"

import { useQuery } from "@tanstack/react-query"
import { AppTopbar } from "@/components/app/app-topbar"
import { MonitoringLiveFeed } from "@/components/app/monitoring/monitoring-live-feed"
import { MonitoringMap } from "@/components/app/monitoring/monitoring-map"
import { MonitoringStats } from "@/components/app/monitoring/monitoring-stats"
import { useMonitoringStream } from "@/lib/hooks/use-monitoring-stream"
import { useMonitoringStore } from "@/lib/store/monitoring-store"
import { monitoringApi } from "@/lib/api/monitoring"
import { useEffect } from "react"
import { Radar, Scan, ShieldAlert, Cpu } from "lucide-react"
import { Card } from "@/components/ui/card"
import { MonitoringRadar } from "@/components/app/monitoring/monitoring-radar"

import { AppErrorBoundary } from "@/components/app/error-boundary"

export default function SurveillancePage() {
  const { isConnected } = useMonitoringStream()
  const { setEvents, events, selectedEventId } = useMonitoringStore()

  const selectedEvent = events.find(e => e.id === selectedEventId)

  // ... (query and effect)
  const { data: initialEvents } = useQuery({
    queryKey: ["monitoring-events"],
    queryFn: () => monitoringApi.listEvents()
  })

  useEffect(() => {
    if (initialEvents) {
      setEvents(initialEvents)
    }
  }, [initialEvents, setEvents])

  return (
    <AppErrorBoundary name="Centre de Surveillance">
      <AppTopbar
        title="Centre de Commandement de Surveillance Live"
        subtitle={isConnected ? "CONNECTÉ · Renseignement satellite en temps réel actif" : "CONNEXION · Initialisation des liaisons orbitales..."}
      />
      
      <div className="flex-1 p-4 lg:p-6 space-y-6 bg-[#050505] text-white overflow-hidden flex flex-col">
        {/* Global Monitoring Header */}
        <MonitoringStats />

        <div className="grid gap-6 lg:grid-cols-[1fr_400px] flex-1 min-h-0">
          {/* Main Intelligence Viewport */}
          <div className="flex flex-col gap-6 min-h-0">
            <div className="relative flex-1 min-h-[500px]">
              <MonitoringMap />
              
              {/* Cinematic Corner Accents */}
              <div className="absolute top-0 left-0 w-16 h-16 border-t-2 border-l-2 border-primary/40 rounded-tl-xl pointer-events-none" />
              <div className="absolute top-0 right-0 w-16 h-16 border-t-2 border-r-2 border-primary/40 rounded-tr-xl pointer-events-none" />
              <div className="absolute bottom-0 left-0 w-16 h-16 border-b-2 border-l-2 border-primary/40 rounded-bl-xl pointer-events-none" />
              <div className="absolute bottom-0 right-0 w-16 h-16 border-b-2 border-r-2 border-primary/40 rounded-br-xl pointer-events-none" />
            </div>

            {/* Selected Alert Detailed Intel */}
            {selectedEvent && (
              <Card className="p-6 bg-black/60 backdrop-blur-2xl border-primary/20 animate-in fade-in slide-in-from-bottom-4">
                <div className="flex flex-col md:flex-row gap-6">
                  <div className="w-full md:w-48 h-32 rounded-lg overflow-hidden border border-white/10 bg-muted relative group">
                    <img 
                      src={selectedEvent.imageUrl || ""} 
                      alt="Capture satellite" 
                      className="w-full h-full object-cover grayscale contrast-125"
                    />
                    <div className="absolute inset-0 bg-primary/20 mix-blend-overlay" />
                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/40">
                      <Scan className="w-8 h-8 text-white animate-pulse" />
                    </div>
                  </div>
                  
                  <div className="flex-1 space-y-4">
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="text-xl font-black uppercase tracking-tighter flex items-center gap-2">
                          <ShieldAlert className="w-5 h-5 text-primary" />
                          {selectedEvent.type?.replace('_', ' ') ?? "ALERTE"}
                        </h3>
                        <p className="text-sm text-muted-foreground font-mono">
                          SECTEUR : {selectedEvent.latitude?.toFixed(4) ?? "0.0000"}, {selectedEvent.longitude?.toFixed(4) ?? "0.0000"}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] text-muted-foreground font-mono uppercase tracking-[0.2em]">Confiance</p>
                        <p className="text-2xl font-black text-primary font-mono">{selectedEvent.confidenceScore ?? 0}%</p>
                      </div>
                    </div>
                    
                    <p className="text-sm leading-relaxed text-slate-300 border-l-2 border-primary/50 pl-4">
                      {selectedEvent.description}
                    </p>
                    
                    <div className="flex items-center gap-6">
                      <div className="flex items-center gap-2">
                        <Radar className="w-4 h-4 text-primary" />
                        <span className="text-[10px] font-mono text-muted-foreground uppercase">Source : {selectedEvent.source ?? "OSINT"}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Cpu className="w-4 h-4 text-primary" />
                        <span className="text-[10px] font-mono text-muted-foreground uppercase">Classification IA : Vérifiée</span>
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            )}
          </div>

          {/* Right Sidebar - Active Intel Stream */}
          <div className="h-full min-h-0 flex flex-col gap-6">
            <div className="h-48">
              <MonitoringRadar />
            </div>
            <MonitoringLiveFeed />
          </div>
        </div>
      </div>
    </AppErrorBoundary>
  )
}

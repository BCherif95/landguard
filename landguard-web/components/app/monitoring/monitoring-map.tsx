"use client"

import { useMemo, useState, useEffect } from "react"
import { MapContainer, TileLayer, Polygon, CircleMarker, Popup, useMap, ZoomControl, ScaleControl } from "react-leaflet"
import L from "leaflet"
import "leaflet/dist/leaflet.css"
import { useMonitoringStore } from "@/lib/store/monitoring-store"
import { useParcels } from "@/lib/hooks/use-parcels"
import { AlertCircle, Map as MapIcon, Loader2, Crosshair, Zap } from "lucide-react"
import { cn } from "@/lib/utils"
import { Parcel } from "@/lib/api/parcels"
import { resolveSeverityMeta } from "@/lib/utils/safe-resolvers"
import { AppErrorBoundary } from "@/components/app/error-boundary"

// Custom controller to handle flyTo/setView
function MapFlyController({ center }: { center: [number, number] | null }) {
  const map = useMap()
  
  useEffect(() => {
    if (center && !isNaN(center[0]) && !isNaN(center[1])) {
      map.flyTo(center, 16, {
        duration: 1.5,
        easeLinearity: 0.25
      })
    }
  }, [center, map])

  return null
}

export function MonitoringMap() {
  const { events, selectedEventId, setSelectedEvent } = useMonitoringStore()
  const { data: parcels, isLoading: isLoadingParcels, error: parcelsError } = useParcels()
  const [isReady, setIsReady] = useState(false)

  // Ensure Leaflet icons work (fix for some builds)
  useEffect(() => {
    delete (L.Icon.Default.prototype as any)._getIconUrl
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
      iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    })
    setIsReady(true)
  }, [])

  const selectedEvent = useMemo(() => 
    Array.isArray(events) ? events.find(e => e.id === selectedEventId) : null, 
  [events, selectedEventId])

  const centerPoint: [number, number] | null = useMemo(() => {
    if (selectedEvent && typeof selectedEvent.latitude === 'number' && typeof selectedEvent.longitude === 'number' && !isNaN(selectedEvent.latitude) && !isNaN(selectedEvent.longitude)) {
      return [selectedEvent.latitude, selectedEvent.longitude]
    }
    return null
  }, [selectedEvent])

  if (!isReady) return null

  return (
    <AppErrorBoundary name="Carte de Surveillance SIG">
      <div className="relative w-full h-full min-h-[500px] rounded-xl overflow-hidden border border-primary/20 shadow-2xl group bg-[#0a0a0a]">
        {isLoadingParcels && (
          <div className="absolute inset-0 z-[1000] bg-black/60 backdrop-blur-sm flex items-center justify-center text-white">
            <div className="flex flex-col items-center gap-3">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
              <p className="text-xs font-bold uppercase tracking-widest opacity-70 font-mono">Synchronisation des flux SIG...</p>
            </div>
          </div>
        )}

        {parcelsError && (
          <div className="absolute inset-0 z-[1000] bg-red-950/40 backdrop-blur-md flex items-center justify-center text-red-400 p-6 text-center border border-red-500/50">
            <div className="flex flex-col items-center gap-3">
              <AlertCircle className="w-8 h-8" />
              <p className="font-black uppercase tracking-tighter text-xl">Liaison de données corrompue</p>
              <p className="text-xs font-medium opacity-80">Le système n'a pas pu récupérer les vecteurs géographiques.</p>
            </div>
          </div>
        )}

        <MapContainer
          center={[12.6392, -7.9892]}
          zoom={12}
          zoomControl={false}
          className="h-full w-full grayscale-[0.2] contrast-[1.1] brightness-[0.9]"
        >
          <MapFlyController center={centerPoint} />
          
          <TileLayer
            attribution='&copy; <a href="https://www.esri.com/">Esri</a>'
            url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
          />
          
          {/* Label Overlay for better context */}
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}{r}.png"
            opacity={0.6}
          />

          <ZoomControl position="topright" />
          <ScaleControl position="bottomright" />

          {Array.isArray(parcels) && parcels.map((p: Parcel) => {
            if (p.geometry?.type !== "Polygon" || !Array.isArray(p.geometry.coordinates?.[0])) return null
            
            const positions = p.geometry.coordinates[0].map((coord: number[]) => [coord[1], coord[0]]) as [number, number][]
            
            return (
              <Polygon
                key={p.id}
                positions={positions}
                pathOptions={{
                  fillColor: "#3b82f6",
                  fillOpacity: 0.1,
                  color: "#3b82f6",
                  weight: 1,
                  dashArray: "4, 4"
                }}
              />
            )
          })}

          {Array.isArray(events) && events.map((event) => {
            if (typeof event.latitude !== 'number' || typeof event.longitude !== 'number' || isNaN(event.latitude) || isNaN(event.longitude)) return null
            
            const meta = resolveSeverityMeta(event.severity)
            const isSelected = selectedEventId === event.id
            const color = meta.tone.includes('red') ? "#ef4444" : meta.tone.includes('yellow') ? "#f59e0b" : "#3b82f6"
            
            return (
              <CircleMarker
                key={event.id}
                center={[event.latitude, event.longitude]}
                radius={isSelected ? 10 : 7}
                pathOptions={{
                  fillColor: color,
                  fillOpacity: 0.8,
                  color: "white",
                  weight: 2
                }}
                eventHandlers={{
                  click: () => setSelectedEvent(event.id)
                }}
              >
                <Popup className="monitoring-popup">
                  <div className="p-3 min-w-[180px] bg-[#0f0f0f] text-white rounded-lg font-mono">
                    <div className="flex items-center gap-2 mb-2 border-b border-white/10 pb-2">
                      <div className={cn("w-2 h-2 rounded-full animate-pulse", 
                        meta.tone.includes('red') ? "bg-red-500" : "bg-blue-500"
                      )} />
                      <span className="text-[10px] font-black uppercase">{event.type}</span>
                    </div>
                    <p className="text-[9px] text-zinc-400 mb-2 leading-tight">{event.description}</p>
                    <div className="flex justify-between items-center text-[8px] opacity-60">
                      <span>CONFIANCE : {event.confidenceScore}%</span>
                      <span>SOURCE : {event.source}</span>
                    </div>
                  </div>
                </Popup>
              </CircleMarker>
            )
          })}
        </MapContainer>

        {/* Cinematic HUD Elements */}
        <div className="absolute top-4 left-4 flex flex-col gap-2 z-[500] pointer-events-none">
          <div className="bg-black/80 backdrop-blur-xl border border-primary/30 p-2 rounded-lg text-[10px] font-mono text-primary flex items-center gap-3 shadow-[0_0_20px_rgba(0,0,0,0.5)]">
            <div className="flex items-center gap-1.5">
              <div className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse" />
              FLUX SATELLITE TEMPS RÉEL
            </div>
            <div className="w-px h-3 bg-white/20" />
            <div className="flex items-center gap-1.5">
              <Crosshair className="w-3 h-3" />
              {selectedEvent ? `${selectedEvent.latitude.toFixed(6)}, ${selectedEvent.longitude.toFixed(6)}` : "RECHERCHE CIBLE..."}
            </div>
          </div>
        </div>

        <div className="absolute bottom-4 left-4 bg-black/80 backdrop-blur-2xl border border-white/10 p-4 rounded-xl text-white z-[500] shadow-2xl">
          <h4 className="text-[10px] uppercase tracking-[0.2em] text-primary mb-3 font-black flex items-center gap-2">
            <Zap className="w-3 h-3" />
            Légende Tactique
          </h4>
          <div className="space-y-2.5">
            <div className="flex items-center gap-3">
              <div className="w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_10px_rgba(239,68,68,0.6)] animate-pulse" />
              <span className="text-[9px] font-bold uppercase tracking-wider font-mono">Anomalie Critique</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500 shadow-[0_0_10px_rgba(245,158,11,0.4)]" />
              <span className="text-[9px] font-bold uppercase tracking-wider font-mono">Risque Intermédiaire</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-2.5 h-2.5 border border-primary/60 bg-primary/10" />
              <span className="text-[9px] font-bold uppercase tracking-wider font-mono">Périmètre Certifié</span>
            </div>
          </div>
        </div>

        {/* Global HUD Accents */}
        <div className="absolute top-0 left-0 w-24 h-24 border-t border-l border-primary/20 pointer-events-none z-[400]" />
        <div className="absolute top-0 right-0 w-24 h-24 border-t border-r border-primary/20 pointer-events-none z-[400]" />
        
        <style jsx global>{`
          .monitoring-popup .leaflet-popup-content-wrapper {
            background: #0f0f0f;
            color: white;
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 8px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
          }
          .monitoring-popup .leaflet-popup-tip {
            background: #0f0f0f;
          }
          .leaflet-container {
            background: #0a0a0a !important;
          }
        `}</style>
      </div>
    </AppErrorBoundary>
  )
}



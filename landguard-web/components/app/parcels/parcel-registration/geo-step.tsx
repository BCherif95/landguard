"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore } from "@/lib/store/registration-flow.store"
import {
  MapPin, MousePointer2, Search, Navigation,
  Check, X, LayoutGrid, Type, Map as MapIcon, Info,
  AlertCircle, Trash2, Undo2, MousePointerClick, Compass, Plus, Loader2
} from "lucide-react"
import { useState, useCallback, useMemo, useEffect } from "react"
import { MapContainer, TileLayer, Polygon, Marker, useMapEvents, useMap, ZoomControl, ScaleControl } from "react-leaflet"
import L from "leaflet"
import "leaflet/dist/leaflet.css"
import { Textarea } from "@/components/ui/textarea"
import { Input } from "@/components/ui/input"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { cn } from "@/lib/utils"
import { useUtmConversion } from "@/lib/hooks/use-utm-conversion"
import type { UtmZone } from "@/lib/api/geo"

// Fix Leaflet icons
const icon = L.icon({
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
})

function MapEvents({ onMapClick }: { onMapClick: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng.lat, e.latlng.lng)
    },
  })
  return null
}

// Re-fits the viewport only when `version` bumps (after a UTM conversion),
// so manual drawing never steals the camera.
function MapFocus({ points, version }: { points: [number, number][]; version: number }) {
  const map = useMap()
  useEffect(() => {
    if (version > 0 && points.length >= 3) {
      map.fitBounds(L.latLngBounds(points), { padding: [40, 40] })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [version])
  return null
}

interface UtmRow {
  easting: string
  northing: string
}

const EMPTY_UTM_ROWS: UtmRow[] = [
  { easting: "", northing: "" },
  { easting: "", northing: "" },
  { easting: "", northing: "" },
]

export function GeoStep() {
  const { updateFormData, nextStep, prevStep, formData } = useRegistrationFlowStore()
  const [activeTab, setActiveTab] = useState<'input' | 'draw' | 'utm'>('input')
  const [coordText, setCoordText] = useState('')
  const [drawnPoints, setDrawnPoints] = useState<[number, number][]>([])
  const [utmZone, setUtmZone] = useState<UtmZone>('29N')
  const [utmRows, setUtmRows] = useState<UtmRow[]>(EMPTY_UTM_ROWS)
  const [mapFocusVersion, setMapFocusVersion] = useState(0)
  const { convert: convertUtm, isLoading: isConvertingUtm } = useUtmConversion()

  // Initialize from store if exists
  useEffect(() => {
    if (formData.geometry?.coordinates?.[0]) {
      const points = formData.geometry.coordinates[0].map((c: any) => [c[1], c[0]]) as [number, number][]
      // Remove last point if it's the same as first (Leaflet Polygon handles closing)
      const uniquePoints = points.slice(0, -1)
      setDrawnPoints(uniquePoints)
      
      if (!coordText) {
        setCoordText(uniquePoints.map(p => `${p[0].toFixed(6)}, ${p[1].toFixed(6)}`).join('\n'))
      }
    }
  }, [formData.geometry])

  const handleMapClick = (lat: number, lng: number) => {
    if (activeTab !== 'draw') return
    const newPoints: [number, number][] = [...drawnPoints, [lat, lng]]
    setDrawnPoints(newPoints)
    setCoordText(newPoints.map(p => `${p[0].toFixed(6)}, ${p[1].toFixed(6)}`).join('\n'))
  }

  const clearPoints = () => {
    setDrawnPoints([])
    setCoordText('')
  }

  const undoLastPoint = () => {
    const newPoints = drawnPoints.slice(0, -1)
    setDrawnPoints(newPoints)
    setCoordText(newPoints.map(p => `${p[0].toFixed(6)}, ${p[1].toFixed(6)}`).join('\n'))
  }

  const updateUtmRow = (index: number, field: keyof UtmRow, value: string) => {
    setUtmRows(rows => rows.map((row, i) => (i === index ? { ...row, [field]: value } : row)))
  }

  const addUtmRow = () => {
    setUtmRows(rows => [...rows, { easting: "", northing: "" }])
  }

  const removeUtmRow = (index: number) => {
    setUtmRows(rows => rows.filter((_, i) => i !== index))
  }

  const completeUtmPoints = useMemo(() => {
    return utmRows
      .map(row => ({ easting: parseFloat(row.easting), northing: parseFloat(row.northing) }))
      .filter(p => Number.isFinite(p.easting) && Number.isFinite(p.northing))
  }, [utmRows])

  const hasPartialUtmRow = useMemo(() => {
    return utmRows.some(row => {
      const filled = [row.easting.trim(), row.northing.trim()].filter(Boolean).length
      return filled === 1
    })
  }, [utmRows])

  const canConvertUtm = completeUtmPoints.length >= 3 && !hasPartialUtmRow && !isConvertingUtm

  const handleUtmConvert = async () => {
    const polygon = await convertUtm({ zone: utmZone, points: completeUtmPoints })
    if (!polygon) return
    // GeoJSON ring is closed ([lng, lat], first == last) — Leaflet wants open [lat, lng]
    const ring = polygon.coordinates[0] ?? []
    const latLngPoints = ring.slice(0, -1).map(([lng, lat]) => [lat, lng] as [number, number])
    setDrawnPoints(latLngPoints)
    setCoordText(latLngPoints.map(p => `${p[0].toFixed(6)}, ${p[1].toFixed(6)}`).join('\n'))
    setMapFocusVersion(v => v + 1)
  }

  const parsedGeometry = useMemo(() => {
    let points: [number, number][] = []

    if (activeTab === 'draw' || activeTab === 'utm') {
      points = drawnPoints
    } else {
      const lines = coordText.split('\n').filter(l => l.trim().length > 0)
      lines.forEach(line => {
        const parts = line.split(/[,\s;]+/).map(p => parseFloat(p)).filter(p => !isNaN(p))
        if (parts.length >= 2) {
          let lat = parts[0]
          let lng = parts[1]
          if (lat < 0 && lng > 0) [lat, lng] = [lng, lat] // Swap if likely Lng, Lat
          points.push([lat, lng])
        }
      })
    }

    if (points.length >= 3) {
      // GeoJSON expects [lng, lat] and closed loop
      const geoJsonCoords = points.map(p => [p[1], p[0]])
      geoJsonCoords.push([...geoJsonCoords[0]])
      
      return {
        type: "Polygon" as const,
        coordinates: [geoJsonCoords]
      }
    }
    return null
  }, [coordText, activeTab, drawnPoints])

  const confirmDrawing = () => {
    if (parsedGeometry) {
      updateFormData({ geometry: parsedGeometry })
      nextStep()
    }
  }

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as any)} className="w-full">
          <TabsList className="grid grid-cols-3 w-full bg-white/5 border border-white/10 p-1 mb-4 h-12 rounded-xl">
            <TabsTrigger value="input" className="rounded-lg gap-2 text-xs font-bold uppercase tracking-widest data-[state=active]:bg-emerald data-[state=active]:text-white">
              <Type className="h-4 w-4" /> Saisie Simple
            </TabsTrigger>
            <TabsTrigger value="draw" className="rounded-lg gap-2 text-xs font-bold uppercase tracking-widest data-[state=active]:bg-emerald data-[state=active]:text-white">
              <MousePointer2 className="h-4 w-4" /> Dessin Manuel
            </TabsTrigger>
            <TabsTrigger value="utm" className="rounded-lg gap-2 text-xs font-bold uppercase tracking-widest data-[state=active]:bg-emerald data-[state=active]:text-white">
              <Compass className="h-4 w-4" /> UTM
            </TabsTrigger>
          </TabsList>

          <div className="relative rounded-2xl border border-white/10 bg-zinc-950 overflow-hidden h-[400px] mb-4 shadow-2xl group">
            <MapContainer
              center={[12.6392, -7.9892]}
              zoom={13}
              zoomControl={false}
              className="h-full w-full"
            >
              <TileLayer
                attribution='&copy; <a href="https://www.esri.com/">Esri</a>'
                url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
              />
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}{r}.png"
                opacity={0.4}
              />
              
              <ZoomControl position="topright" />
              <ScaleControl position="bottomright" />
              <MapEvents onMapClick={handleMapClick} />
              <MapFocus points={drawnPoints} version={mapFocusVersion} />

              {drawnPoints.length > 0 && (
                <>
                  {drawnPoints.map((p, i) => (
                    <Marker key={i} position={p} icon={icon} />
                  ))}
                  {drawnPoints.length >= 3 && (
                    <Polygon 
                      positions={drawnPoints} 
                      pathOptions={{ fillColor: '#10b981', fillOpacity: 0.3, color: '#10b981', weight: 2 }} 
                    />
                  )}
                  {drawnPoints.length < 3 && drawnPoints.length > 0 && (
                    <Polygon 
                      positions={[...drawnPoints, drawnPoints[0]]} 
                      pathOptions={{ color: '#10b981', weight: 1, dashArray: '5, 5' }} 
                    />
                  )}
                </>
              )}
            </MapContainer>

            {/* HUD Overlay */}
            <div className="absolute top-4 left-4 z-[500] flex flex-col gap-2">
              <div className="bg-black/80 backdrop-blur-md border border-white/10 px-3 py-2 rounded-xl flex items-center gap-2 shadow-2xl">
                <div className="h-2 w-2 rounded-full bg-emerald animate-pulse" />
                <span className="text-[10px] font-bold uppercase tracking-widest text-white/90">Moteur SIG Libre</span>
              </div>
              
              {activeTab === 'draw' && (
                <div className="flex gap-2">
                  <Button size="icon" variant="secondary" onClick={undoLastPoint} disabled={drawnPoints.length === 0} className="bg-black/60 border-white/10 hover:bg-black/80">
                    <Undo2 className="h-4 w-4" />
                  </Button>
                  <Button size="icon" variant="destructive" onClick={clearPoints} disabled={drawnPoints.length === 0} className="bg-red-500/20 border-red-500/50 hover:bg-red-500/40 text-red-400">
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              )}
            </div>

            {activeTab === 'draw' && drawnPoints.length === 0 && (
              <div className="absolute inset-0 z-[450] pointer-events-none flex items-center justify-center bg-black/40 backdrop-blur-[2px]">
                <div className="bg-black/80 border border-emerald/30 p-4 rounded-2xl flex flex-col items-center gap-3 animate-in fade-in zoom-in">
                  <MousePointerClick className="h-8 w-8 text-emerald animate-bounce" />
                  <p className="text-[11px] font-bold uppercase tracking-widest text-white">Cliquez sur la carte pour définir les sommets</p>
                </div>
              </div>
            )}
          </div>

          <TabsContent value="input" className="mt-0 space-y-4">
            <div className="p-4 rounded-2xl bg-white/5 border border-white/10 space-y-3 shadow-inner">
              <div className="flex items-center gap-2 text-zinc-400">
                <Info className="h-4 w-4 text-emerald" />
                <p className="text-[11px] font-medium leading-relaxed">
                  Copiez vos coordonnées depuis votre appareil GPS (un point par ligne).
                </p>
              </div>
              <Textarea 
                placeholder={"12.6392, -7.9892\n12.6402, -7.9882\n12.6392, -7.9872..."}
                className="min-h-[120px] bg-black/40 border-white/10 font-mono text-xs text-emerald placeholder:text-zinc-700 focus:border-emerald/50 transition-colors"
                value={coordText}
                onChange={(e) => {
                  setCoordText(e.target.value)
                  // Try to sync drawn points if text is edited manually
                  // (simplified version: we trust the text as primary in this tab)
                }}
              />
              {!parsedGeometry && coordText.length > 0 && (
                <div className="flex items-center gap-2 text-orange-500 bg-orange-500/5 p-2 rounded-lg border border-orange-500/20">
                  <AlertCircle className="h-3 w-3" />
                  <span className="text-[9px] font-bold uppercase tracking-wider">Format invalide (min. 3 points requis)</span>
                </div>
              )}
            </div>
          </TabsContent>

          <TabsContent value="utm" className="mt-0 space-y-4">
            <div className="p-4 rounded-2xl bg-white/5 border border-white/10 space-y-4 shadow-inner">
              <div className="flex items-center gap-2 text-zinc-400">
                <Info className="h-4 w-4 text-emerald" />
                <p className="text-[11px] font-medium leading-relaxed">
                  Saisissez les coordonnées UTM de votre plan de situation, un sommet par ligne (zones 29N et 30N du territoire malien).
                </p>
              </div>

              <div className="space-y-2">
                <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-400">Zone UTM</span>
                <div className="grid grid-cols-2 gap-2">
                  {(["29N", "30N"] as const).map((zone) => (
                    <Button
                      key={zone}
                      type="button"
                      variant="ghost"
                      aria-pressed={utmZone === zone}
                      onClick={() => setUtmZone(zone)}
                      className={cn(
                        "h-10 rounded-lg border text-xs font-bold uppercase tracking-widest transition-colors",
                        utmZone === zone
                          ? "bg-emerald text-black border-emerald hover:bg-emerald/90 hover:text-black"
                          : "bg-black/40 text-zinc-400 border-white/10 hover:text-white",
                      )}
                    >
                      Zone {zone}
                    </Button>
                  ))}
                </div>
              </div>

              <div className="space-y-2">
                <div className="grid grid-cols-[1fr_1fr_auto] gap-2 items-center">
                  <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-400">Coordonnée Est (Easting)</span>
                  <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-400">Coordonnée Nord (Northing)</span>
                  <span className="w-9" />
                </div>
                {utmRows.map((row, index) => (
                  <div key={index} className="grid grid-cols-[1fr_1fr_auto] gap-2 items-center">
                    <Input
                      inputMode="decimal"
                      placeholder="608285.92"
                      aria-label={`Coordonnée Est (Easting) du sommet ${index + 1}`}
                      value={row.easting}
                      onChange={(e) => updateUtmRow(index, "easting", e.target.value)}
                      className="bg-black/40 border-white/10 font-mono text-xs text-emerald placeholder:text-zinc-700 focus:border-emerald/50"
                    />
                    <Input
                      inputMode="decimal"
                      placeholder="1397442.94"
                      aria-label={`Coordonnée Nord (Northing) du sommet ${index + 1}`}
                      value={row.northing}
                      onChange={(e) => updateUtmRow(index, "northing", e.target.value)}
                      className="bg-black/40 border-white/10 font-mono text-xs text-emerald placeholder:text-zinc-700 focus:border-emerald/50"
                    />
                    <Button
                      size="icon"
                      variant="ghost"
                      aria-label="Supprimer ce sommet"
                      onClick={() => removeUtmRow(index)}
                      disabled={utmRows.length <= 3}
                      className="text-zinc-500 hover:text-red-400 disabled:opacity-30"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
                <Button
                  variant="ghost"
                  onClick={addUtmRow}
                  className="w-full h-9 border border-dashed border-white/10 text-zinc-400 hover:text-white text-[10px] font-bold uppercase tracking-widest gap-2"
                >
                  <Plus className="h-3 w-3" /> Ajouter un sommet
                </Button>
              </div>

              {hasPartialUtmRow && (
                <div className="flex items-center gap-2 text-orange-500 bg-orange-500/5 p-2 rounded-lg border border-orange-500/20">
                  <AlertCircle className="h-3 w-3" />
                  <span className="text-[9px] font-bold uppercase tracking-wider">Chaque sommet requiert une coordonnée Est et une coordonnée Nord.</span>
                </div>
              )}

              <Button
                disabled={!canConvertUtm}
                onClick={handleUtmConvert}
                className="w-full h-11 bg-emerald/15 text-emerald border border-emerald/40 hover:bg-emerald/25 font-bold uppercase tracking-widest text-[10px] gap-2 transition-all active:scale-[0.98]"
              >
                {isConvertingUtm ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Conversion en cours…
                  </>
                ) : (
                  <>
                    <Compass className="h-4 w-4" />
                    Convertir et afficher sur la carte
                  </>
                )}
              </Button>
            </div>
          </TabsContent>

          <TabsContent value="draw" className="mt-0">
            <div className="p-4 rounded-2xl bg-white/5 border border-white/10 text-center">
              <div className="flex items-center justify-center gap-4 text-xs font-mono text-zinc-400">
                <div className="flex flex-col items-center gap-1">
                  <span className="text-emerald font-black text-lg">{drawnPoints.length}</span>
                  <span className="uppercase text-[8px] tracking-[0.2em]">Sommets</span>
                </div>
                <div className="h-8 w-px bg-white/10" />
                <p className="text-left text-[10px] leading-relaxed max-w-[200px]">
                  {drawnPoints.length < 3 
                    ? `Ajoutez encore ${3 - drawnPoints.length} point(s) pour former un périmètre.`
                    : "Périmètre fermé. Vous pouvez continuer d'ajouter des points ou valider."}
                </p>
              </div>
            </div>
          </TabsContent>
        </Tabs>

        <div className="grid gap-3">
          <Button 
            disabled={!parsedGeometry}
            onClick={confirmDrawing}
            className="w-full flex items-center justify-center gap-3 h-14 bg-emerald text-black hover:bg-emerald/90 font-bold uppercase tracking-widest text-xs shadow-[0_0_25px_rgba(16,185,129,0.3)] transition-all active:scale-[0.98]"
          >
            {parsedGeometry ? (
              <>
                <Check className="h-5 w-5" strokeWidth={3} />
                Valider l'Empreinte Spatiale
              </>
            ) : (
              <>
                <Navigation className="h-5 w-5" />
                En attente de coordonnées
              </>
            )}
          </Button>
          
          <Button 
            variant="ghost" 
            onClick={prevStep} 
            className="w-full text-zinc-500 h-10 hover:text-white"
          >
            Retour aux informations
          </Button>
        </div>
      </div>
      
      <style jsx global>{`
        .leaflet-container {
          background: #09090b !important;
        }
        .leaflet-tile {
          filter: brightness(0.8) contrast(1.2) grayscale(0.1);
        }
      `}</style>
    </div>
  )
}

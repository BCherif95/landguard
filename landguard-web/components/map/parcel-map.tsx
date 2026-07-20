"use client"

import { useMemo, useEffect, useState, useCallback } from "react"
import {
  MapContainer,
  TileLayer,
  Polygon,
  Polyline,
  Popup,
  CircleMarker,
  useMap,
  useMapEvents,
  ZoomControl,
  ScaleControl,
} from "react-leaflet"
import L from "leaflet"
import "leaflet/dist/leaflet.css"
import { resolveRiskMeta, resolveSeverityMeta, resolveMonitoringTypeMeta } from "@/lib/utils/safe-resolvers"
import type { Parcel } from "@/lib/api/parcels"
import type { MonitoringEvent } from "@/lib/api/types"
import { formatDateTime } from "@/lib/api/parcel-display"

export interface ParcelMapLayers {
  satellite: boolean
  labels: boolean
  parcels: boolean
  events: boolean
}

export const DEFAULT_LAYERS: ParcelMapLayers = {
  satellite: true,
  labels: true,
  parcels: true,
  events: true,
}

interface ParcelMapProps {
  parcels: Parcel[]
  selectedId: string | null
  onSelect: (parcelId: string | null) => void
  initialView?: {
    latitude: number
    longitude: number
    zoom: number
  }
  /** Which layers are visible. Defaults to everything on. */
  layers?: ParcelMapLayers
  /** Real monitoring events rendered as markers when the events layer is on. */
  events?: MonitoringEvent[]
  /** When true, two clicks on the map measure a distance instead of selecting. */
  measureActive?: boolean
  /** Reports the measured distance in meters (null when cleared). */
  onMeasure?: (distanceMeters: number | null) => void
  /** Gives the parent access to the Leaflet map (locate, fit bounds…). */
  onMapReady?: (map: L.Map) => void
}

const RISK_COLORS: Record<string, string> = {
  LOW: "#22c55e",
  MEDIUM: "#eab308",
  HIGH: "#f97316",
  CRITICAL: "#ef4444",
}

const SEVERITY_COLORS: Record<string, string> = {
  LOW: "#60a5fa",
  MEDIUM: "#eab308",
  HIGH: "#f97316",
  CRITICAL: "#ef4444",
}

function getSafeFill(risk: string): string {
  return RISK_COLORS[risk?.toUpperCase()] || "#71717a"
}

// West Africa centroid as default
const DEFAULT_CENTER: [number, number] = [12.6392, -7.9892] // Bamako area
const DEFAULT_ZOOM = 13

function MapController({
  selectedParcel,
  onMapReady,
}: {
  selectedParcel: Parcel | null
  onMapReady?: (map: L.Map) => void
}) {
  const map = useMap()

  useEffect(() => {
    onMapReady?.(map)
  }, [map, onMapReady])

  useEffect(() => {
    const lat = selectedParcel?.centroid?.latitude
    const lng = selectedParcel?.centroid?.longitude
    if (typeof lat !== "number" || typeof lng !== "number" || isNaN(lat) || isNaN(lng)) return

    // Defer to the next frame so the map is laid out, and guard against the
    // StrictMode/unmount race where the container is already gone.
    let cancelled = false
    const frame = requestAnimationFrame(() => {
      if (cancelled) return
      try {
        map.flyTo([lat, lng], Math.max(map.getZoom(), 15), { duration: 1.5 })
      } catch {
        /* map torn down mid-flight — ignore */
      }
    })

    return () => {
      cancelled = true
      cancelAnimationFrame(frame)
      // Halt any in-flight pan/zoom before Leaflet removes the map, otherwise
      // its animation loop dereferences a detached element (_leaflet_pos).
      try {
        map.stop()
      } catch {
        /* already removed */
      }
    }
  }, [selectedParcel, map])

  return null
}

/** Two-click distance measurement; a third click starts a new measurement. */
function MeasureControl({
  active,
  onMeasure,
  points,
  setPoints,
}: {
  active: boolean
  onMeasure?: (distanceMeters: number | null) => void
  points: [number, number][]
  setPoints: (points: [number, number][]) => void
}) {
  useMapEvents({
    click(e) {
      if (!active) return
      const next: [number, number][] =
        points.length >= 2 ? [[e.latlng.lat, e.latlng.lng]] : [...points, [e.latlng.lat, e.latlng.lng]]
      setPoints(next)
      if (next.length === 2) {
        const distance = L.latLng(next[0]).distanceTo(L.latLng(next[1]))
        onMeasure?.(distance)
      } else {
        onMeasure?.(null)
      }
    },
  })
  return null
}

export function ParcelMap({
  parcels,
  selectedId,
  onSelect,
  initialView,
  layers = DEFAULT_LAYERS,
  events = [],
  measureActive = false,
  onMeasure,
  onMapReady,
}: ParcelMapProps) {

  const [measurePoints, setMeasurePoints] = useState<[number, number][]>([])

  // Leaving measure mode clears the segment.
  useEffect(() => {
    if (!measureActive) {
      setMeasurePoints([])
      onMeasure?.(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [measureActive])

  const selectedParcel = useMemo(() =>
    parcels.find(p => p.id === selectedId) || null
  , [parcels, selectedId])

  const handleSelect = useCallback(
    (parcelId: string) => {
      // While measuring, clicks belong to the measurement, not the selection.
      if (!measureActive) onSelect(parcelId)
    },
    [measureActive, onSelect],
  )

  const center: [number, number] = initialView
    ? [initialView.latitude, initialView.longitude]
    : DEFAULT_CENTER

  const zoom = initialView?.zoom || DEFAULT_ZOOM

  return (
    <div className="relative h-full w-full bg-[#0f172a]">
      <MapContainer
        center={center}
        zoom={zoom}
        zoomControl={false}
        scrollWheelZoom={true}
        className="h-full w-full"
      >
        <MapController selectedParcel={selectedParcel} onMapReady={onMapReady} />
        <MeasureControl
          active={measureActive}
          onMeasure={onMeasure}
          points={measurePoints}
          setPoints={setMeasurePoints}
        />

        {layers.satellite && (
          <TileLayer
            attribution='&copy; <a href="https://www.esri.com/">Esri</a>'
            url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
          />
        )}

        {layers.labels && (
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}{r}.png"
            opacity={0.5}
          />
        )}

        <ZoomControl position="topright" />
        <ScaleControl position="bottomright" metric={true} imperial={false} />

        {layers.parcels && parcels.map((parcel) => {
          if (!parcel.geometry || !parcel.geometry.coordinates || parcel.geometry.coordinates.length === 0) {
            return null
          }

          // GeoJSON is [lng, lat], Leaflet is [lat, lng]
          const positions = parcel.geometry.coordinates[0].map((coord: number[]) => [coord[1], coord[0]]) as [number, number][]
          const isSelected = parcel.id === selectedId
          const color = getSafeFill(parcel.riskLevel)

          return (
            <Polygon
              key={parcel.id}
              positions={positions}
              pathOptions={{
                fillColor: color,
                fillOpacity: isSelected ? 0.5 : 0.2,
                color: color,
                weight: isSelected ? 3 : 1,
              }}
              eventHandlers={{
                click: () => handleSelect(parcel.id),
              }}
            >
              <Popup>
                <ParcelPopupCard parcel={parcel} />
              </Popup>
            </Polygon>
          )
        })}

        {layers.events && events.map((event) => {
          if (typeof event.latitude !== "number" || typeof event.longitude !== "number") return null
          const color = SEVERITY_COLORS[event.severity] ?? "#71717a"
          return (
            <CircleMarker
              key={event.id}
              center={[event.latitude, event.longitude]}
              radius={7}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.6, weight: 2 }}
            >
              <Popup>
                <EventPopupCard event={event} />
              </Popup>
            </CircleMarker>
          )
        })}

        {measurePoints.length > 0 && (
          <>
            {measurePoints.map((point, index) => (
              <CircleMarker
                key={`measure-${index}`}
                center={point}
                radius={5}
                pathOptions={{ color: "#ffffff", fillColor: "#10b981", fillOpacity: 1, weight: 2 }}
              />
            ))}
            {measurePoints.length === 2 && (
              <Polyline
                positions={measurePoints}
                pathOptions={{ color: "#10b981", weight: 3, dashArray: "6, 6" }}
              />
            )}
          </>
        )}
      </MapContainer>

      <style jsx global>{`
        .leaflet-popup-content-wrapper {
          background: #0f172a;
          color: white;
          border-radius: 12px;
          padding: 0;
          overflow: hidden;
          border: 1px solid rgba(255,255,255,0.1);
        }
        .leaflet-popup-content {
          margin: 0;
        }
        .leaflet-popup-tip {
          background: #0f172a;
        }
      `}</style>
    </div>
  )
}

function ParcelPopupCard({ parcel }: { parcel: Parcel }) {
  const riskMeta = resolveRiskMeta(parcel.riskLevel)

  return (
    <div className="min-w-[220px] p-4 text-xs font-mono">
      <div className="text-[9px] uppercase tracking-widest text-slate-400 mb-1">
        RÉF : {parcel.reference}
      </div>
      <div className="text-sm font-black text-white mb-1 uppercase tracking-tighter">{parcel.name}</div>
      <div className="text-slate-400 text-[10px] mb-3">{parcel.regionLabel}</div>
      <div className="flex items-center justify-between pt-3 border-t border-white/10">
        <span
          className="rounded px-2 py-0.5 font-bold text-[9px] uppercase tracking-wider"
          style={{ background: getSafeFill(parcel.riskLevel) + "30", color: getSafeFill(parcel.riskLevel) }}
        >
          Risque {riskMeta.label}
        </span>
        <span className="font-black text-emerald-400">
          {parcel.areaHectares} HA
        </span>
      </div>
    </div>
  )
}

function EventPopupCard({ event }: { event: MonitoringEvent }) {
  const severity = resolveSeverityMeta(event.severity)
  const type = resolveMonitoringTypeMeta(event.type)
  return (
    <div className="min-w-[220px] p-4 text-xs">
      <div className="text-[9px] uppercase tracking-widest text-slate-400 mb-1">
        {severity.label} · {type.label}
      </div>
      <div className="text-sm text-white mb-2">{event.description}</div>
      <div className="text-[10px] text-slate-400 font-mono">
        {formatDateTime(event.detectedAt)}
        {event.resolved ? " · Résolu" : ""}
      </div>
    </div>
  )
}

"use client"

import { useMemo, useEffect } from "react"
import { MapContainer, TileLayer, Polygon, Popup, useMap, ZoomControl, ScaleControl } from "react-leaflet"
import L from "leaflet"
import "leaflet/dist/leaflet.css"
import { resolveRiskMeta } from "@/lib/utils/safe-resolvers"
import type { Parcel } from "@/lib/api/parcels"

interface ParcelMapProps {
  parcels: Parcel[]
  selectedId: string | null
  onSelect: (parcelId: string | null) => void
  initialView?: {
    latitude: number
    longitude: number
    zoom: number
  }
}

const RISK_COLORS: Record<string, string> = {
  LOW: "#22c55e",
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

/**
 * Controller to handle map reference and view updates
 */
function MapController({ selectedParcel }: { selectedParcel: Parcel | null }) {
  const map = useMap()
  
  useEffect(() => {
    if (selectedParcel?.centroid) {
      const lat = selectedParcel.centroid.latitude
      const lng = selectedParcel.centroid.longitude
      if (typeof lat === 'number' && typeof lng === 'number' && !isNaN(lat) && !isNaN(lng)) {
        map.flyTo([lat, lng], Math.max(map.getZoom(), 15), {
          duration: 1.5
        })
      }
    }
  }, [selectedParcel, map])

  return null
}

export function ParcelMap({
  parcels,
  selectedId,
  onSelect,
  initialView,
}: ParcelMapProps) {
  
  const selectedParcel = useMemo(() => 
    parcels.find(p => p.id === selectedId) || null
  , [parcels, selectedId])

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
        <MapController selectedParcel={selectedParcel} />
        
        <TileLayer
          attribution='&copy; <a href="https://www.esri.com/">Esri</a>'
          url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
        />
        
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.basemaps.cartocdn.com/light_only_labels/{z}/{x}/{y}{r}.png"
          opacity={0.5}
        />

        <ZoomControl position="topright" />
        <ScaleControl position="bottomright" metric={true} imperial={false} />

        {parcels.map((parcel) => {
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
                click: () => onSelect(parcel.id),
              }}
            >
              <Popup>
                <ParcelPopupCard parcel={parcel} />
              </Popup>
            </Polygon>
          )
        })}
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

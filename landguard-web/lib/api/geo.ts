import { api } from "./client"
import type { PolygonGeometry } from "./parcels"

/** UTM zones covering Malian territory (situation-plan format). */
export type UtmZone = "29N" | "30N"

/** One polygon vertex in UTM metres. */
export interface UtmPoint {
  easting: number
  northing: number
}

export interface UtmToGeoJsonPayload {
  zone: UtmZone
  points: UtmPoint[]
}

export const geoApi = {
  /** Converts UTM vertices into a closed WGS-84 GeoJSON Polygon. */
  utmToGeoJson: async (payload: UtmToGeoJsonPayload): Promise<PolygonGeometry> => {
    const { data } = await api.post<PolygonGeometry>("/geo/utm-to-geojson", payload)
    return data
  },
}

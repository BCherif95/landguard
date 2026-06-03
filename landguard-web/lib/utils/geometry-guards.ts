/**
 * Geospatial guards and normalization utilities for LandGuard.
 * Ensures the UI never crashes due to invalid or incomplete geometry data.
 */

export interface Point {
  x: number
  y: number
}

/**
 * Normalizes a list of GeoJSON-style [lon, lat] coordinates into a 0..100 SVG-friendly point string.
 * @param coordinates Array of [longitude, latitude] pairs.
 * @param padding Padding around the normalized shape.
 * @returns A string of "x,y" points for an SVG <polygon />.
 */
export function normalizePolygon(
  coordinates?: number[][] | null,
  options: { width?: number; height?: number; padding?: number } = {}
): string {
  const { width = 80, height = 80, padding = 10 } = options

  if (!coordinates || !Array.isArray(coordinates) || coordinates.length < 3) {
    return ""
  }

  try {
    const lons = coordinates.map((p) => (Array.isArray(p) ? p[0] : 0))
    const lats = coordinates.map((p) => (Array.isArray(p) ? p[1] : 0))

    const minLon = Math.min(...lons)
    const maxLon = Math.max(...lons)
    const minLat = Math.min(...lats)
    const maxLat = Math.max(...lats)

    const dLon = maxLon - minLon
    const dLat = maxLat - minLat

    // Prevent division by zero if all points are the same
    if (dLon === 0 || dLat === 0) {
      return ""
    }

    return coordinates
      .map(([lon, lat]) => {
        const x = ((lon - minLon) / dLon) * width + padding
        const y = ((maxLat - lat) / dLat) * height + padding
        
        // Final sanity check for NaN or Infinity
        if (isNaN(x) || isNaN(y) || !isFinite(x) || !isFinite(y)) {
          return "0,0"
        }
        
        return `${x.toFixed(2)},${y.toFixed(2)}`
      })
      .join(" ")
  } catch (error) {
    console.error("Error normalizing polygon:", error)
    return ""
  }
}

/**
 * Validates if a set of coordinates forms a valid polygon.
 */
export function isValidPolygon(coordinates?: number[][] | null): boolean {
  if (!coordinates || !Array.isArray(coordinates)) return false
  if (coordinates.length < 3) return false
  return coordinates.every(p => Array.isArray(p) && p.length >= 2 && !isNaN(p[0]) && !isNaN(p[1]))
}

/**
 * Safely calculates the centroid of a polygon.
 */
export function safeCentroid(coordinates?: number[][] | null): [number, number] {
  if (!coordinates || !Array.isArray(coordinates) || coordinates.length === 0) {
    return [0, 0]
  }

  const sum = coordinates.reduce(
    (acc, [lon, lat]) => [acc[0] + lon, acc[1] + lat],
    [0, 0]
  )
  
  return [sum[0] / coordinates.length, sum[1] / coordinates.length]
}

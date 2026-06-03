import { z } from "zod"

/**
 * Runtime validation schemas for critical domain objects.
 * Used to parse backend data and ensure UI-side stability.
 */

export const PolygonGeometrySchema = z.object({
  type: z.literal("Polygon"),
  coordinates: z.array(z.array(z.array(z.number()))),
})

export const ParcelSchema = z.object({
  id: z.string(),
  reference: z.string().default("REF-???"),
  name: z.string().default("Sans Nom"),
  status: z.string().default("DRAFT"),
  riskLevel: z.string().default("LOW"),
  riskScore: z.number().default(0),
  trustScore: z.number().default(0),
  geometry: PolygonGeometrySchema.optional().nullable(),
  centroid: z.object({
    longitude: z.number(),
    latitude: z.number(),
  }).optional().nullable(),
})

export const MonitoringEventSchema = z.object({
  id: z.string(),
  parcelId: z.string(),
  type: z.string().default("UNKNOWN"),
  severity: z.string().default("LOW"),
  detectedAt: z.string(),
  confidenceScore: z.number().default(0),
  longitude: z.number(),
  latitude: z.number(),
  description: z.string().default(""),
  source: z.string().default("SATELLITE"),
  resolved: z.boolean().default(false),
})

export const SuccessionPlanSchema = z.object({
  id: z.string(),
  parcelId: z.string(),
  status: z.string().default("DRAFT"),
  heirs: z.array(z.object({
    id: z.string(),
    fullName: z.string().default("Héritier Inconnu"),
    relation: z.string().default(""),
    sharePercentage: z.number().default(0),
    validated: z.boolean().default(false),
  })).default([]),
})

/**
 * Safely parses a value with a schema, returning a default or partial object on failure.
 */
export function safeParse<T>(schema: z.ZodSchema<T>, data: unknown, fallback: T): T {
  const result = schema.safeParse(data)
  if (result.success) return result.data
  
  console.warn("Runtime validation failed, using fallback:", result.error)
  return fallback
}

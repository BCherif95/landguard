import { z } from "zod"

// Public env (accessible client-side via Next's NEXT_PUBLIC_ prefix).
// Validated once at module load — fail loud if mis-configured.
const PublicEnvSchema = z.object({
  NEXT_PUBLIC_API_URL: z
    .string()
    .url()
    .default("http://localhost:8080/api/v1"),
  // Mapbox token is optional — when missing, map components show a friendly fallback.
  NEXT_PUBLIC_MAPBOX_TOKEN: z
    .string()
    .min(1)
    .optional()
    .or(z.literal("").transform(() => undefined)),
})

const parsed = PublicEnvSchema.safeParse({
  NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  NEXT_PUBLIC_MAPBOX_TOKEN: process.env.NEXT_PUBLIC_MAPBOX_TOKEN,
})

if (!parsed.success) {
  console.error("Invalid public environment", parsed.error.flatten().fieldErrors)
  throw new Error("Invalid environment configuration")
}

export const env = parsed.data

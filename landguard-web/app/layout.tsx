import type { Metadata, Viewport } from "next"
import { Plus_Jakarta_Sans, JetBrains_Mono } from "next/font/google"
import { Analytics } from "@vercel/analytics/next"
import { Toaster } from "sonner"
import { QueryProvider } from "@/lib/providers/query-provider"
import { AuthBootstrap } from "@/lib/providers/auth-bootstrap"
import "./globals.css"

// Single friendly geometric family for the whole product (the Airbnb
// approach: one face, weights do the hierarchy). Plus Jakarta Sans is the
// closest open licensed alternative to Airbnb Cereal.
const jakarta = Plus_Jakarta_Sans({
  subsets: ["latin"],
  variable: "--font-jakarta",
  display: "swap",
})

const jetBrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains-mono",
  display: "swap",
})

export const metadata: Metadata = {
  title: "LA BOUSSOLE — Sécuriser la terre. Préserver l'avenir.",
  description:
    "Plateforme africaine de sécurisation, surveillance et valorisation foncière, alimentée par l'intelligence artificielle, l'imagerie satellite et la blockchain.",
  generator: "v0.app",
  icons: {
    icon: "/icon.svg",
  },
}

export const viewport: Viewport = {
  themeColor: "#F4F5F7",
  width: "device-width",
  initialScale: 1,
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html
      lang="fr"
      className={`${jakarta.variable} ${jetBrainsMono.variable} bg-background`}
      suppressHydrationWarning
    >
      <body className="font-sans antialiased bg-background text-foreground min-h-screen">
        <QueryProvider>
          <AuthBootstrap>{children}</AuthBootstrap>
          <Toaster theme="light" richColors position="top-right" />
        </QueryProvider>
        {process.env.NODE_ENV === "production" && <Analytics />}
      </body>
    </html>
  )
}

"use client"

import Link from "next/link"
import { motion } from "framer-motion"
import {
  ArrowUpRight,
  BellRing,
  Hexagon,
  Map,
  Satellite,
  ShieldCheck,
  Sparkles,
} from "lucide-react"
import { Button } from "@/components/ui/button"

export function LandingHero() {
  return (
    <section className="relative overflow-hidden border-b border-border/40">
      {/* Ambient background grid */}
      <div
        aria-hidden
        className="absolute inset-0 bg-grid opacity-[0.18]"
      />
      <div
        aria-hidden
        className="pointer-events-none absolute inset-x-0 top-0 h-[40rem] [mask-image:radial-gradient(60%_60%_at_50%_0%,#000_40%,transparent_100%)]"
        style={{
          background:
            "radial-gradient(60% 80% at 50% 0%, color-mix(in oklab, var(--emerald) 18%, transparent), transparent 70%)",
        }}
      />

      <div className="relative mx-auto max-w-7xl px-6 pb-20 pt-20 sm:pt-28">
        {/* Eyebrow */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mx-auto mb-8 flex w-fit items-center gap-2 rounded-full border border-emerald/30 bg-emerald-soft px-3 py-1 text-xs text-emerald"
        >
          <Sparkles className="h-3.5 w-3.5" />
          <span className="font-mono uppercase tracking-[0.18em]">
            IA · Satellite · Blockchain
          </span>
        </motion.div>

        {/* Headline */}
        <div className="mx-auto max-w-4xl text-center">
          <motion.h1
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.05 }}
            className="font-display text-5xl font-medium leading-[1.02] tracking-tight text-balance text-foreground sm:text-6xl md:text-7xl"
          >
            Sécuriser la terre.
            <br />
            <span className="text-emerald">Préserver l&apos;avenir.</span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.15 }}
            className="mx-auto mt-6 max-w-2xl text-pretty text-base text-muted-foreground sm:text-lg"
          >
            La première plateforme africaine de surveillance foncière, alimentée par
            l&apos;intelligence artificielle, l&apos;imagerie satellite et la blockchain. Protégez
            votre terrain contre la double vente, l&apos;occupation illégale et les fraudes
            cadastrales — en temps réel.
          </motion.p>

          {/* CTAs */}
          <motion.div
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.25 }}
            className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row"
          >
            <Button
              asChild
              size="lg"
              className="h-11 bg-emerald text-primary-foreground hover:bg-emerald/90"
            >
              <Link href="/dashboard" className="gap-2">
                Lancer Vision Live
                <ArrowUpRight className="h-4 w-4" />
              </Link>
            </Button>
            <Button
              asChild
              size="lg"
              variant="outline"
              className="h-11 border-border bg-background/40 text-foreground backdrop-blur hover:bg-secondary"
            >
              <Link href="#modules">Découvrir les modules</Link>
            </Button>
          </motion.div>

          {/* Trust badges */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 1, delay: 0.45 }}
            className="mt-8 flex flex-wrap items-center justify-center gap-x-6 gap-y-2 font-mono text-[11px] uppercase tracking-[0.16em] text-muted-foreground"
          >
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="h-3.5 w-3.5 text-emerald" />
              Conçu pour le droit foncier malien
            </span>
            <span className="hidden sm:inline">·</span>
            <span className="flex items-center gap-1.5">
              <Satellite className="h-3.5 w-3.5 text-emerald" />
              Imagerie satellite
            </span>
            <span className="hidden sm:inline">·</span>
            <span>Sessions et données sécurisées</span>
          </motion.div>
        </div>

        {/* Hero satellite preview */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.35 }}
          className="relative mx-auto mt-16 max-w-6xl"
        >
          <div className="absolute -inset-x-6 -inset-y-3 -z-10 rounded-[2rem] bg-gradient-to-b from-emerald/15 via-emerald/5 to-transparent blur-2xl" />
          <div className="overflow-hidden rounded-2xl border border-border bg-card/60 p-3 shadow-2xl shadow-black/40 backdrop-blur">
            {/* Window chrome */}
            <div className="flex items-center justify-between border-b border-border/60 px-3 py-2">
              <div className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-danger/80" />
                <span className="h-2 w-2 rounded-full bg-warning/80" />
                <span className="h-2 w-2 rounded-full bg-emerald/80" />
              </div>
              <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                vision-live · /dashboard
              </span>
              <span className="font-mono text-[10px] text-emerald">● LIVE</span>
            </div>

            {/* Inner capability preview — qualitative on purpose: the marketing
                page shows what the platform does, never invented figures. */}
            <div className="grid gap-3 p-3 sm:grid-cols-2">
              <HeroCapability
                icon={Satellite}
                title="Surveillance satellite"
                description="Suivi visuel continu de chaque parcelle enregistrée, avec historique des clichés."
              />
              <HeroCapability
                icon={BellRing}
                title="Alertes en temps réel"
                description="Notification immédiate en cas de construction, d'occupation ou d'activité suspecte."
              />
              <HeroCapability
                icon={Map}
                title="Registre numérique"
                description="Vos parcelles, titres et documents réunis dans un dossier consultable à tout moment."
              />
              <HeroCapability
                icon={Hexagon}
                title="Preuves blockchain"
                description="Les étapes clés du dossier sont ancrées dans un registre horodaté et infalsifiable."
              />
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}

function HeroCapability({
  icon: Icon,
  title,
  description,
}: {
  icon: typeof Satellite
  title: string
  description: string
}) {
  return (
    <div className="rounded-lg border border-border bg-background/40 p-4">
      <div className="flex items-center gap-2.5">
        <div className="flex h-8 w-8 items-center justify-center rounded-md border border-emerald/30 bg-emerald-soft text-emerald">
          <Icon className="h-4 w-4" />
        </div>
        <div className="font-display text-base font-medium tracking-tight text-foreground">
          {title}
        </div>
      </div>
      <p className="mt-2.5 text-xs leading-relaxed text-muted-foreground">
        {description}
      </p>
    </div>
  )
}

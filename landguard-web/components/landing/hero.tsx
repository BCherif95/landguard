"use client"

import Link from "next/link"
import { motion } from "framer-motion"
import { ArrowUpRight, ShieldCheck, Satellite, Sparkles } from "lucide-react"
import { Button } from "@/components/ui/button"
import { SatelliteMap } from "@/components/satellite/satellite-map"
import { parcels, anomalies } from "@/lib/mock-data"

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
              Conforme OHADA
            </span>
            <span className="hidden sm:inline">·</span>
            <span className="flex items-center gap-1.5">
              <Satellite className="h-3.5 w-3.5 text-emerald" />
              Sentinel-2 / Maxar
            </span>
            <span className="hidden sm:inline">·</span>
            <span>Chiffrement AES-256</span>
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

            {/* Inner dashboard preview */}
            <div className="grid gap-3 p-3 md:grid-cols-[1fr_280px]">
              <SatelliteMap
                parcels={parcels}
                anomalies={anomalies}
                selectedParcelId={parcels[2].id}
                intensity="high"
                className="aspect-[16/10] md:aspect-[16/10]"
              />
              <div className="flex flex-col gap-3">
                <HeroStat
                  label="Parcelles surveillées"
                  value="2 847"
                  delta="+12 cette semaine"
                  positive
                />
                <HeroStat
                  label="Alertes critiques"
                  value="3"
                  delta="dernière 24 h"
                />
                <HeroStat
                  label="Score de confiance"
                  value="96 %"
                  delta="moyenne réseau"
                  positive
                />
                <HeroStat
                  label="Valeur sous gestion"
                  value="2,4 Md FCFA"
                  delta="+4,1 % MoM"
                  positive
                />
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}

function HeroStat({
  label,
  value,
  delta,
  positive,
}: {
  label: string
  value: string
  delta: string
  positive?: boolean
}) {
  return (
    <div className="rounded-lg border border-border bg-background/40 p-3.5">
      <div className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </div>
      <div className="mt-1.5 font-display text-2xl font-medium tracking-tight text-foreground">
        {value}
      </div>
      <div
        className={`mt-1 text-xs ${
          positive ? "text-emerald" : "text-muted-foreground"
        }`}
      >
        {delta}
      </div>
    </div>
  )
}

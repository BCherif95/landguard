"use client"

import { motion } from "framer-motion"
import {
  Smartphone,
  Mic,
  QrCode,
  WifiOff,
  Camera,
  MapPin,
  Languages,
  Sparkles,
  Battery,
  Signal,
  Plus,
  ChevronRight,
  Volume2,
} from "lucide-react"
import { Card } from "@/components/ui/card"

const features = [
  {
    icon: QrCode,
    title: "Scan QR de bornage",
    desc: "Validez l'identité d'une parcelle directement sur le terrain.",
  },
  {
    icon: Camera,
    title: "Capture photo géolocalisée",
    desc: "Chaque cliché est horodaté, géolocalisé et versé au dossier de la parcelle.",
  },
  {
    icon: Mic,
    title: "Notes vocales multilingues",
    desc: "Dictée en français, bambara, wolof, dioula et hausa.",
  },
  {
    icon: WifiOff,
    title: "Mode hors-ligne",
    desc: "Saisie sur le terrain puis synchronisation dès le retour de la connexion.",
  },
  {
    icon: Sparkles,
    title: "Assistant vocal",
    desc: "Commandes vocales simples pour consulter une parcelle sans les mains.",
  },
  {
    icon: MapPin,
    title: "Relevés GPS",
    desc: "Localisation précise des bornes pour préparer les levés officiels.",
  },
]

const languages = [
  { code: "FR", name: "Français" },
  { code: "BM", name: "Bambara" },
  { code: "WO", name: "Wolof" },
  { code: "DJ", name: "Dioula" },
  { code: "HA", name: "Hausa" },
]

export default function MobilePage() {
  return (
    <div className="space-y-8 p-4 sm:p-6">
      {/* Header */}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="font-mono text-[10px] uppercase tracking-[0.22em] text-emerald">
            Module 08 · Application Terrain
          </p>
          <h1 className="mt-1 font-display text-2xl tracking-tight text-foreground sm:text-3xl">
            BOUSSOLE Field — l&apos;outil des agents et propriétaires
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Une expérience mobile-first pensée pour les zones rurales : légère, utilisable
            hors-ligne, multilingue et accessible aux agents non techniques. Les écrans
            ci-dessous sont des maquettes du concept — l&apos;application est en cours de
            développement et n&apos;est pas encore disponible.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1.5 rounded-md border border-border bg-secondary px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
            <Smartphone className="h-3 w-3" />
            Aperçu du concept
          </span>
        </div>
      </div>

      {/* Phones row */}
      <div className="grid gap-6 lg:grid-cols-3">
        <PhoneFrame title="Vue Terrain" delay={0}>
          <ScreenField />
        </PhoneFrame>
        <PhoneFrame title="Scan QR" delay={0.1}>
          <ScreenScan />
        </PhoneFrame>
        <PhoneFrame title="Assistant vocal" delay={0.2}>
          <ScreenVoice />
        </PhoneFrame>
      </div>

      {/* Features grid */}
      <div>
        <h2 className="font-display text-lg tracking-tight text-foreground">
          Conçue pour le terrain africain
        </h2>
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f) => (
            <Card key={f.title} className="border-border/60 bg-card/40 p-4">
              <div className="flex h-9 w-9 items-center justify-center rounded-md bg-emerald-soft text-emerald">
                <f.icon className="h-4 w-4" />
              </div>
              <h3 className="mt-3 font-display text-base text-foreground">{f.title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{f.desc}</p>
            </Card>
          ))}
        </div>
      </div>

      {/* Languages */}
      <Card className="border-border/60 bg-card/40">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/60 px-4 py-3">
          <div className="flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            <Languages className="h-3.5 w-3.5" />
            Langues prévues — voix et écriture
          </div>
        </div>
        <div className="grid grid-cols-2 gap-px bg-border/60 sm:grid-cols-5">
          {languages.map((l) => (
            <div key={l.code} className="bg-card/40 p-4">
              <div className="font-display text-2xl tracking-tight text-foreground">{l.code}</div>
              <div className="mt-1 text-xs text-muted-foreground">{l.name}</div>
              <div className="mt-2 flex items-center gap-1 font-mono text-[10px] text-emerald">
                <Volume2 className="h-3 w-3" /> Voix native
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}

function PhoneFrame({
  children,
  title,
  delay,
}: {
  children: React.ReactNode
  title: string
  delay: number
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.5 }}
      className="flex flex-col items-center"
    >
      <div className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
        {title}
      </div>
      <div className="mt-3 relative h-[560px] w-[280px] rounded-[40px] border-[10px] border-slate-900 bg-slate-900 shadow-2xl">
        <div className="absolute left-1/2 top-2 z-10 h-5 w-24 -translate-x-1/2 rounded-full bg-slate-900" />
        <div className="relative h-full w-full overflow-hidden rounded-[28px] bg-[#070d18]">
          {/* Status bar */}
          <div className="flex items-center justify-between px-5 pt-3 font-mono text-[10px] text-white/80">
            <span>09:41</span>
            <div className="flex items-center gap-1">
              <Signal className="h-3 w-3" />
              <span className="text-[8px]">5G</span>
              <Battery className="h-3 w-3" />
            </div>
          </div>
          {children}
        </div>
      </div>
    </motion.div>
  )
}

function ScreenField() {
  return (
    <div className="flex h-full flex-col p-4 pt-5">
      <div className="flex items-center justify-between">
        <div>
          <div className="font-display text-xs text-white">Bonjour, Fatoumata</div>
          <div className="font-mono text-[9px] text-white/50">3 parcelles surveillées</div>
        </div>
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-400/20 font-display text-[10px] font-medium text-emerald-300">
          FD
        </div>
      </div>

      {/* Live map mini */}
      <div className="relative mt-4 aspect-square overflow-hidden rounded-2xl bg-gradient-to-br from-slate-800 to-slate-900">
        <svg viewBox="0 0 200 200" className="h-full w-full">
          <defs>
            <pattern id="g" width="14" height="14" patternUnits="userSpaceOnUse">
              <path
                d="M 14 0 L 0 0 0 14"
                fill="none"
                stroke="rgba(255,255,255,0.06)"
                strokeWidth="0.5"
              />
            </pattern>
          </defs>
          <rect width="200" height="200" fill="url(#g)" />
          <polygon
            points="60,50 150,60 155,140 55,135"
            fill="rgba(74, 222, 128, 0.25)"
            stroke="rgb(74, 222, 128)"
            strokeWidth="1.5"
          />
          <circle cx="100" cy="95" r="3" fill="#fff" />
          <circle cx="100" cy="95" r="10" fill="none" stroke="rgb(74, 222, 128)" strokeWidth="1">
            <animate attributeName="r" values="10;30" dur="2s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="1;0" dur="2s" repeatCount="indefinite" />
          </circle>
        </svg>
        <div className="absolute left-2 top-2 rounded-md bg-black/60 px-2 py-0.5 font-mono text-[9px] uppercase tracking-[0.2em] text-emerald-300 backdrop-blur">
          ● Live
        </div>
        <div className="absolute bottom-2 left-2 right-2 rounded-md bg-black/60 px-2 py-1.5 backdrop-blur">
          <div className="font-mono text-[9px] uppercase tracking-[0.2em] text-white/60">
            Position actuelle
          </div>
          <div className="text-[10px] text-white">12.6392° N · 8.0029° W</div>
        </div>
      </div>

      <div className="mt-3 grid grid-cols-2 gap-2">
        <ActionTile icon={QrCode} label="Scanner QR" />
        <ActionTile icon={Camera} label="Photo terrain" />
      </div>

      <div className="mt-3 flex-1 rounded-2xl bg-white/[0.04] p-3">
        <div className="font-mono text-[9px] uppercase tracking-[0.2em] text-white/50">
          Dernière alerte
        </div>
        <div className="mt-1 flex items-start gap-2">
          <div className="h-1.5 w-1.5 mt-1.5 shrink-0 rounded-full bg-amber-400" />
          <div>
            <div className="text-[11px] text-white">Engin détecté · Parcelle Bamako-Est</div>
            <div className="font-mono text-[9px] text-white/50">Il y a 8 minutes</div>
          </div>
        </div>
      </div>

      <button className="mt-3 flex h-12 items-center justify-center gap-2 rounded-2xl bg-emerald-400 font-display text-sm font-medium text-emerald-950">
        <Plus className="h-4 w-4" /> Ajouter une parcelle
      </button>
    </div>
  )
}

function ScreenScan() {
  return (
    <div className="relative h-full">
      <div className="absolute inset-0 bg-gradient-to-b from-black/60 via-black/40 to-black/80" />
      <div className="relative h-full p-4 pt-5">
        <div className="font-display text-xs text-white">Scanner un QR de bornage</div>
        <div className="mt-1 font-mono text-[9px] text-white/50">
          Centrez le code dans le cadre
        </div>

        <div className="relative mt-6 mx-auto aspect-square w-56 rounded-2xl border-2 border-white/20">
          {/* Corner brackets */}
          {[
            "top-0 left-0 border-t-2 border-l-2 rounded-tl-2xl",
            "top-0 right-0 border-t-2 border-r-2 rounded-tr-2xl",
            "bottom-0 left-0 border-b-2 border-l-2 rounded-bl-2xl",
            "bottom-0 right-0 border-b-2 border-r-2 rounded-br-2xl",
          ].map((c, i) => (
            <div key={i} className={`absolute h-8 w-8 border-emerald-400 ${c}`} />
          ))}
          {/* Illustrative QR pattern — part of the clearly-labelled concept mockup */}
          <div className="absolute inset-6 grid grid-cols-8 gap-px">
            {Array.from({ length: 64 }).map((_, i) => {
              const filled =
                i === 0 || i === 7 || i === 56 || (i * 7) % 5 === 0 || (i + 3) % 4 === 0
              return (
                <div
                  key={i}
                  className={filled ? "bg-white/80" : "bg-transparent"}
                />
              )
            })}
          </div>
          {/* Scan line */}
          <motion.div
            animate={{ y: [0, 220, 0] }}
            transition={{ duration: 2.4, repeat: Number.POSITIVE_INFINITY, ease: "linear" }}
            className="absolute inset-x-0 h-0.5 bg-emerald-400 shadow-[0_0_12px_rgba(74,222,128,0.8)]"
          />
        </div>

        <div className="mt-6 rounded-2xl bg-white/[0.06] p-3 backdrop-blur">
          <div className="font-mono text-[9px] uppercase tracking-[0.2em] text-emerald-300">
            ● Détection en cours
          </div>
          <div className="mt-1 text-[11px] text-white">
            Borne BNX-2024-0421 · Lot Bamako-Est
          </div>
        </div>

        <div className="absolute bottom-4 left-4 right-4 flex items-center justify-around">
          <button className="flex h-12 w-12 items-center justify-center rounded-full bg-white/10">
            <Camera className="h-4 w-4 text-white" />
          </button>
          <button className="flex h-14 w-14 items-center justify-center rounded-full bg-emerald-400 ring-4 ring-emerald-400/20">
            <QrCode className="h-5 w-5 text-emerald-950" />
          </button>
          <button className="flex h-12 w-12 items-center justify-center rounded-full bg-white/10">
            <ChevronRight className="h-4 w-4 text-white" />
          </button>
        </div>
      </div>
    </div>
  )
}

function ScreenVoice() {
  return (
    <div className="flex h-full flex-col p-4 pt-5">
      <div className="font-display text-xs text-white">Boussole · IA vocale</div>
      <div className="mt-1 font-mono text-[9px] text-emerald-300">● À l&apos;écoute en bambara</div>

      <div className="mt-5 flex-1 space-y-2 overflow-hidden">
        <Bubble role="user" text="Boussole, ko parcelle bè dèsè wa ?" hint="(« Y a-t-il une activité sur la parcelle ? »)" />
        <Bubble
          role="ai"
          text="Oui, un engin de chantier a été détecté ce matin à 7h12 sur Bamako-Est. Niveau de risque : modéré."
        />
        <Bubble role="user" text="N&apos;ye preuve di n&apos;ma." hint="(« Génère-moi une preuve. »)" />
        <Bubble
          role="ai"
          text="Dossier de preuve créé. 14 pages, scellé blockchain. Voulez-vous l'envoyer au notaire Diallo ?"
        />
      </div>

      <div className="mt-4 rounded-2xl bg-white/[0.06] p-3 backdrop-blur">
        <div className="flex items-center justify-center gap-1">
          {[8, 14, 22, 30, 22, 14, 8, 14, 22, 30, 22, 14, 8].map((h, i) => (
            <motion.span
              key={i}
              animate={{ height: [h * 0.3, h, h * 0.3] }}
              transition={{
                duration: 0.9,
                repeat: Number.POSITIVE_INFINITY,
                delay: i * 0.06,
              }}
              className="w-1 rounded-full bg-emerald-400"
              style={{ height: h }}
            />
          ))}
        </div>
      </div>

      <button className="mt-3 flex h-14 items-center justify-center gap-2 rounded-2xl bg-emerald-400 font-display text-sm font-medium text-emerald-950">
        <Mic className="h-4 w-4" />
        Maintenir pour parler
      </button>
    </div>
  )
}

function ActionTile({ icon: Icon, label }: { icon: typeof QrCode; label: string }) {
  return (
    <button className="flex flex-col items-start gap-2 rounded-2xl bg-white/[0.06] p-3 transition-colors hover:bg-white/[0.1]">
      <div className="flex h-7 w-7 items-center justify-center rounded-md bg-emerald-400/20">
        <Icon className="h-3.5 w-3.5 text-emerald-300" />
      </div>
      <div className="text-[11px] text-white">{label}</div>
    </button>
  )
}

function Bubble({ role, text, hint }: { role: "user" | "ai"; text: string; hint?: string }) {
  return (
    <div className={role === "user" ? "flex justify-end" : "flex justify-start"}>
      <div
        className={
          role === "user"
            ? "max-w-[85%] rounded-2xl rounded-tr-sm bg-emerald-400/20 p-2 text-[10px] text-white"
            : "max-w-[85%] rounded-2xl rounded-tl-sm bg-white/[0.08] p-2 text-[10px] text-white"
        }
      >
        {text}
        {hint && (
          <div className="mt-0.5 font-mono text-[9px] text-white/40">{hint}</div>
        )}
      </div>
    </div>
  )
}

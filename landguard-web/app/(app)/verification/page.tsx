"use client"

import { motion } from "framer-motion"
import {
  ScanSearch,
  FileCheck2,
  AlertTriangle,
  Copy,
  Sparkles,
  ShieldCheck,
  Eye,
  FileWarning,
  CheckCircle2,
  XCircle,
  UploadCloud,
  Fingerprint,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"

const checks = [
  {
    id: 1,
    name: "OCR document — Titre foncier 2017-04421",
    score: 98,
    status: "ok" as const,
    detail: "Texte extrait avec confiance 98.4%. Tampon notarial reconnu.",
    icon: Eye,
  },
  {
    id: 2,
    name: "Détection de doublons cadastraux",
    score: 100,
    status: "ok" as const,
    detail: "Aucun doublon détecté sur 18 423 titres analysés (rayon 50 km).",
    icon: Copy,
  },
  {
    id: 3,
    name: "Cohérence GPS / Cadastre 2024",
    score: 92,
    status: "warn" as const,
    detail: "Décalage de 1.4 m entre coordonnées déclarées et limite cadastrale Nord.",
    icon: AlertTriangle,
  },
  {
    id: 4,
    name: "Authenticité du tampon — IA visuelle",
    score: 96,
    status: "ok" as const,
    detail: "Empreinte du tampon Notaire Diallo confirmée (base 8 421 références).",
    icon: Fingerprint,
  },
  {
    id: 5,
    name: "Contrôle de signature manuscrite",
    score: 88,
    status: "warn" as const,
    detail: "Variation de pression détectée. Demande de signature de référence.",
    icon: FileWarning,
  },
  {
    id: 6,
    name: "Vérification anti-falsification PDF",
    score: 100,
    status: "ok" as const,
    detail: "Aucune trace d'édition. Hash SHA-256 cohérent avec dépôt initial.",
    icon: ShieldCheck,
  },
]

const recentDocs = [
  { name: "TF-2024-9921-Bamako.pdf", date: "Il y a 3 min", status: "Authentique", score: 99 },
  { name: "Acte-vente-Korhogo-2023.pdf", date: "Il y a 12 min", status: "Authentique", score: 96 },
  { name: "Titre-Sikasso-doublon.pdf", date: "Il y a 41 min", status: "Doublon", score: 42 },
  { name: "Convention-Diema-2022.pdf", date: "Il y a 1 h", status: "Suspect", score: 61 },
  { name: "Permis-occuper-Segou.pdf", date: "Il y a 2 h", status: "Authentique", score: 94 },
]

export default function VerificationPage() {
  const overall = Math.round(checks.reduce((a, c) => a + c.score, 0) / checks.length)
  const verdict = overall >= 90 ? "Authentique" : overall >= 75 ? "À examiner" : "Suspect"

  return (
    <div className="space-y-6 p-4 sm:p-6">
      {/* Header */}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="font-mono text-[10px] uppercase tracking-[0.22em] text-emerald">
            Module 03 · Vérification IA
          </p>
          <h1 className="mt-1 font-display text-2xl tracking-tight text-foreground sm:text-3xl">
            Détection de fraude documentaire
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            OCR intelligent, contrôle anti-doublon, authentification de tampons et signatures —
            entraîné sur plus de 230 000 actes africains.
          </p>
        </div>
        <Button className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90">
          <UploadCloud className="h-4 w-4" />
          Téléverser un document
        </Button>
      </div>

      <div className="grid gap-4 lg:grid-cols-12">
        {/* Document preview + verdict */}
        <Card className="overflow-hidden border-border/60 bg-card/40 lg:col-span-5">
          <div className="border-b border-border/60 px-4 py-2.5 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            Document analysé
          </div>
          <div className="relative aspect-[3/4] overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
            {/* Stylized scanned document */}
            <div className="absolute inset-6 rounded-md bg-[#f6f0e2] p-6 shadow-2xl">
              <div className="flex items-start justify-between">
                <div>
                  <div className="font-display text-xs tracking-[0.3em] text-slate-700">
                    RÉPUBLIQUE DU MALI
                  </div>
                  <div className="mt-0.5 font-display text-[9px] text-slate-600">
                    Direction Nationale du Cadastre
                  </div>
                </div>
                <div className="h-8 w-8 rounded-full border-2 border-slate-800/40" />
              </div>
              <div className="mt-4 font-display text-sm font-semibold text-slate-800">
                TITRE FONCIER N° 2017-04421
              </div>
              <div className="mt-3 space-y-1.5">
                {[80, 95, 70, 88, 92, 76, 84, 60].map((w, i) => (
                  <div
                    key={i}
                    className="h-1 rounded bg-slate-300/80"
                    style={{ width: `${w}%` }}
                  />
                ))}
              </div>
              <div className="mt-5 flex items-end justify-between">
                <div className="space-y-1">
                  <div className="h-1 w-24 rounded bg-slate-300/80" />
                  <div className="h-1 w-16 rounded bg-slate-300/80" />
                </div>
                <div className="relative h-12 w-12 rounded-full border-2 border-rose-700/40">
                  <div className="absolute inset-1 rounded-full border border-rose-700/40" />
                  <div className="absolute inset-0 flex items-center justify-center font-display text-[7px] tracking-widest text-rose-700/60">
                    NOTAIRE
                  </div>
                </div>
              </div>
            </div>
            {/* Scan beam */}
            <motion.div
              aria-hidden
              animate={{ y: ["-100%", "120%"] }}
              transition={{ duration: 3.6, repeat: Number.POSITIVE_INFINITY, ease: "linear" }}
              className="absolute inset-x-0 h-32 bg-gradient-to-b from-transparent via-emerald/30 to-transparent"
            />
            {/* Detection rectangles */}
            <div className="pointer-events-none absolute inset-0">
              <div className="absolute left-[18%] top-[18%] h-8 w-32 rounded border border-emerald/70 bg-emerald/10" />
              <div className="absolute right-[14%] bottom-[14%] h-12 w-12 rounded-full border-2 border-amber-400/80 bg-amber-400/10" />
            </div>
          </div>
          <div className="border-t border-border/60 p-4">
            <div className="flex items-center justify-between">
              <div>
                <div className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                  Verdict global
                </div>
                <div className="mt-1 font-display text-2xl text-foreground">
                  {verdict}
                </div>
              </div>
              <div className="text-right">
                <div className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                  Score IA
                </div>
                <div className="mt-1 font-display text-2xl text-emerald">{overall}%</div>
              </div>
            </div>
            <Progress value={overall} className="mt-3 h-1.5" />
          </div>
        </Card>

        {/* Checks list */}
        <Card className="border-border/60 bg-card/40 lg:col-span-7">
          <div className="flex items-center justify-between border-b border-border/60 px-4 py-2.5">
            <div className="flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
              <ScanSearch className="h-3.5 w-3.5" />
              Pipeline IA · 6 contrôles
            </div>
            <span className="flex items-center gap-1.5 rounded-md bg-emerald-soft px-2 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald">
              <Sparkles className="h-3 w-3" />
              Modèle BOUSSOLE-Vision v3.2
            </span>
          </div>
          <ul className="divide-y divide-border/60">
            {checks.map((c, i) => (
              <motion.li
                key={c.id}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                className="flex items-start gap-3 px-4 py-3"
              >
                <div
                  className={
                    c.status === "ok"
                      ? "flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-emerald-soft text-emerald"
                      : "flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-amber-400/10 text-amber-400"
                  }
                >
                  <c.icon className="h-4 w-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <div className="truncate font-display text-sm text-foreground">{c.name}</div>
                    <div className="flex items-center gap-1.5 font-mono text-xs">
                      {c.status === "ok" ? (
                        <CheckCircle2 className="h-3.5 w-3.5 text-emerald" />
                      ) : (
                        <AlertTriangle className="h-3.5 w-3.5 text-amber-400" />
                      )}
                      <span
                        className={c.status === "ok" ? "text-emerald" : "text-amber-400"}
                      >
                        {c.score}%
                      </span>
                    </div>
                  </div>
                  <p className="mt-0.5 text-xs text-muted-foreground">{c.detail}</p>
                  <Progress value={c.score} className="mt-2 h-1" />
                </div>
              </motion.li>
            ))}
          </ul>
        </Card>
      </div>

      {/* Recent verifications */}
      <Card className="border-border/60 bg-card/40">
        <div className="border-b border-border/60 px-4 py-2.5 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Vérifications récentes — 24 dernières heures
        </div>
        <table className="w-full">
          <thead className="border-b border-border/60 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
            <tr>
              <th className="px-4 py-2 text-left">Document</th>
              <th className="px-4 py-2 text-left">Soumis</th>
              <th className="px-4 py-2 text-left">Statut</th>
              <th className="px-4 py-2 text-right">Score</th>
              <th className="px-4 py-2"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border/60">
            {recentDocs.map((d) => (
              <tr key={d.name} className="text-sm transition-colors hover:bg-secondary/40">
                <td className="px-4 py-3 font-mono text-xs text-foreground">{d.name}</td>
                <td className="px-4 py-3 text-muted-foreground">{d.date}</td>
                <td className="px-4 py-3">
                  <span
                    className={
                      d.status === "Authentique"
                        ? "inline-flex items-center gap-1 rounded bg-emerald-soft px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em] text-emerald"
                        : d.status === "Suspect"
                          ? "inline-flex items-center gap-1 rounded bg-amber-400/15 px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em] text-amber-500"
                          : "inline-flex items-center gap-1 rounded bg-danger/15 px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em] text-danger"
                    }
                  >
                    {d.status === "Authentique" ? (
                      <FileCheck2 className="h-3 w-3" />
                    ) : (
                      <XCircle className="h-3 w-3" />
                    )}
                    {d.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-right font-mono text-xs text-foreground">
                  {d.score}%
                </td>
                <td className="px-4 py-3 text-right">
                  <Button variant="ghost" size="sm" className="text-xs">
                    Détail
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}

import {
  Hexagon,
  Hash,
  ArrowLeftRight,
  ShieldCheck,
  FileBadge2,
  Coins,
  Edit3,
  ExternalLink,
} from "lucide-react"
import { AppTopbar } from "@/components/app/app-topbar"
import { KpiCard } from "@/components/app/kpi-card"
import { Button } from "@/components/ui/button"
import { blockchainEvents, parcels } from "@/lib/mock-data"
import { resolveBlockchainEventMeta } from "@/lib/utils/safe-resolvers"

export default function BlockchainPage() {
  return (
    <>
      <AppTopbar
        title="Blockchain & Traçabilité"
        subtitle="Registre immuable · Hash · Certificats numériques"
      />
      <div className="flex-1 space-y-4 p-4 sm:p-6">
        {/* ... (KpiCards remain same) */}
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <KpiCard
            label="Blocs ancrés"
            value="284 119"
            delta="+ 437 / 24 h"
            trend="up"
            icon={Hexagon}
            accent="emerald"
          />
          <KpiCard
            label="Certificats émis"
            value="2 791"
            delta="98% des parcelles"
            trend="up"
            icon={ShieldCheck}
            accent="gold"
          />
          <KpiCard
            label="Transferts"
            value="184"
            delta="ce mois"
            trend="up"
            icon={ArrowLeftRight}
            accent="info"
          />
          <KpiCard
            label="Hash uniques"
            value="100%"
            delta="0 collision"
            trend="flat"
            icon={Hash}
            accent="emerald"
          />
        </div>

        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          {/* Blockchain timeline */}
          <div className="overflow-hidden rounded-xl border border-border bg-card/60">
            <div className="flex items-center justify-between border-b border-border/60 px-4 py-3">
              <div>
                <h2 className="font-display text-sm font-medium text-foreground">
                  Chaîne foncière — événements récents
                </h2>
                <p className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
                  Réseau Boussole · Consensus PoA
                </p>
              </div>
              <Button
                size="sm"
                variant="outline"
                className="h-8 gap-1.5 border-border bg-background/40 text-xs hover:bg-secondary"
              >
                <ExternalLink className="h-3 w-3" />
                Explorer
              </Button>
            </div>

            <ol className="relative px-6 py-6">
              <span
                aria-hidden
                className="absolute left-[2.4rem] top-6 bottom-6 w-px bg-gradient-to-b from-emerald/40 via-border to-emerald/40"
              />
              {blockchainEvents.map((e) => {
                const meta = resolveBlockchainEventMeta(e.type)
                const parcel = parcels.find((p) => p.id === e.parcelId)
                const Icon = meta.icon
                return (
                  <li key={e.id} className="relative flex gap-4 pb-6 last:pb-0">
                    <div className="relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-emerald/30 bg-card text-emerald shadow-md">
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="min-w-0 flex-1 rounded-lg border border-border bg-background/40 p-4">
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span
                              className={`font-mono text-[10px] uppercase tracking-[0.2em] ${meta.tone}`}
                            >
                              {meta.label}
                            </span>
                            <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                              · Bloc #{e.blockNumber?.toLocaleString("fr-FR") ?? "???"}
                            </span>
                          </div>
                          <p className="mt-1 text-sm text-foreground">{e.description}</p>
                        </div>
                        <span className="shrink-0 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                          {e.timestamp}
                        </span>
                      </div>
                      <div className="mt-3 flex flex-wrap items-center justify-between gap-2 border-t border-border/60 pt-3">
                        <span className="font-mono text-[10px] text-muted-foreground">
                          {parcel?.name ?? "Parcelle inconnue"} · {e.actor}
                        </span>
                        <code className="rounded bg-secondary px-2 py-0.5 font-mono text-[10px] text-emerald">
                          {e.hash?.slice(0, 24) ?? "0x..."}…
                        </code>
                      </div>
                    </div>
                  </li>
                )
              })}
            </ol>
          </div>

          {/* Certificate preview */}
          <div className="rounded-xl border border-border bg-card/60 p-5">
            <div className="flex items-center justify-between">
              <h2 className="font-display text-sm font-medium text-foreground">
                Certificat numérique
              </h2>
              <span className="rounded border border-emerald/30 bg-emerald-soft px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-[0.18em] text-emerald">
                Vérifié
              </span>
            </div>

            <div className="mt-4 rounded-xl border border-emerald/30 p-5"
              style={{
                background:
                  "linear-gradient(160deg, color-mix(in oklab, var(--emerald) 14%, transparent) 0%, color-mix(in oklab, var(--gold) 8%, transparent) 50%, color-mix(in oklab, var(--card) 100%, transparent) 100%)",
              }}
            >
              <div className="flex items-center justify-between">
                <Hexagon className="h-8 w-8 text-emerald" />
                <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-emerald">
                  NFT Foncier · #00187
                </span>
              </div>
              <h3 className="mt-4 font-display text-xl font-medium tracking-tight text-foreground">
                Domaine de Kati Sud
              </h3>
              <p className="mt-1 font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                BSL-ML-2024-00187 · 4,8 ha · Koulikoro, Mali
              </p>

              <div className="mt-6 space-y-2 text-xs">
                <Row label="Hash de bloc" value="0x7af3a91c8d2fb0e7f4a5c89b21c" />
                <Row label="Bloc #" value="284 119" />
                <Row label="Émis le" value="12 mars 2026 — 09:05" />
                <Row label="Signataire" value="Notaire Maître Sow" />
              </div>

              <div className="mt-6 border-t border-emerald/20 pt-4">
                <p className="font-mono text-[9px] uppercase tracking-[0.22em] text-emerald/80">
                  Signature cryptographique · SHA-256 · Réseau Boussole
                </p>
              </div>
            </div>

            <Button className="mt-4 w-full bg-emerald text-primary-foreground hover:bg-emerald/90">
              Télécharger le certificat
            </Button>
          </div>
        </div>
      </div>
    </>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </span>
      <span className="truncate font-mono text-foreground">{value}</span>
    </div>
  )
}

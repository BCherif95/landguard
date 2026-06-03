import Link from "next/link"
import { ArrowUpRight } from "lucide-react"
import { Button } from "@/components/ui/button"

export function LandingCTA() {
  return (
    <section id="tarifs" className="relative overflow-hidden border-b border-border/40 py-24">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(60% 80% at 50% 50%, color-mix(in oklab, var(--emerald) 14%, transparent), transparent 70%)",
        }}
      />
      <div className="relative mx-auto max-w-4xl px-6 text-center">
        <span className="font-mono text-xs uppercase tracking-[0.22em] text-emerald">
          Démo gouvernementale · Pilotes ministériels
        </span>
        <h2 className="mt-4 font-display text-4xl font-medium tracking-tight text-balance text-foreground sm:text-5xl">
          Donnez à la terre la mémoire qu&apos;elle mérite.
        </h2>
        <p className="mx-auto mt-4 max-w-xl text-pretty text-muted-foreground">
          Démarrez avec une parcelle ou déployez à l&apos;échelle d&apos;un cadastre national —
          LA BOUSSOLE évolue avec vous.
        </p>
        <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Button
            asChild
            size="lg"
            className="h-11 bg-emerald text-primary-foreground hover:bg-emerald/90"
          >
            <Link href="/dashboard" className="gap-2">
              Démarrer une démo
              <ArrowUpRight className="h-4 w-4" />
            </Link>
          </Button>
          <Button
            asChild
            size="lg"
            variant="outline"
            className="h-11 border-border bg-card/60 text-foreground hover:bg-secondary"
          >
            <Link href="#modules">Parler à un expert foncier</Link>
          </Button>
        </div>
      </div>
    </section>
  )
}

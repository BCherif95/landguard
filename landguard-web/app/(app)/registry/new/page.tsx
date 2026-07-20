"use client"

import { useRegistrationFlowStore, REGISTRATION_STEPS } from "@/lib/store/registration-flow.store"
import { InfoStep } from "@/components/app/parcels/parcel-registration/info-step"
import { GeoStep } from "@/components/app/parcels/parcel-registration/geo-step"
import { ValidationStep } from "@/components/app/parcels/parcel-registration/validation-step"
import { ConfirmationStep } from "@/components/app/parcels/parcel-registration/confirmation-step"
import { DocumentsStep } from "@/components/app/parcels/parcel-registration/documents-step"
import { cn } from "@/lib/utils"
import { Check, ShieldCheck, ArrowLeft } from "lucide-react"
import { useEffect } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"

export default function NewParcelPage() {
  const { currentStep } = useRegistrationFlowStore()
  const router = useRouter()

  const renderStep = () => {
    switch (currentStep) {
      case 1: return <InfoStep />
      case 2: return <GeoStep />
      case 3: return <DocumentsStep />
      case 4: return <ValidationStep />
      case 5: return <ConfirmationStep />
      default: return <InfoStep />
    }
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Top Header */}
      <div className="sticky top-0 z-50 border-b border-border bg-card/80 backdrop-blur-xl">
        <div className="mx-auto flex h-16 max-w-5xl items-center justify-between gap-4 px-4 sm:px-6">
          <div className="flex items-center gap-4">
            <Link
              href="/registry"
              aria-label="Retour au registre"
              className="flex h-9 w-9 items-center justify-center rounded-full border border-border bg-card text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
            >
              <ArrowLeft className="h-4 w-4" />
            </Link>
            <div className="flex items-center gap-2 rounded-full border border-emerald/20 bg-emerald-soft px-3 py-1">
              <ShieldCheck className="h-4 w-4 text-emerald" />
              <span className="text-[10px] font-bold uppercase tracking-widest text-emerald">Nouveau dossier</span>
            </div>
          </div>

          <div className="hidden items-center gap-6 md:flex">
            {REGISTRATION_STEPS.map((step) => {
              const isCompleted = currentStep > step.id
              const isActive = currentStep === step.id

              return (
                <div key={step.id} className="flex items-center gap-3">
                  <div
                    className={cn(
                      "flex h-6 w-6 items-center justify-center rounded-full text-[10px] font-bold transition-all duration-500",
                      isCompleted ? "bg-emerald text-white shadow-sm" :
                      isActive ? "bg-primary text-primary-foreground ring-4 ring-emerald-soft" :
                      "bg-secondary text-muted-foreground border border-border"
                    )}
                  >
                    {isCompleted ? <Check className="h-3 w-3" strokeWidth={3} /> : step.id}
                  </div>
                  <span className={cn(
                    "text-[10px] font-bold uppercase tracking-[0.15em] transition-colors duration-300",
                    isActive ? "text-foreground" : isCompleted ? "text-emerald" : "text-muted-foreground"
                  )}>
                    {step.label}
                  </span>
                  {step.id < REGISTRATION_STEPS.length && (
                    <div className="ml-2 hidden h-px w-4 bg-border lg:block" />
                  )}
                </div>
              )
            })}
          </div>

          {/* Compact mobile progress */}
          <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground md:hidden">
            Étape {currentStep}/{REGISTRATION_STEPS.length}
          </span>
        </div>
      </div>

      <main className="mx-auto max-w-3xl px-4 py-10 sm:px-6 sm:py-12">
        <div className="mb-8 sm:mb-10">
          <h1 className="font-display text-3xl font-semibold tracking-tight sm:text-4xl">Enregistrer un terrain</h1>
          <p className="mt-3 max-w-xl text-pretty text-base text-muted-foreground sm:text-lg">
            Suivez les étapes normatives pour ancrer votre domaine au registre numérique national du Mali.
          </p>
        </div>

        <div className="rounded-3xl border border-border bg-card p-5 shadow-card-lg sm:p-8">
          {renderStep()}
        </div>

        <div className="mt-10 flex flex-wrap items-center justify-center gap-x-8 gap-y-3">
          {[
            { label: "Serveur souverain", color: "var(--emerald)" },
            { label: "Chiffrement AES-256", color: "var(--info)" },
            { label: "Validation État", color: "var(--gold)" },
          ].map((t) => (
            <div key={t.label} className="flex items-center gap-2">
              <span className="h-2 w-2 rounded-full" style={{ background: t.color }} />
              <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">{t.label}</span>
            </div>
          ))}
        </div>
      </main>
    </div>
  )
}

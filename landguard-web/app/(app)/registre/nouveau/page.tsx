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
    <div className="min-h-screen bg-[#070d18] text-white">
      {/* Top Header */}
      <div className="border-b border-white/5 bg-black/40 backdrop-blur-xl sticky top-0 z-50">
        <div className="max-w-5xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link 
              href="/registre" 
              className="h-8 w-8 rounded-full border border-white/10 flex items-center justify-center hover:bg-white/5 transition-colors"
            >
              <ArrowLeft className="h-4 w-4" />
            </Link>
            <div className="flex items-center gap-2">
              <ShieldCheck className="h-5 w-5 text-emerald" />
              <span className="text-[10px] font-bold uppercase tracking-widest text-emerald">Nouveau Dossier</span>
            </div>
          </div>
          
          <div className="flex items-center gap-8">
            {REGISTRATION_STEPS.map((step) => {
              const isCompleted = currentStep > step.id
              const isActive = currentStep === step.id

              return (
                <div key={step.id} className="flex items-center gap-3">
                  <div 
                    className={cn(
                      "h-6 w-6 rounded-full flex items-center justify-center text-[10px] font-bold transition-all duration-500",
                      isCompleted ? "bg-emerald text-white shadow-[0_0_15px_rgba(16,185,129,0.3)]" : 
                      isActive ? "bg-white text-black" : 
                      "bg-zinc-900 text-zinc-600 border border-white/5"
                    )}
                  >
                    {isCompleted ? <Check className="h-3 w-3" strokeWidth={3} /> : step.id}
                  </div>
                  <span className={cn(
                    "text-[10px] font-bold uppercase tracking-[0.15em] transition-colors duration-300 hidden md:inline",
                    isActive ? "text-white" : isCompleted ? "text-emerald/80" : "text-zinc-600"
                  )}>
                    {step.label}
                  </span>
                  {step.id < REGISTRATION_STEPS.length && (
                    <div className="h-[1px] w-4 bg-white/10 ml-2 hidden md:block" />
                  )}
                </div>
              )
            })}
          </div>
        </div>
      </div>

      <main className="max-w-3xl mx-auto px-6 py-12">
        <div className="mb-12">
          <h1 className="text-4xl font-bold tracking-tight mb-4">Enregistrer un terrain</h1>
          <p className="text-zinc-400 text-lg">
            Suivez les étapes normatives pour ancrer votre domaine au registre numérique national du Mali.
          </p>
        </div>

        <div className="bg-zinc-900/50 border border-white/5 rounded-3xl p-8 backdrop-blur-sm shadow-2xl">
          {renderStep()}
        </div>

        <div className="mt-12 flex items-center justify-center gap-8 text-zinc-600">
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-emerald shadow-[0_0_8px_rgba(16,185,129,0.5)]" />
            <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-500">Serveur Sécurisé</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]" />
            <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-500">Chiffrement AES-256</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-purple-500 shadow-[0_0_8px_rgba(168,85,247,0.5)]" />
            <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-500">Validation État</span>
          </div>
        </div>
      </main>
    </div>
  )
}

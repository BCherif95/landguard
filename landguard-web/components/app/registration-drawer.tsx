"use client"

import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { useRegistrationFlowStore, REGISTRATION_STEPS } from "@/lib/store/registration-flow.store"
import { InfoStep } from "./parcels/parcel-registration/info-step"
import { GeoStep } from "./parcels/parcel-registration/geo-step"
import { ValidationStep } from "./parcels/parcel-registration/validation-step"
import { ConfirmationStep } from "./parcels/parcel-registration/confirmation-step"
import { DocumentsStep } from "./parcels/parcel-registration/documents-step"
import { cn } from "@/lib/utils"
import { Check, ShieldCheck } from "lucide-react"

export function RegistrationDrawer() {
  const { isOpen, closeDrawer, currentStep } = useRegistrationFlowStore()

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
    <Sheet open={isOpen} onOpenChange={(open) => {
      if (!open) closeDrawer()
    }}>
      <SheetContent className="sm:max-w-lg overflow-y-auto border-l border-white/10 bg-black/95 backdrop-blur-2xl p-0">
        <div className="flex flex-col h-full">
          <div className="p-8 pb-4">
            <SheetHeader className="mb-8">
              <div className="flex items-center gap-2 mb-2">
                <div className="h-8 w-8 rounded-lg bg-emerald/10 flex items-center justify-center">
                  <ShieldCheck className="h-5 w-5 text-emerald" />
                </div>
                <span className="text-[10px] font-bold uppercase tracking-widest text-emerald">Sécurisation LandGuard</span>
              </div>
              <SheetTitle className="text-3xl font-bold tracking-tight">Enregistrer un terrain</SheetTitle>
              <SheetDescription className="text-muted-foreground text-base">
                Mise en conformité du domaine au registre numérique national du Mali.
              </SheetDescription>
            </SheetHeader>

            {/* Stepper Premium */}
            <div className="flex items-center justify-between mb-10 relative px-2">
              <div className="absolute top-4 left-0 w-full h-[1px] bg-white/10 z-0" />
              {REGISTRATION_STEPS.map((step) => {
                const isCompleted = currentStep > step.id
                const isActive = currentStep === step.id

                return (
                  <div key={step.id} className="relative z-10 flex flex-col items-center gap-3">
                    <div 
                      className={cn(
                        "h-8 w-8 rounded-full flex items-center justify-center text-[11px] font-bold transition-all duration-500",
                        isCompleted ? "bg-emerald text-white shadow-[0_0_15px_rgba(16,185,129,0.4)]" : 
                        isActive ? "bg-white text-black ring-4 ring-white/10" : 
                        "bg-zinc-900 text-zinc-600 border border-white/5"
                      )}
                    >
                      {isCompleted ? <Check className="h-4 w-4" strokeWidth={3} /> : step.id}
                    </div>
                    <span className={cn(
                      "text-[9px] font-bold uppercase tracking-[0.15em] transition-colors duration-300",
                      isActive ? "text-white" : isCompleted ? "text-emerald/80" : "text-zinc-600"
                    )}>
                      {step.label}
                    </span>
                  </div>
                )
              })}
            </div>

            <div className="mt-4 custom-scrollbar pr-1">
              {renderStep()}
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  )
}

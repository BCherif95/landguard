"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore } from "@/lib/store/registration-flow.store"
import { ShieldCheck, AlertTriangle, CheckCircle2, Loader2, FileText } from "lucide-react"
import { useState, useEffect } from "react"
import { Progress } from "@/components/ui/progress"

export function ValidationStep() {
  const { nextStep, prevStep } = useRegistrationFlowStore()
  const [isValidating, setIsValidating] = useState(true)
  const [progress, setProgress] = useState(0)

  useEffect(() => {
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval)
          setIsValidating(false)
          return 100
        }
        return prev + 5
      })
    }, 100)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <h3 className="text-lg font-medium">Analyse intelligente</h3>
        
        {isValidating ? (
          <div className="space-y-6 py-8 text-center">
            <div className="relative h-20 w-20 mx-auto">
              <Loader2 className="h-20 w-20 animate-spin text-emerald/20" />
              <div className="absolute inset-0 flex items-center justify-center">
                <FileText className="h-8 w-8 text-emerald animate-pulse" />
              </div>
            </div>
            <div className="space-y-2">
              <p className="text-sm font-bold uppercase tracking-widest">Analyse OCR IA en cours</p>
              <p className="text-[10px] text-muted-foreground">Comparaison des titres avec le registre de la conservation foncière...</p>
            </div>
            <Progress value={progress} className="h-1 bg-white/5" />
          </div>
        ) : (
          <div className="space-y-4">
            <div className="p-4 rounded-xl bg-emerald/5 border border-emerald/20 flex items-start gap-4">
              <div className="h-8 w-8 rounded-full bg-emerald/10 flex items-center justify-center shrink-0">
                <CheckCircle2 className="h-5 w-5 text-emerald" />
              </div>
              <div>
                <p className="text-sm font-bold text-emerald uppercase tracking-tight">Documents Authentiques</p>
                <p className="text-[11px] text-emerald/70 mt-0.5">Le Titre Foncier correspond à la base de données de l'État du Mali.</p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-blue-500/5 border border-blue-500/20 flex items-start gap-4">
              <div className="h-8 w-8 rounded-full bg-blue-500/10 flex items-center justify-center shrink-0">
                <ShieldCheck className="h-5 w-5 text-blue-500" />
              </div>
              <div>
                <p className="text-sm font-bold text-blue-500 uppercase tracking-tight">Empreinte Spatiale Validée</p>
                <p className="text-[11px] text-blue-500/70 mt-0.5">Aucun chevauchement détecté avec les parcelles voisines.</p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-orange-500/5 border border-orange-500/20 flex items-start gap-4">
              <div className="h-8 w-8 rounded-full bg-orange-500/10 flex items-center justify-center shrink-0">
                <AlertTriangle className="h-5 w-5 text-orange-500" />
              </div>
              <div>
                <p className="text-sm font-bold text-orange-500 uppercase tracking-tight">Risque d'Inondation</p>
                <p className="text-[11px] text-orange-500/70 mt-0.5">Zone classée 'Zone Humide' selon le plan d'urbanisme de Kati.</p>
              </div>
            </div>
          </div>
        )}
      </div>

      {!isValidating && (
        <div className="flex gap-3 mt-6">
          <Button variant="ghost" onClick={prevStep} className="flex-1 text-zinc-500">
            Retour
          </Button>
          <Button onClick={nextStep} className="flex-1 bg-white text-black hover:bg-zinc-200 h-11 font-bold uppercase tracking-widest">
            Valider le Dossier
          </Button>
        </div>
      )}
    </div>
  )
}

"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore } from "@/lib/store/registration-flow.store"
import { useRegisterParcel } from "@/lib/hooks/use-register-parcel"
import { ShieldCheck, Loader2, MapPin, User, FileText } from "lucide-react"

export function ConfirmationStep() {
  const { formData, prevStep } = useRegistrationFlowStore()
  const { register, isLoading } = useRegisterParcel()

  const handleRegister = async () => {
    await register()
  }

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <h3 className="text-lg font-medium">Résumé de l'ancrage</h3>
        
        <div className="rounded-xl border bg-card p-4 space-y-4">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-emerald/10 flex items-center justify-center">
              <FileText className="h-5 w-5 text-emerald" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Référence & Nom</p>
              <p className="text-sm font-semibold">{formData.reference} — {formData.name}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-blue-500/10 flex items-center justify-center">
              <MapPin className="h-5 w-5 text-blue-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Localisation</p>
              <p className="text-sm font-semibold">{formData.regionLabel} — {formData.areaHectares} ha</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-orange-500/10 flex items-center justify-center">
              <User className="h-5 w-5 text-orange-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Propriétaire</p>
              <p className="text-sm font-semibold">{formData.ownerLabel}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-purple-500/10 flex items-center justify-center">
              <FileText className="h-5 w-5 text-purple-500" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase tracking-wider">Documents</p>
              <p className="text-sm font-semibold">{formData.documents.filter(d => d.status === 'UPLOADED').length} fichiers téléversés</p>
            </div>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-emerald/5 border border-emerald/20">
          <div className="flex items-center gap-2 mb-2">
            <div className="h-2 w-2 rounded-full bg-emerald animate-pulse" />
            <span className="text-[10px] font-bold uppercase tracking-widest text-emerald">Prêt pour ancrage immuable</span>
          </div>
          <p className="text-[11px] text-muted-foreground leading-relaxed">
            Toutes les pièces justificatives et les données géospatiales sont validées. 
            L'ancrage générera un certificat numérique infalsifiable conforme aux normes de la République du Mali.
          </p>
        </div>
      </div>

      <div className="flex gap-3">
        <Button variant="ghost" onClick={prevStep} className="flex-1 text-zinc-500" disabled={isLoading}>
          Retour
        </Button>
        <Button 
          onClick={handleRegister} 
          className="flex-1 bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_20px_rgba(16,185,129,0.3)] h-11 font-bold uppercase tracking-widest"
          disabled={isLoading}
        >
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Ancrer au Registre
        </Button>
      </div>
    </div>
  )
}

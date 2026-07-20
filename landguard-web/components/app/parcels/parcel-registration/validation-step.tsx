"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore } from "@/lib/store/registration-flow.store"
import {
  FileText,
  ScanSearch,
  AlertTriangle,
  UserRound,
  Ruler,
  Hourglass,
} from "lucide-react"
import { useMemo } from "react"

/**
 * Honest pre-submission review: everything displayed here comes from the real
 * OCR readings attached to the uploaded documents. No score is invented and
 * nothing is declared "authentic" — final validation is always human.
 */
export function ValidationStep() {
  const { formData, nextStep, prevStep } = useRegistrationFlowStore()

  const uploadedDocuments = formData.documents.filter((doc) => doc.status === "UPLOADED")

  // Real, client-side consistency checks between OCR readings and the form.
  const advisories = useMemo(() => {
    const notes: string[] = []
    for (const doc of uploadedDocuments) {
      const ocr = doc.ocr
      if (!ocr || ocr.status !== "PENDING_VERIFICATION") continue
      if (
        ocr.ownerName &&
        formData.ownerLabel.trim() &&
        ocr.ownerName.trim().toLowerCase() !== formData.ownerLabel.trim().toLowerCase()
      ) {
        notes.push(
          `Le nom lu sur « ${doc.label} » (${ocr.ownerName}) diffère du propriétaire saisi (${formData.ownerLabel}).`,
        )
      }
      const surface = ocr.surfaceAreaHectares === null ? NaN : Number(ocr.surfaceAreaHectares)
      if (Number.isFinite(surface) && surface > 0 && formData.areaHectares > 0) {
        const gap = Math.abs(surface - formData.areaHectares) / formData.areaHectares
        if (gap > 0.05) {
          notes.push(
            `La superficie lue sur « ${doc.label} » (${surface} ha) s'écarte de la superficie saisie (${formData.areaHectares} ha).`,
          )
        }
      }
    }
    return notes
  }, [uploadedDocuments, formData.ownerLabel, formData.areaHectares])

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <h3 className="text-lg font-medium">Relecture avant soumission</h3>

        {/* Per-document OCR reading — real data only */}
        <div className="space-y-3">
          {uploadedDocuments.map((doc) => {
            const ocr = doc.ocr
            return (
              <div key={doc.type} className="p-4 rounded-xl bg-secondary border border-border space-y-2">
                <div className="flex items-center gap-2">
                  <FileText className="h-4 w-4 text-emerald shrink-0" />
                  <p className="text-sm font-medium">{doc.label}</p>
                  <span className="ml-auto text-[10px] font-mono text-muted-foreground truncate max-w-[140px]">
                    {doc.fileName}
                  </span>
                </div>

                {ocr?.status === "PENDING_VERIFICATION" ? (
                  <div className="space-y-1.5 pl-6">
                    {ocr.titleNumber && (
                      <p className="text-[11px] text-muted-foreground flex items-center gap-1.5">
                        <ScanSearch className="h-3 w-3 text-emerald" />
                        Numéro de titre lu : <span className="font-mono text-emerald">{ocr.titleNumber}</span>
                      </p>
                    )}
                    {ocr.ownerName && (
                      <p className="text-[11px] text-muted-foreground flex items-center gap-1.5">
                        <UserRound className="h-3 w-3 text-emerald" />
                        Propriétaire lu : <span className="text-foreground">{ocr.ownerName}</span>
                      </p>
                    )}
                    {ocr.surfaceAreaHectares !== null && (
                      <p className="text-[11px] text-muted-foreground flex items-center gap-1.5">
                        <Ruler className="h-3 w-3 text-emerald" />
                        Superficie lue : <span className="text-foreground">{String(ocr.surfaceAreaHectares)} ha</span>
                      </p>
                    )}
                    {ocr.structuralAnomalies.length > 0 && (
                      <p className="text-[11px] text-amber-600 leading-relaxed">
                        Une incohérence a été détectée dans ce document. Un agent habilité
                        examinera ce point avant toute certification.
                      </p>
                    )}
                    {!ocr.titleNumber && !ocr.ownerName && ocr.surfaceAreaHectares === null && (
                      <p className="text-[11px] text-muted-foreground">
                        Lecture automatique effectuée — aucune donnée exploitable n&apos;a pu être extraite.
                      </p>
                    )}
                  </div>
                ) : (
                  <p className="pl-6 text-[11px] text-muted-foreground leading-relaxed">
                    Lecture automatique indisponible pour ce document — il sera examiné
                    manuellement par un agent habilité.
                  </p>
                )}
              </div>
            )
          })}
        </div>

        {/* Real cross-checks between OCR readings and the form input */}
        {advisories.length > 0 && (
          <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 space-y-2">
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-amber-600 shrink-0" />
              <p className="text-sm font-bold text-amber-700 uppercase tracking-tight">Points de vigilance</p>
            </div>
            <ul className="space-y-1 pl-6">
              {advisories.map((note) => (
                <li key={note} className="text-[11px] text-amber-800/90 leading-relaxed list-disc">
                  {note}
                </li>
              ))}
            </ul>
            <p className="pl-6 text-[10px] text-muted-foreground">
              Vous pouvez revenir en arrière pour corriger, ou poursuivre : ces points seront
              signalés à l&apos;agent en charge du dossier.
            </p>
          </div>
        )}

        {/* Honest status: no automatic certification exists at this stage */}
        <div className="p-4 rounded-xl bg-secondary border border-border flex items-start gap-4">
          <div className="h-8 w-8 rounded-full bg-card border border-border flex items-center justify-center shrink-0">
            <Hourglass className="h-4 w-4 text-muted-foreground" />
          </div>
          <div>
            <p className="text-sm font-bold uppercase tracking-tight">En attente de vérification humaine</p>
            <p className="text-[11px] text-muted-foreground mt-0.5 leading-relaxed">
              Aucun contrôle d&apos;authenticité automatique n&apos;est réalisé à cette étape.
              Après soumission, votre dossier sera vérifié par les agents de la conservation
              foncière avant toute certification.
            </p>
          </div>
        </div>
      </div>

      <div className="flex gap-3 mt-6">
        <Button variant="ghost" onClick={prevStep} className="flex-1 text-muted-foreground hover:text-foreground">
          Précédent
        </Button>
        <Button onClick={nextStep} size="lg" className="flex-1 font-semibold uppercase tracking-widest">
          Continuer
        </Button>
      </div>
    </div>
  )
}

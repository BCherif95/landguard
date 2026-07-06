"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore, LandDocument } from "@/lib/store/registration-flow.store"
import { FileText, Upload, CheckCircle2, AlertCircle, X, Loader2, ScanSearch } from "lucide-react"
import { useState, useRef } from "react"
import { cn } from "@/lib/utils"
import { parcelsApi, OcrExtraction } from "@/lib/api/parcels"
import { toast } from "sonner"

const DEFAULT_AREA_HECTARES = 1.0

// OCR is advisory only: it pre-fills fields the user has not touched yet and
// surfaces anomalies. It never validates a document — that stays human.
function applyOcrPrefill(ocr: OcrExtraction) {
  const { formData, updateFormData } = useRegistrationFlowStore.getState()
  const updates: Partial<typeof formData> = {}

  if (ocr.ownerName && formData.ownerLabel.trim() === "") {
    updates.ownerLabel = ocr.ownerName
  }
  const surface = ocr.surfaceAreaHectares === null ? NaN : Number(ocr.surfaceAreaHectares)
  if (Number.isFinite(surface) && surface > 0 && formData.areaHectares === DEFAULT_AREA_HECTARES) {
    updates.areaHectares = surface
  }
  if (Object.keys(updates).length > 0) {
    updateFormData(updates)
  }
}

export function DocumentsStep() {
  const { formData, updateDocument, nextStep, prevStep } = useRegistrationFlowStore()
  const [uploadingType, setUploadingType] = useState<LandDocument['type'] | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const activeTypeRef = useRef<LandDocument['type'] | null>(null)

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    const type = activeTypeRef.current
    if (!file || !type) return

    setUploadingType(type)
    try {
      const response = await parcelsApi.uploadDocument(file, type)
      updateDocument(type, {
        status: 'UPLOADED',
        fileName: file.name,
        storageKey: response.storageKey
      })
      try {
        const ocr = await parcelsApi.getDocumentOcrExtraction(response.id)
        updateDocument(type, { ocr })
        if (ocr.status === 'PENDING_VERIFICATION') {
          applyOcrPrefill(ocr)
        }
      } catch (ocrError) {
        // The upload itself succeeded; a missing OCR reading only means the
        // instructor will review the document unaided.
        console.error("OCR extraction unavailable", ocrError)
      }
    } catch {
      toast.error("Le téléversement a échoué. Vérifiez votre connexion et réessayez.")
    } finally {
      setUploadingType(null)
      activeTypeRef.current = null
      if (fileInputRef.current) fileInputRef.current.value = ""
    }
  }

  const triggerUpload = (type: LandDocument['type']) => {
    activeTypeRef.current = type
    fileInputRef.current?.click()
  }

  const removeDocument = (type: LandDocument['type']) => {
    updateDocument(type, { status: 'PENDING', fileName: undefined, storageKey: undefined, ocr: undefined })
  }

  const allUploaded = formData.documents.every(doc => doc.status === 'UPLOADED')

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <h3 className="text-lg font-medium">Documents obligatoires (Mali)</h3>
        <p className="text-sm text-muted-foreground">
          Téléversez les scans originaux. Une lecture automatique pré-remplit certains champs ;
          la vérification finale reste effectuée par un agent habilité.
        </p>

        <div className="grid gap-3">
          {formData.documents.map((doc) => (
            <div 
              key={doc.type}
              className={cn(
                "p-4 rounded-xl border transition-all duration-300 flex items-center justify-between gap-4",
                doc.status === 'UPLOADED' ? "bg-emerald/5 border-emerald/20" : "bg-card border-border"
              )}
            >
              <div className="flex items-center gap-3">
                <div className={cn(
                  "h-10 w-10 rounded-lg flex items-center justify-center",
                  doc.status === 'UPLOADED' ? "bg-emerald/10 text-emerald" : "bg-muted text-muted-foreground"
                )}>
                  <FileText className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-sm font-medium">{doc.label}</p>
                  {doc.fileName ? (
                    <p className="text-[10px] text-emerald font-mono">{doc.fileName}</p>
                  ) : (
                    <p className="text-[10px] text-muted-foreground">Format PDF, JPG (max 5Mo)</p>
                  )}
                  {doc.ocr?.status === 'PENDING_VERIFICATION' && doc.ocr.titleNumber && (
                    <p className="text-[10px] text-muted-foreground font-mono flex items-center gap-1 mt-1">
                      <ScanSearch className="h-3 w-3 text-emerald" />
                      N° de titre détecté : {doc.ocr.titleNumber}
                    </p>
                  )}
                  {doc.ocr?.status === 'PENDING_VERIFICATION' && doc.ocr.structuralAnomalies.length > 0 && (
                    <p className="text-[10px] text-orange-500 leading-relaxed mt-1 max-w-md">
                      Notre système a détecté une incohérence dans ce document (ex: police d'écriture non uniforme). Un expert vérifiera ce point avant certification.
                    </p>
                  )}
                  {doc.ocr?.status === 'FAILED' && (
                    <p className="text-[10px] text-muted-foreground leading-relaxed mt-1 max-w-md">
                      Lecture automatique indisponible pour ce document — il sera vérifié manuellement par un expert.
                    </p>
                  )}
                </div>
              </div>

              {doc.status === 'UPLOADED' ? (
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="h-5 w-5 text-emerald" />
                  <Button 
                    variant="ghost" 
                    size="icon" 
                    className="h-8 w-8 text-muted-foreground hover:text-destructive"
                    onClick={() => removeDocument(doc.type)}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              ) : (
                <Button 
                  variant="outline" 
                  size="sm" 
                  className="h-9 gap-2"
                  onClick={() => triggerUpload(doc.type)}
                  disabled={uploadingType !== null}
                >
                  {uploadingType === doc.type ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Upload className="h-4 w-4" />
                  )}
                  {uploadingType === doc.type ? 'En cours...' : 'Téléverser'}
                </Button>
              )}
            </div>
          ))}
        </div>

        <input 
          type="file" 
          ref={fileInputRef} 
          className="hidden" 
          onChange={handleFileChange}
          accept=".pdf,.jpg,.jpeg,.png"
        />

        {!allUploaded && (
          <div className="p-3 rounded-lg bg-orange-500/5 border border-orange-500/10 flex items-center gap-2 text-orange-600">
            <AlertCircle className="h-4 w-4" />
            <span className="text-[10px] font-medium uppercase tracking-wider">
              Tous les documents sont requis pour la certification
            </span>
          </div>
        )}
      </div>

      <div className="flex gap-3">
        <Button variant="ghost" onClick={prevStep} className="flex-1">
          Retour
        </Button>
        <Button 
          onClick={nextStep} 
          className="flex-1 bg-emerald hover:bg-emerald/90 text-white"
          disabled={!allUploaded}
        >
          Suivant
        </Button>
      </div>
    </div>
  )
}

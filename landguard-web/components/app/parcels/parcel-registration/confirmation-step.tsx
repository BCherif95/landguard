"use client"

import { Button } from "@/components/ui/button"
import { useRegistrationFlowStore } from "@/lib/store/registration-flow.store"
import { useRegisterParcel } from "@/lib/hooks/use-register-parcel"
import { formatXof } from "@/lib/api/parcel-display"
import {
  Loader2,
  MapPin,
  User,
  FileText,
  Hash,
  Banknote,
  Shapes,
  AlertCircle,
} from "lucide-react"

export function ConfirmationStep() {
  const { formData, prevStep } = useRegistrationFlowStore()
  const { register, isLoading, error } = useRegisterParcel()

  const uploadedDocuments = formData.documents.filter((d) => d.status === "UPLOADED")
  const vertexCount = formData.geometry
    ? Math.max((formData.geometry.coordinates[0]?.length ?? 1) - 1, 0)
    : 0

  return (
    <div className="space-y-6 py-4">
      <div className="space-y-4">
        <h3 className="text-lg font-medium">Résumé complet du dossier</h3>

        {/* Every field captured in the wizard is restated here before the real API call. */}
        <div className="rounded-xl border bg-card p-4 space-y-4">
          <SummaryRow
            icon={Hash}
            iconClass="bg-emerald/10 text-emerald"
            label="Référence cadastrale"
            value={formData.reference}
            mono
          />
          <SummaryRow
            icon={FileText}
            iconClass="bg-emerald/10 text-emerald"
            label="Nom du domaine"
            value={formData.name}
          />
          <SummaryRow
            icon={MapPin}
            iconClass="bg-blue-500/10 text-blue-500"
            label="Localisation et superficie"
            value={`${formData.regionLabel} — ${formData.areaHectares} ha`}
          />
          <SummaryRow
            icon={User}
            iconClass="bg-orange-500/10 text-orange-500"
            label="Propriétaire"
            value={formData.ownerLabel}
          />
          <SummaryRow
            icon={Banknote}
            iconClass="bg-emerald/10 text-emerald"
            label="Valeur estimée"
            value={formatXof(formData.estimatedValueXof)}
          />
          <SummaryRow
            icon={Shapes}
            iconClass="bg-gold/10 text-gold"
            label="Empreinte spatiale"
            value={
              formData.geometry
                ? `Polygone de ${vertexCount} sommets`
                : "Aucune géométrie définie"
            }
          />
        </div>

        {/* Documents restated one by one, not just a count */}
        <div className="rounded-xl border bg-card p-4 space-y-2">
          <p className="text-xs text-muted-foreground uppercase tracking-wider">
            Pièces jointes ({uploadedDocuments.length})
          </p>
          {uploadedDocuments.length === 0 ? (
            <p className="text-sm text-muted-foreground">Aucun document téléversé.</p>
          ) : (
            <ul className="space-y-1.5">
              {uploadedDocuments.map((doc) => (
                <li key={doc.type} className="flex items-center gap-2 text-sm">
                  <FileText className="h-3.5 w-3.5 text-emerald shrink-0" />
                  <span className="font-medium">{doc.label}</span>
                  <span className="ml-auto max-w-[160px] truncate font-mono text-[10px] text-muted-foreground">
                    {doc.fileName}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="p-4 rounded-xl bg-emerald/5 border border-emerald/20">
          <div className="flex items-center gap-2 mb-2">
            <div className="h-2 w-2 rounded-full bg-emerald animate-pulse" />
            <span className="text-[10px] font-bold uppercase tracking-widest text-emerald">
              Prêt pour transmission au registre
            </span>
          </div>
          <p className="text-[11px] text-muted-foreground leading-relaxed">
            En confirmant, votre dossier sera transmis au registre numérique. Les pièces
            justificatives seront ensuite vérifiées par les agents de la conservation
            foncière avant certification.
          </p>
        </div>

        {error && (
          <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3">
            <AlertCircle className="h-4 w-4 text-red-600 shrink-0 mt-0.5" />
            <div>
              <p className="text-sm font-bold text-red-700">L&apos;enregistrement a échoué</p>
              <p className="text-[11px] text-red-700/80 mt-0.5 leading-relaxed">
                Vérifiez votre connexion et réessayez. Si le problème persiste, vos données
                saisies restent conservées dans ce formulaire.
              </p>
            </div>
          </div>
        )}
      </div>

      <div className="flex gap-3">
        <Button variant="ghost" onClick={prevStep} className="flex-1 text-muted-foreground hover:text-foreground" disabled={isLoading}>
          Précédent
        </Button>
        <Button
          onClick={() => register()}
          size="lg"
          className="flex-1 shadow-md font-semibold uppercase tracking-widest"
          disabled={isLoading}
        >
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {isLoading ? "Enregistrement…" : "Ancrer au Registre"}
        </Button>
      </div>
    </div>
  )
}

function SummaryRow({
  icon: Icon,
  iconClass,
  label,
  value,
  mono,
}: {
  icon: typeof MapPin
  iconClass: string
  label: string
  value: string
  mono?: boolean
}) {
  return (
    <div className="flex items-center gap-3">
      <div className={`h-10 w-10 rounded-full flex items-center justify-center shrink-0 ${iconClass}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-muted-foreground uppercase tracking-wider">{label}</p>
        <p className={`text-sm font-semibold truncate ${mono ? "font-mono" : ""}`}>{value}</p>
      </div>
    </div>
  )
}

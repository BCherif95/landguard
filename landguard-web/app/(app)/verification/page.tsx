"use client"

import { useEffect, useMemo, useState } from "react"
import {
  FileCheck2,
  FileSearch,
  Loader2,
  ShieldCheck,
  AlertTriangle,
  Check,
  Landmark,
  Send,
  ScrollText,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Checkbox } from "@/components/ui/checkbox"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import { PremiumEmptyState, ErrorState } from "@/components/app/empty-states"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { useParcels } from "@/lib/hooks/use-parcels"
import { useCurrentUser } from "@/lib/hooks/use-current-user"
import {
  useTitleVerificationCase,
  useOpenVerificationCase,
  useSubmitVerificationCase,
  useStartDomainControl,
  useRecordRequisition,
  useCertifyVerificationCase,
} from "@/lib/hooks/use-title-verification"
import type {
  TitleVerificationCase,
  TitleRequisition,
  VerificationStatus,
} from "@/lib/api/title-verifications"
import { formatDateTime } from "@/lib/api/parcel-display"
import { toast } from "sonner"
import { cn } from "@/lib/utils"

// Nominal path of a verification case; branch states are surfaced as banners.
const STEPS: { status: VerificationStatus; label: string }[] = [
  { status: "DRAFT", label: "Dossier ouvert" },
  { status: "PENDING_VERIFICATION", label: "Soumis pour vérification" },
  { status: "DOMAIN_CONTROL", label: "Contrôle au bureau des Domaines" },
  { status: "TF_VERIFIED", label: "Réquisition enregistrée" },
  { status: "CERTIFIED", label: "Certifié" },
]

const STEP_RANK: Partial<Record<VerificationStatus, number>> = {
  DRAFT: 1,
  PENDING_VERIFICATION: 2,
  PENDING_COMPLEMENT: 2,
  DOMAIN_CONTROL: 3,
  TF_VERIFIED: 4,
  CERTIFIED: 5,
}

const RESTRICTED_ACTION_HINT = "Action réservée à un notaire ou agent habilité"

function generateCaseReference(): string {
  const year = new Date().getFullYear()
  const suffix = Math.floor(100000 + Math.random() * 900000)
  return `DOS-${year}-${suffix}`
}

export default function VerificationPage() {
  const user = useCurrentUser()
  const isOfficial = user ? ["OFFICER", "LEGAL", "ADMIN"].includes(user.role) : false

  const { data: parcels, isLoading: parcelsLoading, isError: parcelsError, refetch } = useParcels({ limit: 200 })
  const [selectedParcelId, setSelectedParcelId] = useState<string | null>(null)

  useEffect(() => {
    if (!selectedParcelId && parcels && parcels.length > 0) {
      setSelectedParcelId(parcels[0].id)
    }
  }, [parcels, selectedParcelId])

  const selectedParcel = useMemo(
    () => parcels?.find((p) => p.id === selectedParcelId) ?? null,
    [parcels, selectedParcelId],
  )

  const caseQuery = useTitleVerificationCase(selectedParcelId)

  return (
    <AppErrorBoundary name="Vérification de titre">
      <TooltipProvider delayDuration={200}>
        <div className="space-y-6 p-4 sm:p-6">
          {/* Header */}
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="font-mono text-[10px] uppercase tracking-[0.22em] text-emerald">
                Module 03 · Vérification de titre
              </p>
              <h1 className="mt-1 font-display text-2xl tracking-tight text-foreground sm:text-3xl">
                Vérification d&apos;un titre foncier
              </h1>
              <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
                Suivi du dossier officiel : ouverture, soumission, contrôle au bureau des
                Domaines, réquisition et certification finale.
              </p>
            </div>

            {parcels && parcels.length > 0 && (
              <div className="w-full sm:w-80">
                <Label className="mb-1.5 block font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
                  Parcelle concernée
                </Label>
                <Select
                  value={selectedParcelId ?? undefined}
                  onValueChange={(value) => setSelectedParcelId(value)}
                >
                  <SelectTrigger aria-label="Sélectionner une parcelle">
                    <SelectValue placeholder="Choisissez une parcelle" />
                  </SelectTrigger>
                  <SelectContent>
                    {parcels.map((p) => (
                      <SelectItem key={p.id} value={p.id}>
                        {p.name} — {p.reference}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>

          {parcelsLoading && (
            <div className="flex items-center justify-center p-16 text-muted-foreground">
              <Loader2 className="mr-2 h-5 w-5 animate-spin" />
              Chargement de vos parcelles…
            </div>
          )}

          {parcelsError && (
            <ErrorState
              message="Impossible de charger vos parcelles. Vérifiez votre connexion puis réessayez."
              retry={() => refetch()}
            />
          )}

          {parcels && parcels.length === 0 && (
            <PremiumEmptyState
              icon={FileSearch}
              title="Aucune parcelle enregistrée"
              description="Enregistrez d'abord une parcelle au registre pour pouvoir ouvrir une vérification de titre foncier."
            />
          )}

          {selectedParcel && (
            <CaseSection
              parcelId={selectedParcel.id}
              parcelName={selectedParcel.name}
              caseQuery={caseQuery}
              isOfficial={isOfficial}
            />
          )}
        </div>
      </TooltipProvider>
    </AppErrorBoundary>
  )
}

function CaseSection({
  parcelId,
  parcelName,
  caseQuery,
  isOfficial,
}: {
  parcelId: string
  parcelName: string
  caseQuery: ReturnType<typeof useTitleVerificationCase>
  isOfficial: boolean
}) {
  if (caseQuery.isLoading) {
    return (
      <div className="flex items-center justify-center p-16 text-muted-foreground">
        <Loader2 className="mr-2 h-5 w-5 animate-spin" />
        Recherche d&apos;un dossier de vérification…
      </div>
    )
  }

  if (caseQuery.isError) {
    return (
      <ErrorState
        message="Impossible de consulter le dossier de vérification. Vérifiez votre connexion puis réessayez."
        retry={() => caseQuery.refetch()}
      />
    )
  }

  const verificationCase = caseQuery.data ?? null

  if (!verificationCase) {
    return (
      <PremiumEmptyState
        icon={FileCheck2}
        title="Aucune vérification en cours"
        description={`Aucun dossier de vérification n'est ouvert pour « ${parcelName} ». Ouvrez un dossier en renseignant les informations du titre foncier à contrôler.`}
        action={<OpenCaseDialog parcelId={parcelId} />}
      />
    )
  }

  return <CaseDetail verificationCase={verificationCase} isOfficial={isOfficial} />
}

/* ------------------------------------------------------------------ */
/* Open case dialog                                                    */
/* ------------------------------------------------------------------ */

function OpenCaseDialog({ parcelId }: { parcelId: string }) {
  const [open, setOpen] = useState(false)
  const openCase = useOpenVerificationCase()

  const [form, setForm] = useState({
    tfNumber: "",
    volume: "",
    folio: "",
    conservationOffice: "",
    issueDate: "",
    ownerName: "",
    areaHectares: "",
    location: "",
  })

  const setField = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const missingFields =
    !form.tfNumber.trim() ||
    !form.conservationOffice.trim() ||
    !form.ownerName.trim() ||
    !form.location.trim() ||
    !form.issueDate ||
    !(parseFloat(form.areaHectares) > 0)

  const handleSubmit = async () => {
    try {
      await openCase.mutateAsync({
        parcelId,
        caseReference: generateCaseReference(),
        reportedTitle: {
          tfNumber: form.tfNumber.trim(),
          volume: form.volume.trim(),
          folio: form.folio.trim(),
          conservationOffice: form.conservationOffice.trim(),
          issueDate: new Date(form.issueDate).toISOString(),
          ownerName: form.ownerName.trim(),
          areaHectares: parseFloat(form.areaHectares),
          location: form.location.trim(),
          status: "DECLARE",
        },
      })
      toast.success("Dossier de vérification ouvert.")
      setOpen(false)
    } catch {
      toast.error("L'ouverture du dossier a échoué. Vérifiez les informations puis réessayez.")
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90">
          <FileCheck2 className="h-4 w-4" />
          Ouvrir une vérification de titre pour cette parcelle
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Ouvrir un dossier de vérification</DialogTitle>
          <DialogDescription>
            Renseignez les informations telles qu&apos;elles figurent sur le titre foncier.
            Elles seront contrôlées au bureau des Domaines.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-3 py-2">
          <FieldRow label="Numéro du titre foncier (TF)" required>
            <Input value={form.tfNumber} onChange={setField("tfNumber")} placeholder="Ex : TF-4421-BKO" />
          </FieldRow>
          <div className="grid grid-cols-2 gap-3">
            <FieldRow label="Volume">
              <Input value={form.volume} onChange={setField("volume")} placeholder="Ex : 412" />
            </FieldRow>
            <FieldRow label="Folio">
              <Input value={form.folio} onChange={setField("folio")} placeholder="Ex : 89" />
            </FieldRow>
          </div>
          <FieldRow label="Bureau de la conservation foncière" required>
            <Input
              value={form.conservationOffice}
              onChange={setField("conservationOffice")}
              placeholder="Ex : Conservation de Bamako"
            />
          </FieldRow>
          <FieldRow label="Date de délivrance" required>
            <Input type="date" value={form.issueDate} onChange={setField("issueDate")} />
          </FieldRow>
          <FieldRow label="Propriétaire inscrit au titre" required>
            <Input value={form.ownerName} onChange={setField("ownerName")} placeholder="Ex : Famille Traoré" />
          </FieldRow>
          <div className="grid grid-cols-2 gap-3">
            <FieldRow label="Superficie (ha)" required>
              <Input
                type="number"
                step="0.0001"
                min="0"
                value={form.areaHectares}
                onChange={setField("areaHectares")}
                placeholder="Ex : 4,8"
              />
            </FieldRow>
            <FieldRow label="Localisation" required>
              <Input value={form.location} onChange={setField("location")} placeholder="Ex : Kati, Koulikoro" />
            </FieldRow>
          </div>
        </div>

        {missingFields && (
          <p className="text-xs text-muted-foreground">
            Renseignez tous les champs obligatoires pour ouvrir le dossier.
          </p>
        )}

        <Button
          onClick={handleSubmit}
          disabled={missingFields || openCase.isPending}
          className="w-full bg-emerald text-primary-foreground hover:bg-emerald/90"
        >
          {openCase.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Ouvrir le dossier
        </Button>
      </DialogContent>
    </Dialog>
  )
}

function FieldRow({
  label,
  required,
  children,
}: {
  label: string
  required?: boolean
  children: React.ReactNode
}) {
  return (
    <div className="space-y-1.5">
      <Label className="text-xs text-muted-foreground">
        {label}
        {required && <span className="text-danger"> *</span>}
      </Label>
      {children}
    </div>
  )
}

/* ------------------------------------------------------------------ */
/* Case detail: stepper, title facts, actions                          */
/* ------------------------------------------------------------------ */

function CaseDetail({
  verificationCase,
  isOfficial,
}: {
  verificationCase: TitleVerificationCase
  isOfficial: boolean
}) {
  const status = verificationCase.status
  const rank = STEP_RANK[status] ?? 0
  const isTerminalFailure = status === "TF_REJECTED" || status === "DISPUTE_SIGNALED"

  return (
    <div className="space-y-4">
      {/* Case header */}
      <div className="flex flex-wrap items-center gap-3">
        <span className="font-mono text-xs text-muted-foreground">
          Dossier {verificationCase.caseReference}
        </span>
        <span className="font-mono text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
          Ouvert le {formatDateTime(verificationCase.createdAt)}
        </span>
      </div>

      {/* Branch-state banners */}
      {status === "TF_REJECTED" && (
        <Banner
          tone="danger"
          title="Titre rejeté"
          description={
            verificationCase.requisition?.rejectionReason
              ? `Motif : ${verificationCase.requisition.rejectionReason}`
              : "Le titre présenté n'a pas été authentifié par le bureau des Domaines."
          }
        />
      )}
      {status === "DISPUTE_SIGNALED" && (
        <Banner
          tone="danger"
          title="Litige signalé"
          description="Un litige a été relevé lors du contrôle domanial. Le dossier est suspendu dans l'attente d'une résolution juridique."
        />
      )}
      {status === "PENDING_COMPLEMENT" && (
        <Banner
          tone="warning"
          title="Complément requis"
          description="Le bureau des Domaines demande des pièces ou informations complémentaires. Complétez le dossier puis soumettez-le à nouveau."
        />
      )}

      {/* Stepper */}
      {!isTerminalFailure && (
        <Card className="border-border/60 bg-card/40 p-5">
          <ol className="grid gap-4 sm:grid-cols-5">
            {STEPS.map((step, index) => {
              const stepNumber = index + 1
              const done = rank > stepNumber || status === "CERTIFIED"
              const active = rank === stepNumber && status !== "CERTIFIED"
              return (
                <li key={step.status} className="flex items-start gap-3 sm:flex-col sm:items-center sm:text-center">
                  <div
                    className={cn(
                      "flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[11px] font-bold transition-colors",
                      done && "bg-emerald text-white",
                      active && "bg-foreground text-background ring-4 ring-foreground/10",
                      !done && !active && "border border-border bg-secondary text-muted-foreground",
                    )}
                  >
                    {done ? <Check className="h-4 w-4" strokeWidth={3} /> : stepNumber}
                  </div>
                  <span
                    className={cn(
                      "text-xs leading-snug",
                      active ? "font-medium text-foreground" : done ? "text-emerald" : "text-muted-foreground",
                    )}
                  >
                    {step.label}
                  </span>
                </li>
              )
            })}
          </ol>
        </Card>
      )}

      {status === "CERTIFIED" && (
        <Banner
          tone="success"
          title="Titre certifié"
          description={`Certification enregistrée${verificationCase.certifiedAt ? ` le ${formatDateTime(verificationCase.certifiedAt)}` : ""}.`}
        />
      )}

      <div className="grid gap-4 lg:grid-cols-2">
        {/* Reported title facts */}
        <Card className="border-border/60 bg-card/40">
          <div className="flex items-center gap-2 border-b border-border/60 px-4 py-2.5 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            <ScrollText className="h-3.5 w-3.5" />
            Titre déclaré
          </div>
          <dl className="space-y-2 p-4 text-sm">
            <FactRow label="Numéro TF" value={verificationCase.reportedTitle.tfNumber} mono />
            <FactRow
              label="Volume / Folio"
              value={
                [verificationCase.reportedTitle.volume, verificationCase.reportedTitle.folio]
                  .filter(Boolean)
                  .join(" / ") || "Non renseigné"
              }
            />
            <FactRow label="Conservation" value={verificationCase.reportedTitle.conservationOffice} />
            <FactRow label="Délivré le" value={formatDateTime(verificationCase.reportedTitle.issueDate)} />
            <FactRow label="Propriétaire inscrit" value={verificationCase.reportedTitle.ownerName} />
            <FactRow label="Superficie" value={`${verificationCase.reportedTitle.areaHectares} ha`} />
            <FactRow label="Localisation" value={verificationCase.reportedTitle.location} />
          </dl>
        </Card>

        {/* Requisition result, if recorded */}
        <Card className="border-border/60 bg-card/40">
          <div className="flex items-center gap-2 border-b border-border/60 px-4 py-2.5 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            <Landmark className="h-3.5 w-3.5" />
            Réquisition domaniale
          </div>
          {verificationCase.requisition ? (
            <dl className="space-y-2 p-4 text-sm">
              <FactRow label="N° de réquisition" value={verificationCase.requisition.requisitionNumber} mono />
              <FactRow label="Bureau" value={verificationCase.requisition.domainOffice} />
              <FactRow
                label="Vérificateur"
                value={`${verificationCase.requisition.verifierName} (${verificationCase.requisition.verifierRole})`}
              />
              <FactRow
                label="Authenticité"
                value={verificationCase.requisition.authenticityConfirmed ? "Confirmée" : "Non confirmée"}
              />
              <FactRow
                label="Litige détecté"
                value={verificationCase.requisition.litigationDetected ? "Oui" : "Non"}
              />
              <FactRow
                label="Hypothèque bloquante"
                value={verificationCase.requisition.mortgageDetected ? "Oui" : "Non"}
              />
              <FactRow
                label="Concordance du nom"
                value={verificationCase.requisition.nameMatchConfirmed ? "Confirmée" : "Non confirmée"}
              />
              {verificationCase.requisition.verificationNotes && (
                <FactRow label="Observations" value={verificationCase.requisition.verificationNotes} />
              )}
            </dl>
          ) : (
            <p className="p-4 text-sm text-muted-foreground">
              Aucune réquisition n&apos;a encore été enregistrée pour ce dossier.
            </p>
          )}
        </Card>
      </div>

      <CaseActions verificationCase={verificationCase} isOfficial={isOfficial} />
    </div>
  )
}

function Banner({
  tone,
  title,
  description,
}: {
  tone: "danger" | "warning" | "success"
  title: string
  description: string
}) {
  const toneClass = {
    danger: "border-danger/30 bg-danger/10 text-danger",
    warning: "border-amber-500/30 bg-amber-500/10 text-amber-500",
    success: "border-emerald/30 bg-emerald-soft text-emerald",
  }[tone]
  const Icon = tone === "success" ? ShieldCheck : AlertTriangle
  return (
    <div className={cn("flex items-start gap-3 rounded-xl border p-4", toneClass)}>
      <Icon className="mt-0.5 h-4 w-4 shrink-0" />
      <div>
        <p className="text-sm font-medium">{title}</p>
        <p className="mt-0.5 text-xs opacity-80">{description}</p>
      </div>
    </div>
  )
}

function FactRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <dt className="shrink-0 text-xs text-muted-foreground">{label}</dt>
      <dd className={cn("text-right text-sm text-foreground", mono && "font-mono text-xs")}>{value}</dd>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/* Role-aware actions                                                  */
/* ------------------------------------------------------------------ */

function CaseActions({
  verificationCase,
  isOfficial,
}: {
  verificationCase: TitleVerificationCase
  isOfficial: boolean
}) {
  const submit = useSubmitVerificationCase()
  const startControl = useStartDomainControl()
  const certify = useCertifyVerificationCase()

  const ids = { id: verificationCase.id, parcelId: verificationCase.parcelId }
  const status = verificationCase.status

  const run = async (action: () => Promise<unknown>, successMessage: string) => {
    try {
      await action()
      toast.success(successMessage)
    } catch {
      toast.error("L'opération a échoué. Vérifiez votre connexion puis réessayez.")
    }
  }

  if (status === "CERTIFIED" || status === "TF_REJECTED" || status === "DISPUTE_SIGNALED") {
    return null
  }

  return (
    <Card className="border-border/60 bg-card/40 p-4">
      <p className="mb-3 font-mono text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
        Prochaine étape
      </p>

      {(status === "DRAFT" || status === "PENDING_COMPLEMENT") && (
        <Button
          onClick={() =>
            run(() => submit.mutateAsync(ids), "Dossier soumis pour vérification.")
          }
          disabled={submit.isPending}
          className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90"
        >
          {submit.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
          {status === "PENDING_COMPLEMENT" ? "Soumettre à nouveau" : "Soumettre pour vérification"}
        </Button>
      )}

      {status === "PENDING_VERIFICATION" && (
        <OfficialGate
          isOfficial={isOfficial}
          waitingMessage="Votre dossier est en file d'attente : un agent des Domaines prendra le relais pour le contrôle."
        >
          <Button
            onClick={() =>
              run(() => startControl.mutateAsync(ids), "Contrôle au bureau des Domaines démarré.")
            }
            disabled={startControl.isPending}
            className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90"
          >
            {startControl.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Landmark className="h-4 w-4" />}
            Démarrer le contrôle domanial
          </Button>
        </OfficialGate>
      )}

      {status === "DOMAIN_CONTROL" && (
        <OfficialGate
          isOfficial={isOfficial}
          waitingMessage="Le contrôle est en cours au bureau des Domaines. Le résultat de la réquisition sera affiché ici."
        >
          <RequisitionForm verificationCase={verificationCase} />
        </OfficialGate>
      )}

      {status === "TF_VERIFIED" && (
        <OfficialGate
          isOfficial={isOfficial}
          waitingMessage="Le titre a été vérifié. La certification finale sera prononcée par un notaire ou un agent habilité."
        >
          <Button
            onClick={() => run(() => certify.mutateAsync(ids), "Titre certifié.")}
            disabled={certify.isPending}
            className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90"
          >
            {certify.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <ShieldCheck className="h-4 w-4" />}
            Certifier le titre
          </Button>
        </OfficialGate>
      )}
    </Card>
  )
}

/**
 * Shows the action to officials; citizens get an honest waiting message and a
 * disabled control with an explanatory tooltip instead of a dead button.
 */
function OfficialGate({
  isOfficial,
  waitingMessage,
  children,
}: {
  isOfficial: boolean
  waitingMessage: string
  children: React.ReactNode
}) {
  if (isOfficial) return <>{children}</>
  return (
    <div className="space-y-3">
      <p className="text-sm text-muted-foreground">{waitingMessage}</p>
      <Tooltip>
        <TooltipTrigger asChild>
          <span className="inline-block">
            <Button disabled variant="outline" className="pointer-events-none gap-2">
              <ShieldCheck className="h-4 w-4" />
              Action de contrôle
            </Button>
          </span>
        </TooltipTrigger>
        <TooltipContent>{RESTRICTED_ACTION_HINT}</TooltipContent>
      </Tooltip>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/* Requisition form (officials only)                                   */
/* ------------------------------------------------------------------ */

function RequisitionForm({ verificationCase }: { verificationCase: TitleVerificationCase }) {
  const recordRequisition = useRecordRequisition()

  const [form, setForm] = useState({
    requisitionNumber: "",
    domainOffice: verificationCase.reportedTitle.conservationOffice,
    verifierName: "",
    verifierRole: "",
    verificationNotes: "",
    authenticityConfirmed: false,
    conflictDetected: false,
    litigationDetected: false,
    mortgageDetected: false,
    nameMatchConfirmed: false,
    rejectionReason: "",
  })

  // PRD 02.2: the title is rejected (not disputed) when any mandatory checklist
  // item fails without a litigation — a written reason is then required.
  const checklistPassed =
    form.authenticityConfirmed && !form.mortgageDetected && form.nameMatchConfirmed
  const needsRejectionReason = !form.litigationDetected && !checklistPassed
  const missing =
    !form.requisitionNumber.trim() ||
    !form.domainOffice.trim() ||
    !form.verifierName.trim() ||
    !form.verifierRole.trim() ||
    (needsRejectionReason && !form.rejectionReason.trim())

  const handleSubmit = async () => {
    const requisition: TitleRequisition = {
      requisitionNumber: form.requisitionNumber.trim(),
      requisitionDate: new Date().toISOString(),
      domainOffice: form.domainOffice.trim(),
      verifierName: form.verifierName.trim(),
      verifierRole: form.verifierRole.trim(),
      verificationNotes: form.verificationNotes.trim(),
      authenticityConfirmed: form.authenticityConfirmed,
      conflictDetected: form.conflictDetected,
      litigationDetected: form.litigationDetected,
      mortgageDetected: form.mortgageDetected,
      nameMatchConfirmed: form.nameMatchConfirmed,
      rejectionReason: form.rejectionReason.trim() || null,
    }
    try {
      await recordRequisition.mutateAsync({
        id: verificationCase.id,
        parcelId: verificationCase.parcelId,
        requisition,
      })
      toast.success("Réquisition enregistrée.")
    } catch {
      toast.error("L'enregistrement de la réquisition a échoué. Réessayez.")
    }
  }

  return (
    <div className="space-y-3">
      <p className="text-sm text-muted-foreground">
        Consignez le résultat de la réquisition effectuée au bureau des Domaines.
      </p>

      <div className="grid gap-3 sm:grid-cols-2">
        <FieldRow label="Numéro de réquisition" required>
          <Input
            value={form.requisitionNumber}
            onChange={(e) => setForm((f) => ({ ...f, requisitionNumber: e.target.value }))}
            placeholder="Ex : REQ-2026-0042"
          />
        </FieldRow>
        <FieldRow label="Bureau des Domaines" required>
          <Input
            value={form.domainOffice}
            onChange={(e) => setForm((f) => ({ ...f, domainOffice: e.target.value }))}
          />
        </FieldRow>
        <FieldRow label="Nom du vérificateur" required>
          <Input
            value={form.verifierName}
            onChange={(e) => setForm((f) => ({ ...f, verifierName: e.target.value }))}
            placeholder="Ex : Moussa Koné"
          />
        </FieldRow>
        <FieldRow label="Qualité du vérificateur" required>
          <Input
            value={form.verifierRole}
            onChange={(e) => setForm((f) => ({ ...f, verifierRole: e.target.value }))}
            placeholder="Ex : Agent des Domaines"
          />
        </FieldRow>
      </div>

      <FieldRow label="Observations">
        <Textarea
          value={form.verificationNotes}
          onChange={(e) => setForm((f) => ({ ...f, verificationNotes: e.target.value }))}
          placeholder="Constats effectués lors du contrôle…"
          rows={3}
        />
      </FieldRow>

      <div className="grid gap-2 sm:grid-cols-2">
        <CheckboxRow
          label="Authenticité confirmée"
          checked={form.authenticityConfirmed}
          onChange={(checked) => setForm((f) => ({ ...f, authenticityConfirmed: checked }))}
        />
        <CheckboxRow
          label="Concordance du nom avec le répertoire national"
          checked={form.nameMatchConfirmed}
          onChange={(checked) => setForm((f) => ({ ...f, nameMatchConfirmed: checked }))}
        />
        <CheckboxRow
          label="Conflit détecté"
          checked={form.conflictDetected}
          onChange={(checked) => setForm((f) => ({ ...f, conflictDetected: checked }))}
        />
        <CheckboxRow
          label="Litige détecté"
          checked={form.litigationDetected}
          onChange={(checked) => setForm((f) => ({ ...f, litigationDetected: checked }))}
        />
        <CheckboxRow
          label="Hypothèque bloquante détectée"
          checked={form.mortgageDetected}
          onChange={(checked) => setForm((f) => ({ ...f, mortgageDetected: checked }))}
        />
      </div>

      {needsRejectionReason && (
        <FieldRow label="Motif de rejet" required>
          <Textarea
            value={form.rejectionReason}
            onChange={(e) => setForm((f) => ({ ...f, rejectionReason: e.target.value }))}
            placeholder="Obligatoire lorsque l'authenticité n'est pas confirmée."
            rows={2}
          />
        </FieldRow>
      )}

      {missing && (
        <p className="text-xs text-muted-foreground">
          Renseignez tous les champs obligatoires pour enregistrer la réquisition.
        </p>
      )}

      <Button
        onClick={handleSubmit}
        disabled={missing || recordRequisition.isPending}
        className="gap-2 bg-emerald text-primary-foreground hover:bg-emerald/90"
      >
        {recordRequisition.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Enregistrer la réquisition
      </Button>
    </div>
  )
}

function CheckboxRow({
  label,
  checked,
  onChange,
}: {
  label: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <label className="flex items-center gap-2 rounded-lg border border-border bg-background/40 px-3 py-2 text-sm">
      <Checkbox checked={checked} onCheckedChange={(v) => onChange(v === true)} />
      {label}
    </label>
  )
}

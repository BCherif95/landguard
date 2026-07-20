"use client"

import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from "@/components/ui/sheet"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useQuery } from "@tanstack/react-query"
import { useParcelDetails } from "@/lib/hooks/use-parcel-details"
import { useMonitoringEvents } from "@/lib/hooks/use-monitoring-events"
import { useBlockchainRecords } from "@/lib/hooks/use-blockchain-records"
import { ParcelSummary } from "./parcel-summary"
import { resolveStatusMeta, resolveSeverityMeta, resolveMonitoringTypeMeta } from "@/lib/utils/safe-resolvers"
import { formatDateTime } from "@/lib/api/parcel-display"
import {
  Loader2, Globe, Shield, History, FileText, Link as LinkIcon,
  Users, Download, ExternalLink, MapPin,
  CheckCircle2, FilePlus, Hexagon,
} from "lucide-react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { useTransitionStatus, useIssueTitle } from "@/lib/hooks/use-parcels"
import { useCurrentUser } from "@/lib/hooks/use-current-user"
import { parcelsApi } from "@/lib/api/parcels"
import { heritageApi } from "@/lib/api/heritage"
import { toast } from "sonner"

import { AppErrorBoundary } from "./error-boundary"

const SUCCESSION_STATUS_LABEL: Record<string, string> = {
  DRAFT: "Brouillon",
  IN_VOTING: "Vote des héritiers en cours",
  VALIDATED: "Validé par les héritiers",
  ANCHORED: "Ancré sur la blockchain",
  TRANSFERRED: "Transfert effectué",
  REJECTED: "Rejeté",
}

/** Roles allowed to drive the government certification workflow (PRD Feature 02.2). */
const WORKFLOW_ROLES = ["OFFICER", "LEGAL", "ADMIN"]

export function ParcelDetailSheet() {
  const { parcel, isLoading, isOpen, close, activeTab, setTab } = useParcelDetails()
  const { mutateAsync: transition } = useTransitionStatus()
  const { mutateAsync: issueTitle } = useIssueTitle()
  const currentUser = useCurrentUser()
  const canManageWorkflow = Boolean(currentUser?.role && WORKFLOW_ROLES.includes(currentUser.role))

  const parcelId = parcel?.id ?? null

  const { events: parcelEvents, isLoading: eventsLoading } = useMonitoringEvents(
    { parcelId: parcelId ?? undefined },
    { enabled: isOpen && !!parcelId },
  )
  const { data: anchorRecords, isLoading: anchorsLoading } = useBlockchainRecords(
    { entityId: parcelId ?? undefined },
    { enabled: isOpen && !!parcelId },
  )
  const successionQuery = useQuery({
    queryKey: ["heritage", "plan-by-parcel", parcelId] as const,
    queryFn: () => heritageApi.getPlanByParcel(parcelId!),
    enabled: isOpen && !!parcelId,
    retry: false,
  })

  const handleCertify = async () => {
    if (!parcel) return
    try {
      await transition({ id: parcel.id, status: "CERTIFIED" })
      toast.success("Parcelle certifiée avec succès.")
    } catch {
      toast.error("Erreur lors de la certification.")
    }
  }

  const handleIssueTitle = async () => {
    if (!parcel) return
    try {
      await issueTitle(parcel.id)
      toast.success("Titre Foncier (TF) émis avec succès.")
    } catch {
      toast.error("Erreur lors de l'émission du TF.")
    }
  }

  const handleStartVerification = async () => {
    if (!parcel) return
    try {
      await transition({ id: parcel.id, status: "UNDER_VERIFICATION" })
      toast.success("Vérification démarrée.")
    } catch {
      toast.error("Action non autorisée ou indisponible. Vérifiez vos droits puis réessayez.")
    }
  }

  const handleReject = async () => {
    if (!parcel) return
    try {
      await transition({ id: parcel.id, status: "REJECTED" })
      toast.success("Dossier rejeté.")
    } catch {
      toast.error("Action non autorisée ou indisponible. Vérifiez vos droits puis réessayez.")
    }
  }

  const handleDownloadPdf = () => {
    if (!parcel) return
    window.open(parcelsApi.getTitlePdfUrl(parcel.id), "_blank")
  }

  if (!parcel && !isLoading) return null

  const statusMeta = parcel ? resolveStatusMeta(parcel.status) : null

  // Real lifecycle milestones only — sourced from the parcel record itself.
  const historyEntries = parcel
    ? [
        parcel.lastVerifiedAt && {
          at: parcel.lastVerifiedAt,
          title: "Dernière vérification de la parcelle",
          tone: "bg-emerald shadow-[0_0_10px_rgba(16,185,129,0.5)]",
        },
        parcel.updatedAt && {
          at: parcel.updatedAt,
          title: "Dernière mise à jour du dossier",
          tone: "bg-blue-500 shadow-[0_0_10px_rgba(59,130,246,0.5)]",
        },
        parcel.createdAt && {
          at: parcel.createdAt,
          title: "Enregistrement de la parcelle au registre",
          tone: "bg-muted-foreground/40",
        },
      ].filter((e): e is { at: string; title: string; tone: string } => Boolean(e))
    : []

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && close()}>
      <SheetContent className="sm:max-w-2xl overflow-y-auto p-0 border-l border-border bg-card">
        <AppErrorBoundary name="Détails de la parcelle">
          {isLoading ? (
            <div className="h-full flex items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-emerald" />
            </div>
          ) : parcel && (
            <div className="flex flex-col h-full">
            <div className="p-6 pb-0">
              <SheetHeader className="mb-6">
                <div className="flex items-center gap-2 mb-1">
                  <span className="px-2 py-0.5 rounded bg-emerald/10 text-emerald text-[10px] font-bold uppercase tracking-wider">
                    {statusMeta?.label}
                  </span>
                  <span className="text-muted-foreground text-xs font-mono">{parcel.reference}</span>
                </div>
                <SheetTitle className="text-3xl font-bold tracking-tight">{parcel.name}</SheetTitle>
                <SheetDescription className="sr-only">
                  Fiche détaillée de la parcelle {parcel.name} ({parcel.reference}) : statut, surveillance, historique, documents, ancrages blockchain et succession.
                </SheetDescription>
              </SheetHeader>

              <Tabs value={activeTab} onValueChange={setTab} className="w-full">
                <TabsList className="grid grid-cols-6 w-full bg-secondary p-1 rounded-xl border border-border">
                  <TabsTrigger value="overview" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <Globe className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Général</span>
                  </TabsTrigger>
                  <TabsTrigger value="monitoring" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <Shield className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Surv.</span>
                  </TabsTrigger>
                  <TabsTrigger value="history" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <History className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Hist.</span>
                  </TabsTrigger>
                  <TabsTrigger value="documents" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <FileText className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Docs</span>
                  </TabsTrigger>
                  <TabsTrigger value="blockchain" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <LinkIcon className="h-3.5 w-3.5" /> <span className="hidden sm:inline">BC</span>
                  </TabsTrigger>
                  <TabsTrigger value="heritage" className="text-[10px] uppercase gap-1.5 rounded-lg text-muted-foreground data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                    <Users className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Hér.</span>
                  </TabsTrigger>
                </TabsList>

                <div className="py-6 h-[calc(100vh-250px)] overflow-y-auto pr-2 custom-scrollbar">
                  <TabsContent value="overview" className="mt-0 outline-none">
                    <div className="space-y-6">
                      <ParcelSummary parcel={parcel} />

                      {/* Malian workflow actions — gouvernemental (agents) + retrait du titre (propriétaire). */}
                      {(canManageWorkflow || parcel.status === "TITLE_ISSUED") && (
                        <div className="p-4 rounded-xl border border-emerald/20 bg-emerald/5 space-y-4">
                          <div className="flex items-center justify-between">
                            <h4 className="text-[10px] font-bold uppercase tracking-widest text-emerald flex items-center gap-2">
                              <Shield className="h-3 w-3" />
                              {canManageWorkflow ? "Workflow Gouvernemental" : "Titre Foncier"}
                            </h4>
                          </div>

                          <div className="flex flex-wrap gap-2">
                            {canManageWorkflow && parcel.status === "SUBMITTED" && (
                              <Button
                                onClick={handleStartVerification}
                                className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90"
                              >
                                <CheckCircle2 className="h-3.5 w-3.5 mr-2" /> Démarrer Vérification
                              </Button>
                            )}

                            {canManageWorkflow && parcel.status === "UNDER_VERIFICATION" && (
                              <Button
                                onClick={handleCertify}
                                className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_15px_rgba(16,185,129,0.3)]"
                              >
                                <Shield className="h-3.5 w-3.5 mr-2" /> Certifier la Parcelle
                              </Button>
                            )}

                            {canManageWorkflow && parcel.status === "CERTIFIED" && (
                              <Button
                                onClick={handleIssueTitle}
                                className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_15px_rgba(16,185,129,0.3)]"
                              >
                                <FilePlus className="h-3.5 w-3.5 mr-2" /> Émettre le Titre Foncier (TF)
                              </Button>
                            )}

                            {parcel.status === "TITLE_ISSUED" && (
                              <Button
                                onClick={handleDownloadPdf}
                                className="h-9 text-[10px] uppercase font-bold tracking-widest bg-primary text-primary-foreground hover:bg-primary/90"
                              >
                                <Download className="h-3.5 w-3.5 mr-2" /> Télécharger le Titre (PDF)
                              </Button>
                            )}

                            {canManageWorkflow && ["SUBMITTED", "UNDER_VERIFICATION"].includes(parcel.status) && (
                              <Button
                                variant="ghost"
                                onClick={handleReject}
                                className="h-9 text-[10px] uppercase font-bold tracking-widest text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                              >
                                Rejeter le dossier
                              </Button>
                            )}
                          </div>
                        </div>
                      )}

                      <div className="p-4 rounded-xl border border-border bg-secondary space-y-3">
                        <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground flex items-center gap-2">
                          <MapPin className="h-3 w-3" /> Localisation
                        </h4>
                        <div className="space-y-1">
                          <p className="text-sm font-medium">{parcel.regionLabel}</p>
                          {parcel.centroid && (
                            <p className="text-xs text-muted-foreground font-mono">
                              {parcel.centroid.latitude.toFixed(6)}, {parcel.centroid.longitude.toFixed(6)}
                            </p>
                          )}
                        </div>
                        {parcel.centroid && (
                          <Button
                            asChild
                            variant="secondary"
                            size="sm"
                            className="w-full h-8 text-[10px] gap-2"
                          >
                            <a
                              href={`https://www.openstreetmap.org/?mlat=${parcel.centroid.latitude}&mlon=${parcel.centroid.longitude}#map=17/${parcel.centroid.latitude}/${parcel.centroid.longitude}`}
                              target="_blank"
                              rel="noreferrer"
                            >
                              <ExternalLink className="h-3 w-3" /> Voir sur la carte
                            </a>
                          </Button>
                        )}
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="monitoring" className="outline-none">
                    {eventsLoading ? (
                      <div className="flex items-center justify-center p-12 text-muted-foreground">
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Chargement des événements…
                      </div>
                    ) : parcelEvents.length === 0 ? (
                      <div className="p-12 text-center border border-dashed border-border rounded-2xl bg-secondary">
                        <Shield className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
                        <p className="text-sm font-medium">Aucun événement de surveillance</p>
                        <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[280px] mx-auto">
                          Aucun événement n&apos;a été enregistré pour cette parcelle à ce jour.
                        </p>
                      </div>
                    ) : (
                      <ul className="space-y-3">
                        {parcelEvents.map((event) => {
                          const severity = resolveSeverityMeta(event.severity)
                          const type = resolveMonitoringTypeMeta(event.type)
                          const SeverityIcon = severity.icon
                          return (
                            <li key={event.id} className="flex gap-3 rounded-xl border border-border bg-card p-4 shadow-card">
                              <SeverityIcon className={`mt-0.5 h-4 w-4 shrink-0 ${severity.tone}`} />
                              <div className="min-w-0 flex-1">
                                <div className="flex items-center gap-2">
                                  <span className={`font-mono text-[10px] uppercase tracking-widest ${severity.tone}`}>
                                    {severity.label} · {type.label}
                                  </span>
                                  <span className="ml-auto font-mono text-[10px] text-muted-foreground">
                                    {formatDateTime(event.detectedAt)}
                                  </span>
                                </div>
                                <p className="mt-1 text-sm">{event.description}</p>
                                {event.resolved && (
                                  <p className="mt-1 text-[10px] uppercase tracking-widest text-emerald">Résolu</p>
                                )}
                              </div>
                            </li>
                          )
                        })}
                      </ul>
                    )}
                  </TabsContent>

                  <TabsContent value="history" className="outline-none">
                    <div className="space-y-6 pl-2">
                      {historyEntries.map((entry) => (
                        <div key={entry.title} className="relative pl-8 border-l border-border pb-6 last:pb-0">
                          <div className={`absolute -left-[5px] top-0 h-2.5 w-2.5 rounded-full ${entry.tone}`} />
                          <p className="text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1">
                            {formatDateTime(entry.at)}
                          </p>
                          <p className="text-sm font-medium">{entry.title}</p>
                        </div>
                      ))}
                    </div>
                  </TabsContent>

                  <TabsContent value="documents" className="outline-none">
                    <div className="p-12 text-center border border-dashed border-border rounded-2xl bg-secondary">
                      <FileText className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
                      <p className="text-sm font-medium">Consultation des pièces indisponible ici</p>
                      <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[300px] mx-auto">
                        Les pièces justificatives sont conservées de manière sécurisée. Leur consultation
                        depuis cette fiche n&apos;est pas encore proposée.
                      </p>
                    </div>
                  </TabsContent>

                  <TabsContent value="blockchain" className="outline-none">
                    {anchorsLoading ? (
                      <div className="flex items-center justify-center p-12 text-muted-foreground">
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Chargement des ancrages…
                      </div>
                    ) : !anchorRecords || anchorRecords.length === 0 ? (
                      <div className="p-12 text-center border border-dashed border-border rounded-2xl bg-secondary">
                        <Hexagon className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
                        <p className="text-sm font-medium">Aucun ancrage blockchain</p>
                        <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[300px] mx-auto">
                          Aucune empreinte n&apos;a encore été ancrée pour cette parcelle.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {anchorRecords.map((record) => (
                          <div key={record.id} className="p-4 rounded-2xl bg-navy border border-navy font-mono text-[10px] break-all leading-relaxed shadow-card">
                            <p className="text-emerald mb-2 flex items-center gap-2">
                              <span className="h-1.5 w-1.5 rounded-full bg-emerald" />
                              Ancrage · {formatDateTime(record.anchoredAt)}
                            </p>
                            <div className="space-y-1 text-white/60">
                              <p><span className="text-emerald/70">EMPREINTE :</span> {record.hash}</p>
                              <p><span className="text-emerald/70">RÉSEAU :</span> {record.network}</p>
                              <p><span className="text-emerald/70">TRANSACTION :</span> {record.transactionId}</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </TabsContent>

                  <TabsContent value="heritage" className="outline-none">
                    {successionQuery.isLoading ? (
                      <div className="flex items-center justify-center p-12 text-muted-foreground">
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Chargement du plan de succession…
                      </div>
                    ) : successionQuery.data ? (
                      <div className="p-6 border border-border rounded-2xl bg-secondary space-y-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Users className="h-5 w-5 text-emerald" />
                            <span className="text-xs font-bold uppercase tracking-widest text-emerald">Plan de succession</span>
                          </div>
                          <span className="text-[10px] font-mono uppercase tracking-widest text-muted-foreground">
                            {SUCCESSION_STATUS_LABEL[successionQuery.data.status] ?? successionQuery.data.status}
                          </span>
                        </div>
                        <ul className="space-y-2">
                          {successionQuery.data.heirs.map((heir) => (
                            <li key={heir.id} className="flex items-center justify-between rounded-lg border border-border bg-card px-3 py-2 text-sm">
                              <span>{heir.fullName} <span className="text-muted-foreground">· {heir.relation}</span></span>
                              <span className="font-mono text-xs">{heir.sharePercentage}%</span>
                            </li>
                          ))}
                        </ul>
                        <Button asChild variant="secondary" className="w-full text-[10px] uppercase font-bold tracking-widest h-9">
                          <Link href="/heritage">Gérer la succession</Link>
                        </Button>
                      </div>
                    ) : (
                      <div className="p-6 border border-amber-200 rounded-2xl bg-amber-50 space-y-4">
                        <div className="flex items-center gap-2">
                          <Users className="h-5 w-5 text-amber-600" />
                          <span className="text-xs font-bold uppercase tracking-widest text-amber-700">Plan de succession foncière</span>
                        </div>
                        <p className="text-xs text-amber-800/80 leading-relaxed">
                          Aucun héritier n&apos;est actuellement désigné pour cette parcelle.
                          Configurez votre plan de succession pour assurer la transmission sécurisée de vos terres.
                        </p>
                        <Button asChild className="w-full bg-amber-600 hover:bg-amber-700 text-white text-[10px] uppercase font-bold tracking-widest h-9">
                          <Link href="/heritage">Configurer la succession</Link>
                        </Button>
                      </div>
                    )}
                  </TabsContent>
                </div>
              </Tabs>
            </div>
          </div>
        )}
        </AppErrorBoundary>
      </SheetContent>
    </Sheet>
  )
}

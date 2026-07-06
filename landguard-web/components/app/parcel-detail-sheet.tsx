"use client"

import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
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

export function ParcelDetailSheet() {
  const { parcel, isLoading, isOpen, close, activeTab, setTab } = useParcelDetails()
  const { mutateAsync: transition } = useTransitionStatus()
  const { mutateAsync: issueTitle } = useIssueTitle()

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
          tone: "bg-white/70",
        },
      ].filter((e): e is { at: string; title: string; tone: string } => Boolean(e))
    : []

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && close()}>
      <SheetContent className="sm:max-w-2xl overflow-y-auto p-0 border-l border-white/10 bg-black/95 backdrop-blur-2xl">
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
              </SheetHeader>

              <Tabs value={activeTab} onValueChange={setTab} className="w-full">
                <TabsList className="grid grid-cols-6 w-full bg-white/5 p-1 rounded-xl border border-white/10">
                  <TabsTrigger value="overview" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <Globe className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Général</span>
                  </TabsTrigger>
                  <TabsTrigger value="monitoring" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <Shield className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Surv.</span>
                  </TabsTrigger>
                  <TabsTrigger value="history" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <History className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Hist.</span>
                  </TabsTrigger>
                  <TabsTrigger value="documents" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <FileText className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Docs</span>
                  </TabsTrigger>
                  <TabsTrigger value="blockchain" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <LinkIcon className="h-3.5 w-3.5" /> <span className="hidden sm:inline">BC</span>
                  </TabsTrigger>
                  <TabsTrigger value="heritage" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <Users className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Hér.</span>
                  </TabsTrigger>
                </TabsList>

                <div className="py-6 h-[calc(100vh-250px)] overflow-y-auto pr-2 custom-scrollbar">
                  <TabsContent value="overview" className="mt-0 outline-none">
                    <div className="space-y-6">
                      <ParcelSummary parcel={parcel} />

                      {/* Malian Workflow Actions */}
                      <div className="p-4 rounded-xl border border-emerald/20 bg-emerald/5 space-y-4">
                        <div className="flex items-center justify-between">
                          <h4 className="text-[10px] font-bold uppercase tracking-widest text-emerald flex items-center gap-2">
                            <Shield className="h-3 w-3" /> Workflow Gouvernemental
                          </h4>
                        </div>

                        <div className="flex flex-wrap gap-2">
                          {parcel.status === "SUBMITTED" && (
                            <Button
                              onClick={() => transition({ id: parcel.id, status: "UNDER_VERIFICATION" })}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90"
                            >
                              <CheckCircle2 className="h-3.5 w-3.5 mr-2" /> Démarrer Vérification
                            </Button>
                          )}

                          {parcel.status === "UNDER_VERIFICATION" && (
                            <Button
                              onClick={handleCertify}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_15px_rgba(16,185,129,0.3)]"
                            >
                              <Shield className="h-3.5 w-3.5 mr-2" /> Certifier la Parcelle
                            </Button>
                          )}

                          {parcel.status === "CERTIFIED" && (
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
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-white text-black hover:bg-zinc-200"
                            >
                              <Download className="h-3.5 w-3.5 mr-2" /> Télécharger le Titre (PDF)
                            </Button>
                          )}

                          {["SUBMITTED", "UNDER_VERIFICATION"].includes(parcel.status) && (
                            <Button
                              variant="ghost"
                              onClick={() => transition({ id: parcel.id, status: "REJECTED" })}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest text-zinc-500 hover:text-white hover:bg-red-500/10"
                            >
                              Rejeter le dossier
                            </Button>
                          )}
                        </div>
                      </div>

                      <div className="p-4 rounded-xl border border-white/10 bg-white/5 space-y-3">
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
                            className="w-full h-8 text-[10px] gap-2 bg-white/10 hover:bg-white/20"
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
                      <div className="p-12 text-center border border-dashed border-white/10 rounded-2xl bg-white/5">
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
                            <li key={event.id} className="flex gap-3 rounded-xl border border-white/10 bg-white/5 p-4">
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
                        <div key={entry.title} className="relative pl-8 border-l border-white/10 pb-6 last:pb-0">
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
                    <div className="p-12 text-center border border-dashed border-white/10 rounded-2xl bg-white/5">
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
                      <div className="p-12 text-center border border-dashed border-white/10 rounded-2xl bg-white/5">
                        <Hexagon className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
                        <p className="text-sm font-medium">Aucun ancrage blockchain</p>
                        <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[300px] mx-auto">
                          Aucune empreinte n&apos;a encore été ancrée pour cette parcelle.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {anchorRecords.map((record) => (
                          <div key={record.id} className="p-4 rounded-2xl bg-zinc-950 border border-white/5 font-mono text-[10px] break-all leading-relaxed">
                            <p className="text-emerald/60 mb-2 flex items-center gap-2">
                              <span className="h-1.5 w-1.5 rounded-full bg-emerald" />
                              Ancrage · {formatDateTime(record.anchoredAt)}
                            </p>
                            <div className="space-y-1 text-zinc-400">
                              <p><span className="text-emerald/40">EMPREINTE :</span> {record.hash}</p>
                              <p><span className="text-emerald/40">RÉSEAU :</span> {record.network}</p>
                              <p><span className="text-emerald/40">TRANSACTION :</span> {record.transactionId}</p>
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
                      <div className="p-6 border border-white/10 rounded-2xl bg-white/5 space-y-4">
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
                            <li key={heir.id} className="flex items-center justify-between rounded-lg border border-white/10 bg-black/30 px-3 py-2 text-sm">
                              <span>{heir.fullName} <span className="text-muted-foreground">· {heir.relation}</span></span>
                              <span className="font-mono text-xs">{heir.sharePercentage}%</span>
                            </li>
                          ))}
                        </ul>
                        <Button asChild variant="secondary" className="w-full bg-white/10 hover:bg-white/20 text-[10px] uppercase font-bold tracking-widest h-9">
                          <Link href="/heritage">Gérer la succession</Link>
                        </Button>
                      </div>
                    ) : (
                      <div className="p-6 border border-orange-500/20 rounded-2xl bg-orange-500/5 space-y-4">
                        <div className="flex items-center gap-2">
                          <Users className="h-5 w-5 text-orange-500" />
                          <span className="text-xs font-bold uppercase tracking-widest text-orange-500">Plan de succession foncière</span>
                        </div>
                        <p className="text-xs text-orange-200/70 leading-relaxed">
                          Aucun héritier n&apos;est actuellement désigné pour cette parcelle.
                          Configurez votre plan de succession pour assurer la transmission sécurisée de vos terres.
                        </p>
                        <Button asChild className="w-full bg-orange-600 hover:bg-orange-700 text-white text-[10px] uppercase font-bold tracking-widest h-9">
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

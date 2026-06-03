"use client"

import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useParcelDetails } from "@/lib/hooks/use-parcel-details"
import { ParcelSummary } from "./parcel-summary"
import { toLegacyStatus, toLegacyRisk } from "@/lib/api/parcel-display"
import { 
  Loader2, Globe, Shield, History, FileText, Link, 
  Users, Upload, Download, ExternalLink, MapPin,
  CheckCircle2, FilePlus
} from "lucide-react"
import type { Parcel as LegacyParcel } from "@/lib/types"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { useTransitionStatus, useIssueTitle } from "@/lib/hooks/use-parcels"
import { parcelsApi } from "@/lib/api/parcels"
import { toast } from "sonner"

import { AppErrorBoundary } from "./error-boundary"

// ... (top level)
export function ParcelDetailSheet() {
  const { parcel, isLoading, isOpen, close, activeTab, setTab } = useParcelDetails()
  const { mutateAsync: transition } = useTransitionStatus()
  const { mutateAsync: issueTitle } = useIssueTitle()

  const handleCertify = async () => {
    if (!parcel) return
    try {
      await transition({ id: parcel.id, status: "CERTIFIED" })
      toast.success("Parcelle certifiée avec succès.")
    } catch (e) {
      toast.error("Erreur lors de la certification.")
    }
  }

  const handleIssueTitle = async () => {
    if (!parcel) return
    try {
      await issueTitle(parcel.id)
      toast.success("Titre Foncier (TF) émis avec succès.")
    } catch (e) {
      toast.error("Erreur lors de l'émission du TF.")
    }
  }

  const handleDownloadPdf = () => {
    if (!parcel) return
    window.open(parcelsApi.getTitlePdfUrl(parcel.id), "_blank")
  }

  useEffect(() => {
    if (isOpen) {
      console.log("Detail Sheet opened for parcel:", parcel?.id)
    }
  }, [isOpen, parcel])

  if (!parcel && !isLoading) return null

  const legacyParcel: LegacyParcel | null = parcel ? {
    id: parcel.id,
    reference: parcel.reference,
    name: parcel.name,
    owner: parcel.ownerLabel,
    region: parcel.regionLabel,
    area: parseFloat(parcel.areaHectares || "0"),
    status: toLegacyStatus(parcel.status),
    risk: toLegacyRisk(parcel.riskLevel),
    riskScore: parcel.riskScore || 0,
    trustScore: parcel.trustScore || 0,
    estimatedValue: parcel.estimatedValueXof || 0,
    coordinates: {
      lat: parcel.centroid?.latitude || 0,
      lng: parcel.centroid?.longitude || 0,
    },
    blockchainHash: "0x" + parcel.id.replace(/-/g, "").substring(0, 32),
    titleNumber: parcel.titleNumber,
    lastVerifiedAt: parcel.lastVerifiedAt ? new Date(parcel.lastVerifiedAt).toLocaleDateString("fr-FR") : "Jamais",
    polygon: parcel.geometry?.coordinates?.[0]?.map(([lng, lat]) => ({ x: lng, y: lat })) || [],
  } : null

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && close()}>
      <SheetContent className="sm:max-w-2xl overflow-y-auto p-0 border-l border-white/10 bg-black/95 backdrop-blur-2xl">
        <AppErrorBoundary name="Détails de la parcelle">
          {isLoading ? (
            <div className="h-full flex items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-emerald" />
            </div>
          ) : legacyParcel && (
            <div className="flex flex-col h-full">
              {/* ... rest of the component */}
            <div className="p-6 pb-0">
              <SheetHeader className="mb-6">
                <div className="flex items-center gap-2 mb-1">
                  <span className="px-2 py-0.5 rounded bg-emerald/10 text-emerald text-[10px] font-bold uppercase tracking-wider">
                    {legacyParcel.status}
                  </span>
                  <span className="text-muted-foreground text-xs font-mono">{legacyParcel.reference}</span>
                </div>
                <SheetTitle className="text-3xl font-bold tracking-tight">{legacyParcel.name}</SheetTitle>
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
                    <Link className="h-3.5 w-3.5" /> <span className="hidden sm:inline">BC</span>
                  </TabsTrigger>
                  <TabsTrigger value="heritage" className="text-[10px] uppercase gap-1.5 rounded-lg data-[state=active]:bg-emerald data-[state=active]:text-white">
                    <Users className="h-3.5 w-3.5" /> <span className="hidden sm:inline">Hér.</span>
                  </TabsTrigger>
                </TabsList>

                <div className="py-6 h-[calc(100vh-250px)] overflow-y-auto pr-2 custom-scrollbar">
                  <TabsContent value="overview" className="mt-0 outline-none">
                    <div className="space-y-6">
                      <ParcelSummary parcel={legacyParcel} />

                      {/* Malian Workflow Actions */}
                      <div className="p-4 rounded-xl border border-emerald/20 bg-emerald/5 space-y-4">
                        <div className="flex items-center justify-between">
                          <h4 className="text-[10px] font-bold uppercase tracking-widest text-emerald flex items-center gap-2">
                            <Shield className="h-3 w-3" /> Workflow Gouvernemental
                          </h4>
                          <span className="text-[9px] font-mono text-emerald/60 uppercase">Sécurisé</span>
                        </div>
                        
                        <div className="flex flex-wrap gap-2">
                          {parcel?.status === "SUBMITTED" && (
                            <Button 
                              onClick={() => parcel && transition({ id: parcel.id, status: "UNDER_VERIFICATION" })}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90"
                            >
                              <CheckCircle2 className="h-3.5 w-3.5 mr-2" /> Démarrer Vérification
                            </Button>
                          )}
                          
                          {parcel?.status === "UNDER_VERIFICATION" && (
                            <Button 
                              onClick={handleCertify}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_15px_rgba(16,185,129,0.3)]"
                            >
                              <Shield className="h-3.5 w-3.5 mr-2" /> Certifier la Parcelle
                            </Button>
                          )}

                          {parcel?.status === "CERTIFIED" && (
                            <Button 
                              onClick={handleIssueTitle}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-emerald text-white hover:bg-emerald/90 shadow-[0_0_15px_rgba(16,185,129,0.3)]"
                            >
                              <FilePlus className="h-3.5 w-3.5 mr-2" /> Émettre le Titre Foncier (TF)
                            </Button>
                          )}

                          {parcel?.status === "TITLE_ISSUED" && (
                            <Button 
                              onClick={handleDownloadPdf}
                              className="h-9 text-[10px] uppercase font-bold tracking-widest bg-white text-black hover:bg-zinc-200"
                            >
                              <Download className="h-3.5 w-3.5 mr-2" /> Télécharger le Titre (PDF)
                            </Button>
                          )}

                          {parcel && ["SUBMITTED", "UNDER_VERIFICATION"].includes(parcel.status) && (
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
                      
                      <div className="grid grid-cols-2 gap-4">
                        <div className="p-4 rounded-xl border border-white/10 bg-white/5 space-y-3">
                          <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground flex items-center gap-2">
                            <MapPin className="h-3 w-3" /> Localisation
                          </h4>
                          <div className="space-y-1">
                            <p className="text-sm font-medium">{legacyParcel.region}</p>
                            <p className="text-xs text-muted-foreground font-mono">
                              {legacyParcel.coordinates.lat.toFixed(6)}, {legacyParcel.coordinates.lng.toFixed(6)}
                            </p>
                          </div>
                          <Button variant="secondary" size="sm" className="w-full h-8 text-[10px] gap-2 bg-white/10 hover:bg-white/20">
                            <ExternalLink className="h-3 w-3" /> Voir sur Maps
                          </Button>
                        </div>
                        
                        <div className="p-4 rounded-xl border border-white/10 bg-white/5 space-y-3">
                          <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground flex items-center gap-2">
                            <Shield className="h-3 w-3" /> Certification
                          </h4>
                          <div className="space-y-1">
                            <p className="text-sm font-medium">Rang A - Haute Confiance</p>
                            <p className="text-xs text-emerald font-mono">Validé le {legacyParcel.lastVerifiedAt}</p>
                          </div>
                          <div className="h-1.5 w-full bg-white/10 rounded-full overflow-hidden">
                            <div className="h-full bg-emerald w-[92%] rounded-full" />
                          </div>
                        </div>
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="monitoring" className="outline-none">
                    <div className="p-12 text-center border border-dashed border-white/10 rounded-2xl bg-white/5">
                      <Shield className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
                      <p className="text-sm font-medium">Surveillance Sentinel-2 Active</p>
                      <p className="text-xs text-muted-foreground mt-2 leading-relaxed max-w-[240px] mx-auto">
                        Aucune intrusion ou changement morphologique détecté sur les 30 derniers jours.
                      </p>
                    </div>
                  </TabsContent>

                  <TabsContent value="history" className="outline-none">
                    <div className="space-y-6 pl-2">
                      <div className="relative pl-8 border-l border-white/10 pb-6">
                        <div className="absolute -left-[5px] top-0 h-2.5 w-2.5 rounded-full bg-emerald shadow-[0_0_10px_rgba(16,185,129,0.5)]" />
                        <p className="text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1">Aujourd'hui</p>
                        <p className="text-sm font-medium">Consultation du dossier propriétaire</p>
                        <p className="text-xs text-muted-foreground mt-1">Accès sécurisé via IP 192.168.1.XX</p>
                      </div>
                      <div className="relative pl-8 border-l border-white/10 pb-6">
                        <div className="absolute -left-[5px] top-0 h-2.5 w-2.5 rounded-full bg-blue-500 shadow-[0_0_10px_rgba(59,130,246,0.5)]" />
                        <p className="text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1">12 Mai 2026</p>
                        <p className="text-sm font-medium">Ancrage blockchain réussi</p>
                        <p className="text-xs text-muted-foreground mt-1">Hash: 0x82f...a12b validé sur Polygon</p>
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="documents" className="outline-none">
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h4 className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Pièces justificatives certifiées</h4>
                        <Button variant="outline" size="sm" className="h-7 text-[10px] gap-1 bg-white/5 border-white/10 hover:bg-white/10">
                          <Upload className="h-3 w-3" /> Ajouter
                        </Button>
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        {[
                          { name: "Titre Foncier (Original)", size: "2.4 MB", type: "PDF" },
                          { name: "Plan de bornage signé", size: "1.1 MB", type: "PDF" },
                          { name: "Acte de cession notarié", size: "3.2 MB", type: "PDF" },
                          { name: "ID Propriétaire (NINA)", size: "0.8 MB", type: "JPG" },
                        ].map((doc, i) => (
                          <div key={i} className="group p-3 border border-white/10 rounded-xl hover:bg-emerald/5 hover:border-emerald/20 cursor-pointer transition-all flex items-center gap-3">
                            <div className="h-10 w-10 rounded-lg bg-blue-500/10 flex items-center justify-center text-blue-500 group-hover:bg-emerald/10 group-hover:text-emerald transition-colors">
                              <FileText className="h-5 w-5" />
                            </div>
                            <div className="overflow-hidden flex-1">
                              <p className="text-[11px] font-medium truncate">{doc.name}</p>
                              <div className="flex items-center gap-2 mt-0.5">
                                <span className="text-[9px] text-muted-foreground uppercase font-mono">{doc.type}</span>
                                <span className="text-[9px] text-muted-foreground">•</span>
                                <span className="text-[9px] text-muted-foreground font-mono">{doc.size}</span>
                              </div>
                            </div>
                            <Button variant="ghost" size="icon" className="h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity">
                              <Download className="h-4 w-4" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="blockchain" className="outline-none">
                    <div className="p-6 rounded-2xl bg-zinc-950 border border-white/5 font-mono text-[10px] break-all leading-relaxed shadow-inner">
                      <p className="text-emerald/60 mb-3 flex items-center gap-2">
                        <span className="h-1.5 w-1.5 rounded-full bg-emerald animate-pulse" />
                        // BLOCKCHAIN ANCHOR RECORD
                      </p>
                      <div className="space-y-1 text-zinc-400">
                        <p><span className="text-emerald/40">TX_HASH:</span> {legacyParcel.blockchainHash}</p>
                        <p><span className="text-emerald/40">BLOCK:</span> 1849201</p>
                        <p><span className="text-emerald/40">NETWORK:</span> Polygon Mainnet</p>
                        <p><span className="text-emerald/40">TIMESTAMP:</span> {parcel?.createdAt}</p>
                        <p><span className="text-emerald/40">METHOD:</span> EIP-712 Meta-Transaction</p>
                      </div>
                      <div className="mt-4 pt-4 border-t border-white/5">
                        <p className="text-emerald font-bold tracking-widest text-center">VERIFIED BY LANDGUARD NODE</p>
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="heritage" className="outline-none">
                    <div className="p-6 border border-orange-500/20 rounded-2xl bg-orange-500/5 space-y-4">
                      <div className="flex items-center gap-2">
                        <Users className="h-5 w-5 text-orange-500" />
                        <span className="text-xs font-bold uppercase tracking-widest text-orange-500">Plan de succession foncière</span>
                      </div>
                      <p className="text-xs text-orange-200/70 leading-relaxed">
                        Aucun héritier n'est actuellement désigné pour cette parcelle. 
                        Configurez votre plan de succession pour assurer la transmission sécurisée de vos terres en cas de décès.
                      </p>
                      <Button className="w-full bg-orange-600 hover:bg-orange-700 text-white text-[10px] uppercase font-bold tracking-widest h-9">
                        Configurer la succession
                      </Button>
                    </div>
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

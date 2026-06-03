"use client"

import { useState } from "react"
import { 
  Search, 
  Shield, 
  ShieldCheck,
  Download, 
  CheckCircle2, 
  Clock,
  AlertCircle,
  Gavel,
  History,
  Fingerprint
} from "lucide-react"
import { useParcels } from "@/lib/hooks/use-parcels"
import { legalApi } from "@/lib/api/legal"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useQuery } from "@tanstack/react-query"
import { format } from "date-fns"
import { fr } from "date-fns/locale"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { resolveStatusMeta } from "@/lib/utils/safe-resolvers"

export default function PreuvesPage() {
  const { data: parcels, isLoading: parcelsLoading } = useParcels()
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedParcelId, setSelectedParcelId] = useState<string | null>(null)
  
  const selectedParcel = (parcels || []).find(p => p.id === selectedParcelId)

  const { data: dossier, isLoading: dossierLoading } = useQuery({
    queryKey: ["legal-dossier", selectedParcelId],
    queryFn: () => selectedParcelId ? legalApi.generateProof(selectedParcelId) : null,
    enabled: !!selectedParcelId
  })

  const { data: disputes } = useQuery({
    queryKey: ["legal-disputes"],
    queryFn: legalApi.listDisputes
  })

  const filteredParcels = (parcels || []).filter(p => 
    (p.name || "").toLowerCase().includes(searchTerm.toLowerCase()) || 
    (p.reference || "").toLowerCase().includes(searchTerm.toLowerCase())
  )

  const parcelDisputes = (disputes || []).filter(d => d.parcelId === selectedParcelId)

  return (
    <AppErrorBoundary name="Centre d'Intelligence Juridique">
      <div className="flex flex-col gap-6 p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Centre d&apos;Intelligence Juridique</h1>
            <p className="text-muted-foreground">Espace de preuves judiciaires et gestion des éléments de preuve.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="gap-2">
              <History className="h-4 w-4" />
              Archives
            </Button>
            <Button className="gap-2 bg-emerald-600 hover:bg-emerald-700">
              <Fingerprint className="h-4 w-4" />
              Vérifier l&apos;Intégrité des Preuves
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-12 gap-6">
          {/* Left: Parcel List */}
          <div className="col-span-12 lg:col-span-4 space-y-4">
            <Card className="h-[calc(100vh-220px)] flex flex-col">
              <CardHeader className="pb-3">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Rechercher des terres enregistrées..."
                    className="pl-9"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </CardHeader>
              <CardContent className="flex-1 overflow-y-auto p-0 border-t">
                {parcelsLoading ? (
                  <div className="p-8 text-center text-muted-foreground">Chargement des parcelles...</div>
                ) : (
                  <div className="divide-y">
                    {filteredParcels.map((parcel) => {
                      const statusMeta = resolveStatusMeta(parcel.status)
                      return (
                        <button
                          key={parcel.id}
                          onClick={() => setSelectedParcelId(parcel.id)}
                          className={`w-full text-left p-4 transition-colors hover:bg-muted/50 ${
                            parcel.id === selectedParcelId ? "bg-emerald-500/5 border-l-4 border-l-emerald-500" : ""
                          }`}
                        >
                          <div className="flex items-start justify-between mb-1">
                            <span className="font-semibold text-sm truncate pr-2">{parcel.name}</span>
                            <Badge variant={parcel.status === "DISPUTED" ? "destructive" : "outline"} className={`text-[10px] h-4 ${statusMeta.tone}`}>
                              {statusMeta.label}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] text-muted-foreground font-mono">REF: {parcel.reference}</span>
                            <span className="text-[10px] font-bold text-emerald-600">{parcel.trustScore}% Confiance</span>
                          </div>
                        </button>
                      )
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Right: Evidence Workspace */}
          <div className="col-span-12 lg:col-span-8">
            {selectedParcel ? (
              <div className="space-y-6">
                {/* Summary Stats */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card className="bg-emerald-500/5 border-emerald-500/20">
                    <CardContent className="pt-4">
                      <div className="flex items-center gap-2 mb-2">
                        <Shield className="h-4 w-4 text-emerald-500" />
                        <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Score d&apos;Intégrité</span>
                      </div>
                      <div className="text-2xl font-bold text-emerald-600">{selectedParcel.trustScore}/100</div>
                      <p className="text-[10px] text-muted-foreground mt-1">Basé sur les données blockchain et terrain</p>
                    </CardContent>
                  </Card>
                  <Card className={parcelDisputes.length ? "bg-red-500/5 border-red-500/20" : ""}>
                    <CardContent className="pt-4">
                      <div className="flex items-center gap-2 mb-2">
                        <Gavel className={`h-4 w-4 ${parcelDisputes.length ? "text-red-500" : "text-muted-foreground"}`} />
                        <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Litiges Actifs</span>
                      </div>
                      <div className={`text-2xl font-bold ${parcelDisputes.length ? "text-red-600" : ""}`}>
                        {parcelDisputes.length}
                      </div>
                      <p className="text-[10px] text-muted-foreground mt-1">En attente de résolution judiciaire</p>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardContent className="pt-4">
                      <div className="flex items-center gap-2 mb-2">
                        <Clock className="h-4 w-4 text-blue-500" />
                        <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Surveillance</span>
                      </div>
                      <div className="text-2xl font-bold">100%</div>
                      <p className="text-[10px] text-muted-foreground mt-1">Couverture satellite continue</p>
                    </CardContent>
                  </Card>
                </div>

                <Tabs defaultValue="dossier" className="w-full">
                  <TabsList className="grid w-full grid-cols-4">
                    <TabsTrigger value="dossier">Dossier Juridique</TabsTrigger>
                    <TabsTrigger value="disputes">Litiges</TabsTrigger>
                    <TabsTrigger value="blockchain">Preuve Blockchain</TabsTrigger>
                    <TabsTrigger value="history">Historique</TabsTrigger>
                  </TabsList>
                  
                  <TabsContent value="dossier" className="mt-4 space-y-4">
                    {dossierLoading ? (
                      <div className="p-12 text-center text-muted-foreground">Compilation du dossier juridique...</div>
                    ) : dossier ? (
                      <div className="space-y-4">
                        <Card className="border-emerald-500/20 shadow-lg">
                          <CardHeader className="bg-emerald-500/5 pb-4">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center gap-3">
                                <div className="bg-emerald-500 text-white p-2 rounded-md">
                                  <ShieldCheck className="h-6 w-6" />
                                </div>
                                <div>
                                  <CardTitle className="text-xl">Dossier Foncier Immuable</CardTitle>
                                  <CardDescription>Généré le {dossier.generatedAt ? format(new Date(dossier.generatedAt), "PPP", { locale: fr }) : "Date inconnue"}</CardDescription>
                                </div>
                              </div>
                              <Button 
                                className="bg-emerald-600 hover:bg-emerald-700 gap-2"
                                onClick={() => window.open(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/legal/export-dossier?parcelId=${selectedParcelId}`, '_blank')}
                              >
                                <Download className="h-4 w-4" />
                                Télécharger le Dossier PDF
                              </Button>
                            </div>
                          </CardHeader>
                          <CardContent className="pt-6 space-y-6">
                            <div className="grid grid-cols-2 gap-8">
                              <div className="space-y-4">
                                <h3 className="text-sm font-bold uppercase tracking-widest text-muted-foreground">Informations sur la parcelle</h3>
                                <div className="space-y-2">
                                  <div className="flex justify-between border-b pb-1">
                                    <span className="text-sm text-muted-foreground">Réf. Cadastrale</span>
                                    <span className="text-sm font-mono font-bold">{dossier.parcel?.reference || "N/A"}</span>
                                  </div>
                                  <div className="flex justify-between border-b pb-1">
                                    <span className="text-sm text-muted-foreground">Propriétaire enregistré</span>
                                    <span className="text-sm font-bold">{dossier.parcel?.ownerLabel || "Inconnu"}</span>
                                  </div>
                                  <div className="flex justify-between border-b pb-1">
                                    <span className="text-sm text-muted-foreground">Superficie</span>
                                    <span className="text-sm font-bold">{dossier.parcel?.areaHectares || "0"} Hectares</span>
                                  </div>
                                  <div className="flex justify-between border-b pb-1">
                                    <span className="text-sm text-muted-foreground">Statut de Certification</span>
                                    <Badge variant="outline" className="text-emerald-600 bg-emerald-50">CERTIFIÉ</Badge>
                                  </div>
                                </div>
                              </div>
                              <div className="flex flex-col items-center justify-center p-4 bg-muted/30 rounded-lg border-2 border-dashed">
                                <div className="bg-white p-2 rounded-lg shadow-sm mb-3">
                                  <div className="h-32 w-32 bg-slate-100 flex items-center justify-center text-[10px] text-muted-foreground text-center p-4">
                                    CODE QR<br/>{(dossier.qrCodeContent || "").substring(0, 20)}...
                                  </div>
                                </div>
                                <p className="text-[10px] text-center text-muted-foreground uppercase font-bold tracking-tighter">
                                  Scanner pour vérifier sur la blockchain
                                </p>
                              </div>
                            </div>
                            
                            <div className="space-y-4">
                              <h3 className="text-sm font-bold uppercase tracking-widest text-muted-foreground">Preuve d&apos;Ancrage Blockchain</h3>
                              <div className="rounded-md bg-slate-950 p-4 text-emerald-500 font-mono text-[10px] overflow-hidden">
                                <div className="flex gap-4 mb-2">
                                  <span className="text-muted-foreground/50">HORODATAGE :</span>
                                  <span>{dossier.generatedAt || "N/A"}</span>
                                </div>
                                <div className="flex gap-4 mb-2">
                                  <span className="text-muted-foreground/50">ID_PARCELLE :</span>
                                  <span>{dossier.parcel?.id || "N/A"}</span>
                                </div>
                                <div className="flex gap-4 mb-2">
                                  <span className="text-muted-foreground/50">HASH_RACINE :</span>
                                  <span className="break-all">{dossier.blockchainHash || "N/A"}</span>
                                </div>
                                <div className="flex gap-4">
                                  <span className="text-muted-foreground/50">RÉSEAU :</span>
                                  <span>LANDGUARD-GOV-MAINNET</span>
                                </div>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      </div>
                    ) : null}
                  </TabsContent>
                  
                  <TabsContent value="disputes" className="mt-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-lg">Historique des Litiges Juridiques</CardTitle>
                      </CardHeader>
                      <CardContent>
                        {parcelDisputes.length === 0 ? (
                          <div className="flex flex-col items-center py-12 text-center">
                            <CheckCircle2 className="h-12 w-12 text-emerald-500 mb-4" />
                            <h4 className="font-bold">Aucun litige trouvé</h4>
                            <p className="text-sm text-muted-foreground">Cette parcelle a un casier judiciaire vierge.</p>
                          </div>
                        ) : (
                          <div className="space-y-4">
                            {parcelDisputes.map(dispute => (
                              <div key={dispute.id} className="flex items-start gap-4 p-4 rounded-lg border bg-muted/10">
                                <div className={`p-2 rounded-full ${dispute.status === 'OPEN' ? 'bg-red-500/10 text-red-500' : 'bg-emerald-500/10 text-emerald-500'}`}>
                                  {dispute.status === 'OPEN' ? <AlertCircle className="h-5 w-5" /> : <CheckCircle2 className="h-5 w-5" />}
                                </div>
                                <div className="flex-1">
                                  <div className="flex items-center justify-between mb-1">
                                    <h4 className="font-bold text-sm">{dispute.reason}</h4>
                                    <Badge variant={dispute.status === 'OPEN' ? 'destructive' : 'default'}>{dispute.status === 'OPEN' ? 'OUVERT' : dispute.status}</Badge>
                                  </div>
                                  <p className="text-xs text-muted-foreground mb-2">Ouvert le {dispute.openedAt ? format(new Date(dispute.openedAt), "PPP", { locale: fr }) : "Date inconnue"}</p>
                                  <div className="flex gap-2">
                                    <Button size="sm" variant="outline" className="h-7 text-[10px] uppercase font-bold">Voir le Dossier</Button>
                                    {dispute.status === 'OPEN' && <Button size="sm" variant="outline" className="h-7 text-[10px] uppercase font-bold border-emerald-200 text-emerald-600">Résoudre le Litige</Button>}
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  </TabsContent>
                </Tabs>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center space-y-4 rounded-xl border-2 border-dashed border-muted bg-muted/5">
                <div className="rounded-full bg-emerald-500/10 p-6">
                  <Gavel className="h-12 w-12 text-emerald-500" />
                </div>
                <div>
                  <h3 className="text-xl font-bold">Sélectionnez une propriété pour examen juridique</h3>
                  <p className="text-muted-foreground max-w-md">
                    Choisissez une parcelle du registre pour accéder à son dossier juridique immuable, ses preuves blockchain et son historique de litiges.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </AppErrorBoundary>
  )
}

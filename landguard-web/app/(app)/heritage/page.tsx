"use client"

import { useState } from "react"
import { 
  Scale, 
  Users, 
  History, 
  ShieldCheck, 
  Plus, 
  Search,
  ArrowUpRight,
  Landmark,
  FileCheck
} from "lucide-react"
import { useHeritagePlans, useHeritageMutations } from "@/lib/hooks/use-heritage"
import { useParcels } from "@/lib/hooks/use-parcels"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { BlockchainAnchorCard } from "@/components/app/heritage/blockchain-anchor-card"
import { HeirVotingPanel } from "@/components/app/heritage/heir-voting-panel"
import { SuccessionTimeline } from "@/components/app/heritage/succession-timeline"
import { useHeritageStore } from "@/lib/store/heritage-store"
import { useQuery } from "@tanstack/react-query"
import { heritageApi } from "@/lib/api/heritage"
import { AppErrorBoundary } from "@/components/app/error-boundary"
import { resolveStatusMeta } from "@/lib/utils/safe-resolvers"

export default function HeritagePage() {
  const { data: plans, isLoading: plansLoading } = useHeritagePlans()
  const { data: parcels } = useParcels()
  const { anchor, vote, submitForVoting, transfer } = useHeritageMutations()
  const [searchTerm, setSearchTerm] = useState("")
  
  const selectedPlanId = useHeritageStore(state => state.selectedPlanId)
  const setSelectedPlanId = useHeritageStore(state => state.setSelectedPlan)
  
  const selectedPlan = (plans || []).find(p => p.id === selectedPlanId)
  const selectedParcel = (parcels || []).find(p => p.id === selectedPlan?.parcelId)

  const { data: auditTrail } = useQuery({
    queryKey: ["heritage-audit", selectedPlanId],
    queryFn: () => selectedPlanId ? heritageApi.getAuditTrail(selectedPlanId) : Promise.resolve([]),
    enabled: !!selectedPlanId
  })

  const filteredPlans = (plans || []).filter(p => {
    const parcel = (parcels || []).find(pa => pa.id === p.parcelId)
    return parcel?.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
           parcel?.reference.toLowerCase().includes(searchTerm.toLowerCase())
  })

  return (
    <AppErrorBoundary name="Gestion des Successions">
      <div className="flex flex-col gap-6 p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Succession & Héritage</h1>
            <p className="text-muted-foreground">Gérez l&apos;héritage foncier et les plans de succession sécurisés par blockchain.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="gap-2">
              <Landmark className="h-4 w-4" />
              Cadre Juridique
            </Button>
            <Button className="gap-2 bg-indigo-600 hover:bg-indigo-700">
              <Plus className="h-4 w-4" />
              Nouveau Plan de Succession
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-12 gap-6">
          {/* Left Sidebar: Plan List */}
          <div className="col-span-12 lg:col-span-4 space-y-4">
            <Card className="h-[calc(100vh-220px)] flex flex-col">
              <CardHeader className="pb-3">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Rechercher des parcelles..."
                    className="pl-9"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </CardHeader>
              <CardContent className="flex-1 overflow-y-auto p-0 border-t">
                {plansLoading ? (
                  <div className="p-8 text-center text-muted-foreground">Chargement des plans...</div>
                ) : filteredPlans.length === 0 ? (
                  <div className="p-8 text-center text-muted-foreground">Aucun plan trouvé.</div>
                ) : (
                  <div className="divide-y">
                    {filteredPlans.map((plan) => {
                      const parcel = (parcels || []).find(p => p.id === plan.parcelId)
                      const isSelected = plan.id === selectedPlanId
                      const statusMeta = resolveStatusMeta(plan.status)
                      
                      return (
                        <button
                          key={plan.id}
                          onClick={() => setSelectedPlanId(plan.id)}
                          className={`w-full text-left p-4 transition-colors hover:bg-muted/50 ${
                            isSelected ? "bg-indigo-500/5 border-l-4 border-l-indigo-500" : ""
                          }`}
                        >
                          <div className="flex items-start justify-between mb-1">
                            <span className="font-semibold text-sm truncate pr-2">
                              {parcel?.name || "Parcelle Inconnue"}
                            </span>
                            <Badge variant="outline" className={`text-[10px] h-4 uppercase ${statusMeta.tone}`}>
                              {statusMeta.label}
                            </Badge>
                          </div>
                          <div className="text-[11px] text-muted-foreground font-mono truncate">
                            REF: {parcel?.reference}
                          </div>
                          <div className="flex items-center gap-2 mt-2">
                            <div className="flex -space-x-1.5 overflow-hidden">
                              {plan.heirs.map((heir, i) => (
                                <div key={i} className="inline-block h-5 w-5 rounded-full ring-2 ring-background bg-muted flex items-center justify-center text-[10px] font-bold">
                                  {heir.fullName?.[0] || "?"}
                                </div>
                              ))}
                            </div>
                            <span className="text-[10px] text-muted-foreground">
                              {plan.heirs.length} héritiers enregistrés
                            </span>
                          </div>
                        </button>
                      )
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Right Content: Plan Detail */}
          <div className="col-span-12 lg:col-span-8 space-y-6">
            {selectedPlan ? (
              <div className="space-y-6">
                {/* Header Card */}
                <Card className="overflow-hidden border-indigo-500/10">
                  <div className="h-2 bg-indigo-500" />
                  <CardHeader className="flex flex-row items-start justify-between space-y-0">
                    <div>
                      <CardTitle className="text-2xl">{selectedParcel?.name}</CardTitle>
                      <CardDescription className="flex items-center gap-2 mt-1">
                        <Scale className="h-3.5 w-3.5" />
                        ID du Plan de Succession : {selectedPlan.id.substring(0, 8)}...
                      </CardDescription>
                    </div>
                    <div className="flex gap-2">
                      {selectedPlan.status === "DRAFT" && (
                        <Button size="sm" onClick={() => submitForVoting(selectedPlan.id)}>
                          Soumettre au Vote
                        </Button>
                      )}
                      {selectedPlan.status === "ANCHORED" && (
                        <Button size="sm" className="bg-emerald-600 hover:bg-emerald-700" onClick={() => transfer(selectedPlan.id)}>
                          Finaliser le Transfert
                        </Button>
                      )}
                      <Button variant="ghost" size="icon">
                        <ArrowUpRight className="h-4 w-4" />
                      </Button>
                    </div>
                  </CardHeader>
                </Card>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-6">
                    {/* Voting Panel */}
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base flex items-center gap-2">
                          <Users className="h-4 w-4 text-indigo-500" />
                          Approbations des Héritiers
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <HeirVotingPanel 
                          heirs={selectedPlan.heirs} 
                          onVote={(heirId, approved) => vote({ planId: selectedPlan.id, payload: { heirId, approved } })}
                          disabled={selectedPlan.status !== "IN_VOTING"}
                        />
                      </CardContent>
                    </Card>

                    {/* Blockchain Card */}
                    <BlockchainAnchorCard 
                      status={selectedPlan.status} 
                      hash={selectedPlan.blockchainHash}
                      onAnchor={() => anchor(selectedPlan.id)}
                    />
                  </div>

                  <div className="space-y-6">
                    {/* Tabs: Timeline / Legal / Map */}
                    <Tabs defaultValue="timeline" className="w-full">
                      <TabsList className="grid w-full grid-cols-3">
                        <TabsTrigger value="timeline">Historique</TabsTrigger>
                        <TabsTrigger value="legal">Preuve Juridique</TabsTrigger>
                        <TabsTrigger value="map">Carte de la Parcelle</TabsTrigger>
                      </TabsList>
                      <TabsContent value="timeline" className="mt-4">
                        <Card>
                          <CardHeader>
                            <CardTitle className="text-base flex items-center gap-2">
                              <History className="h-4 w-4 text-indigo-500" />
                              Piste d&apos;Audit de la Succession
                            </CardTitle>
                          </CardHeader>
                          <CardContent className="max-h-[500px] overflow-y-auto">
                            <SuccessionTimeline events={auditTrail || []} />
                          </CardContent>
                        </Card>
                      </TabsContent>
                      <TabsContent value="legal" className="mt-4">
                        <Card>
                          <CardHeader>
                            <CardTitle className="text-base flex items-center gap-2">
                              <FileCheck className="h-4 w-4 text-indigo-500" />
                              Documentation Juridique
                            </CardTitle>
                          </CardHeader>
                          <CardContent className="space-y-4">
                            <div className="rounded-lg border p-4 space-y-3">
                              <div className="flex items-center justify-between">
                                <span className="text-sm font-medium">Certificat de Propriété</span>
                                <Button variant="outline" size="sm">Télécharger PDF</Button>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-sm font-medium">Journal de Validation des Héritiers</span>
                                <Button variant="outline" size="sm">Exporter CSV</Button>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-sm font-medium">Reçu d&apos;Ancrage Blockchain</span>
                                <Button variant="outline" size="sm">Vérifier le Lien</Button>
                              </div>
                            </div>
                            
                            <div className="bg-muted/30 rounded-lg p-4 border border-dashed text-center">
                              <ShieldCheck className="h-8 w-8 text-emerald-500 mx-auto mb-2" />
                              <p className="text-xs text-muted-foreground">
                                Tous les documents sont signés cryptographiquement et ancrés sur le registre LandGuard.
                              </p>
                            </div>
                          </CardContent>
                        </Card>
                      </TabsContent>
                      <TabsContent value="map" className="mt-4">
                        <Card className="overflow-hidden">
                          <div className="h-[400px] bg-muted flex items-center justify-center">
                            <p className="text-sm text-muted-foreground">Superposition de Carte Interactive</p>
                            {/* Mapbox integration would go here */}
                          </div>
                        </Card>
                      </TabsContent>
                    </Tabs>
                  </div>
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center space-y-4 rounded-xl border-2 border-dashed border-muted bg-muted/5">
                <div className="rounded-full bg-indigo-500/10 p-6">
                  <Users className="h-12 w-12 text-indigo-500" />
                </div>
                <div>
                  <h3 className="text-xl font-bold">Sélectionnez un plan de succession</h3>
                  <p className="text-muted-foreground max-w-md">
                    Choisissez un plan dans la liste pour gérer les approbations des héritiers, consulter l&apos;historique d&apos;audit et ancrer les preuves sur la blockchain.
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

"use client"

import { useState } from "react"
import { 
  Landmark, 
  TrendingUp, 
  ShieldAlert, 
  BarChart3, 
  Search, 
  ArrowUpRight, 
  AlertTriangle,
  FileCheck2,
  PieChart,
  LineChart,
  Activity,
  Zap
} from "lucide-react"
import { useParcels } from "@/lib/hooks/use-parcels"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { AppErrorBoundary } from "@/components/app/error-boundary"

export default function BanquePage() {
  const { data: parcels, isLoading: parcelsLoading } = useParcels()
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedParcelId, setSelectedParcelId] = useState<string | null>(null)
  
  const selectedParcel = (parcels || []).find(p => p.id === selectedParcelId)

  const filteredParcels = (parcels || []).filter(p => 
    (p.name || "").toLowerCase().includes(searchTerm.toLowerCase()) || 
    (p.reference || "").toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <AppErrorBoundary name="Banque & Valorisation">
      <div className="flex flex-col gap-6 p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Banque & Valorisation</h1>
            <p className="text-muted-foreground">Intelligence du risque collatéral et score de confiance des parcelles.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="gap-2">
              <BarChart3 className="h-4 w-4" />
              Rapports de Marché
            </Button>
            <Button className="gap-2 bg-indigo-600 hover:bg-indigo-700">
              <Zap className="h-4 w-4" />
              Audit Collatéral Instantané
            </Button>
          </div>
        </div>

        {/* KPI Section */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Valeur Totale du Portefeuille</span>
                <TrendingUp className="h-4 w-4 text-emerald-500" />
              </div>
              <div className="text-2xl font-bold">14,2 Md FCFA</div>
              <div className="text-[10px] text-emerald-500 mt-1 font-bold">+2,4% vs mois dernier</div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Score de Confiance Moyen</span>
                <ShieldAlert className="h-4 w-4 text-indigo-500" />
              </div>
              <div className="text-2xl font-bold">84,2%</div>
              <Progress value={84.2} className="h-1.5 mt-2 bg-indigo-500/20" />
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Collatéral à Risque</span>
                <AlertTriangle className="h-4 w-4 text-red-500" />
              </div>
              <div className="text-2xl font-bold">1,2 Md FCFA</div>
              <div className="text-[10px] text-red-500 mt-1 font-bold">8 parcelles contestées</div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Couverture de Surveillance</span>
                <Activity className="h-4 w-4 text-blue-500" />
              </div>
              <div className="text-2xl font-bold">100%</div>
              <div className="text-[10px] text-blue-500 mt-1 font-bold">Surveillance en temps réel active</div>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-12 gap-6">
          {/* Left: Parcel Browser */}
          <div className="col-span-12 lg:col-span-4 space-y-4">
            <Card className="h-[calc(100vh-320px)] flex flex-col">
              <CardHeader className="pb-3">
                <div className="relative">
                  <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Rechercher des actifs collatéraux..."
                    className="pl-9"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
              </CardHeader>
              <CardContent className="flex-1 overflow-y-auto p-0 border-t">
                <div className="divide-y">
                  {filteredParcels.map((parcel) => (
                    <button
                      key={parcel.id}
                      onClick={() => setSelectedParcelId(parcel.id)}
                      className={`w-full text-left p-4 transition-colors hover:bg-muted/50 ${
                        parcel.id === selectedParcelId ? "bg-indigo-500/5 border-l-4 border-l-indigo-500" : ""
                      }`}
                    >
                      <div className="flex items-start justify-between mb-1">
                        <span className="font-semibold text-sm truncate pr-2">{parcel.name}</span>
                        <span className="text-xs font-bold">{new Intl.NumberFormat('fr-FR').format(parcel.estimatedValueXof || 0)} FCFA</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-[11px] text-muted-foreground font-mono">REF: {parcel.reference}</span>
                        <div className="flex items-center gap-1">
                          <div className={`h-1.5 w-1.5 rounded-full ${parcel.trustScore > 70 ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                          <span className="text-[10px] font-bold uppercase">{parcel.trustScore}% CONFIANCE</span>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Right: Risk Intelligence */}
          <div className="col-span-12 lg:col-span-8">
            {selectedParcel ? (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Scoring Card */}
                  <Card className="border-indigo-500/10 shadow-lg">
                    <CardHeader className="pb-2">
                      <CardTitle className="text-base flex items-center gap-2">
                        <ShieldAlert className="h-4 w-4 text-indigo-500" />
                        Score d&apos;Intelligence du Risque
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                      <div className="flex flex-col items-center py-4">
                        <div className="relative h-32 w-32 flex items-center justify-center">
                          <svg className="h-full w-full transform -rotate-90">
                            <circle
                              cx="64"
                              cy="64"
                              r="58"
                              stroke="currentColor"
                              strokeWidth="8"
                              fill="transparent"
                              className="text-muted/20"
                            />
                            <circle
                              cx="64"
                              cy="64"
                              r="58"
                              stroke="currentColor"
                              strokeWidth="8"
                              fill="transparent"
                              strokeDasharray={364.4}
                              strokeDashoffset={364.4 * (1 - (selectedParcel.trustScore || 0) / 100)}
                              className="text-indigo-500 transition-all duration-1000 ease-out"
                            />
                          </svg>
                          <div className="absolute inset-0 flex flex-col items-center justify-center">
                            <span className="text-3xl font-bold">{selectedParcel.trustScore || 0}</span>
                            <span className="text-[10px] font-bold text-muted-foreground uppercase">Score de Confiance</span>
                          </div>
                        </div>
                      </div>
                      
                      <div className="space-y-3">
                        <div className="space-y-1.5">
                          <div className="flex justify-between text-[10px] font-bold uppercase tracking-wider">
                            <span>Ancrage Blockchain</span>
                            <span className="text-emerald-500">Vérifié</span>
                          </div>
                          <Progress value={100} className="h-1 bg-emerald-500/20" />
                        </div>
                        <div className="space-y-1.5">
                          <div className="flex justify-between text-[10px] font-bold uppercase tracking-wider">
                            <span>Stabilité de la Succession</span>
                            <span className="text-indigo-500">Stable</span>
                          </div>
                          <Progress value={85} className="h-1 bg-indigo-500/20" />
                        </div>
                        <div className="space-y-1.5">
                          <div className="flex justify-between text-[10px] font-bold uppercase tracking-wider">
                            <span>Probabilité de Litige</span>
                            <span className="text-emerald-500">Faible (4%)</span>
                          </div>
                          <Progress value={4} className="h-1 bg-emerald-500/20" />
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Market Card */}
                  <Card>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-base flex items-center gap-2">
                        <TrendingUp className="h-4 w-4 text-emerald-500" />
                        Intelligence de Valorisation
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="p-4 rounded-lg bg-emerald-500/5 border border-emerald-500/10">
                        <div className="text-[10px] font-bold text-muted-foreground uppercase mb-1">Estimation Actuelle</div>
                        <div className="text-2xl font-bold text-emerald-700">
                          {new Intl.NumberFormat('fr-FR').format(selectedParcel.estimatedValueXof || 0)} FCFA
                        </div>
                        <div className="text-[10px] text-emerald-600 font-bold flex items-center gap-1 mt-1">
                          <ArrowUpRight className="h-3 w-3" />
                          +12% vs année dernière
                        </div>
                      </div>
                      
                      <div className="space-y-3">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-muted-foreground">Prix par m²</span>
                          <span className="font-bold">45 000 FCFA</span>
                        </div>
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-muted-foreground">Liquidité du Marché</span>
                          <Badge variant="outline" className="text-emerald-600 bg-emerald-50">ÉLEVÉE</Badge>
                        </div>
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-muted-foreground">Score de Bancabilité</span>
                          <span className="font-bold text-indigo-600">PREMIUM AA+</span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                <Tabs defaultValue="collateral" className="w-full">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="collateral">Audit Collatéral</TabsTrigger>
                    <TabsTrigger value="fraud">Détection de Fraude</TabsTrigger>
                    <TabsTrigger value="monitoring">Surveillance Live</TabsTrigger>
                  </TabsList>
                  <TabsContent value="collateral" className="mt-4">
                    <Card>
                      <CardContent className="pt-6 space-y-4">
                        <div className="flex items-start gap-4 p-4 rounded-lg border bg-muted/10">
                          <div className="p-2 rounded-md bg-indigo-500 text-white">
                            <FileCheck2 className="h-5 w-5" />
                          </div>
                          <div className="flex-1">
                            <h4 className="font-bold text-sm">Éligibilité Collatérale Confirmée</h4>
                            <p className="text-xs text-muted-foreground">Cette parcelle répond à tous les critères pour l&apos;Hypothèque de Rang 1.</p>
                            <Button size="sm" variant="link" className="h-auto p-0 text-indigo-600 text-[10px] font-bold uppercase mt-2">
                              Télécharger le Certificat de Conformité
                            </Button>
                          </div>
                        </div>
                        
                        <div className="grid grid-cols-2 gap-4">
                          <div className="p-3 rounded-lg border border-dashed text-center">
                            <PieChart className="h-5 w-5 mx-auto mb-2 text-muted-foreground" />
                            <div className="text-xs font-bold">Ratio LTV</div>
                            <div className="text-lg font-bold">65%</div>
                          </div>
                          <div className="p-3 rounded-lg border border-dashed text-center">
                            <LineChart className="h-5 w-5 mx-auto mb-2 text-muted-foreground" />
                            <div className="text-xs font-bold">Volatilité</div>
                            <div className="text-lg font-bold">Faible</div>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </TabsContent>
                  <TabsContent value="fraud" className="mt-4">
                    <Card className="border-red-500/10">
                      <CardContent className="pt-6 space-y-4">
                        <div className="flex items-center justify-between">
                          <h4 className="text-sm font-bold flex items-center gap-2">
                            <ShieldAlert className="h-4 w-4 text-red-500" />
                            Moteur d&apos;Heuristique de Fraude
                          </h4>
                          <Badge variant="outline" className="text-emerald-600 bg-emerald-50">AUCUNE ANOMALIE</Badge>
                        </div>
                        <div className="space-y-2">
                          {[
                            "Vérification de l'Identité du Propriétaire",
                            "Audit d'Alignement Cadastral",
                            "Détection de Double Nantissement",
                            "Cartographie des Litiges Historiques"
                          ].map((check, i) => (
                            <div key={i} className="flex items-center justify-between p-2 rounded bg-muted/30 text-[11px]">
                              <span>{check}</span>
                              <div className="flex items-center gap-1.5 text-emerald-600 font-bold">
                                <FileCheck2 className="h-3 w-3" />
                                RÉUSSI
                              </div>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  </TabsContent>
                </Tabs>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center h-full min-h-[500px] text-center space-y-4 rounded-xl border-2 border-dashed border-muted bg-muted/5">
                <div className="rounded-full bg-indigo-500/10 p-6">
                  <Landmark className="h-12 w-12 text-indigo-500" />
                </div>
                <div>
                  <h3 className="text-xl font-bold">Intelligence du Portefeuille Institutionnel</h3>
                  <p className="text-muted-foreground max-w-md">
                    Sélectionnez un actif collatéral pour accéder au scoring de risque bancaire, aux analyses de valorisation et à la détection de fraude en temps réel.
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

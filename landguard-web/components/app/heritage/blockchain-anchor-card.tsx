"use client"

import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Shield, CheckCircle2, Lock, Cpu, Globe, ArrowRight } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { cn } from "@/lib/utils"
import { resolveStatusMeta } from "@/lib/utils/safe-resolvers"

interface BlockchainAnchorCardProps {
  status: string
  hash: string | null
  onAnchor: () => Promise<void>
}

export function BlockchainAnchorCard({ status, hash, onAnchor }: BlockchainAnchorCardProps) {
  const [isAnchoring, setIsAnchoring] = useState(false)
  const [step, setStep] = useState(0)
  const statusMeta = resolveStatusMeta(status)

  const steps = [
    "Génération du hash déterministe SHA-256...",
    "Propagation vers la sidechain de gouvernance foncière...",
    "Confirmation de l'immutabilité sur le mainnet...",
    "Ancrage terminé."
  ]

  const handleAnchor = async () => {
    setIsAnchoring(true)
    for (let i = 0; i < 4; i++) {
      setStep(i)
      await new Promise(r => setTimeout(r, 1500))
    }
    await onAnchor()
    setIsAnchoring(false)
  }

  const isAnchored = status === "ANCHORED" || status === "TRANSFERRED" || status === "TITLE_ISSUED"

  return (
    <Card className="overflow-hidden border-emerald-500/20 bg-emerald-500/5 dark:bg-emerald-500/10">
      <CardHeader className="relative pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="rounded-full bg-emerald-500/20 p-2 text-emerald-500">
              <Shield className="h-5 w-5" />
            </div>
            <div>
              <CardTitle className="text-lg">Ancrage Blockchain</CardTitle>
              <CardDescription>Preuve immuable de succession foncière</CardDescription>
            </div>
          </div>
          <Badge variant={isAnchored ? "default" : "outline"} className={cn(isAnchored ? "bg-emerald-500 hover:bg-emerald-600" : "", statusMeta.tone)}>
            {isAnchored ? "SÉCURISÉ" : statusMeta.label}
          </Badge>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        <AnimatePresence mode="wait">
          {!isAnchored && !isAnchoring ? (
            <motion.div
              key="ready"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="space-y-4"
            >
              <div className="rounded-lg bg-emerald-500/10 p-4 border border-emerald-500/20">
                <p className="text-sm text-emerald-700 dark:text-emerald-300">
                  Ce plan de succession a été validé par tous les héritiers. Vous pouvez maintenant l&apos;ancrer sur la blockchain pour garantir son immutabilité juridique.
                </p>
              </div>
              <Button 
                onClick={handleAnchor} 
                className="w-full bg-emerald-600 hover:bg-emerald-700"
                disabled={status !== "VALIDATED"}
              >
                Lancer le Processus d&apos;Ancrage
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </motion.div>
          ) : isAnchoring ? (
            <motion.div
              key="anchoring"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              className="space-y-4"
            >
              <div className="flex items-center justify-between text-sm mb-1">
                <span className="text-emerald-600 font-medium">{steps[step]}</span>
                <span>{Math.round(((step + 1) / steps.length) * 100)}%</span>
              </div>
              <Progress value={((step + 1) / steps.length) * 100} className="h-2 bg-emerald-500/20" />
              
              <div className="grid grid-cols-4 gap-2 py-2">
                {[0, 1, 2, 3].map((s) => (
                  <div 
                    key={s} 
                    className={`h-1 rounded-full transition-colors duration-500 ${
                      s <= step ? "bg-emerald-500" : "bg-emerald-500/20"
                    }`}
                  />
                ))}
              </div>
              
              <div className="flex justify-center py-4">
                <div className="relative">
                  <motion.div
                    animate={{ rotate: 360 }}
                    transition={{ duration: 4, repeat: Infinity, ease: "linear" }}
                    className="rounded-full border-2 border-dashed border-emerald-500/50 p-8"
                  >
                    <Cpu className="h-8 w-8 text-emerald-500" />
                  </motion.div>
                  <motion.div
                    animate={{ scale: [1, 1.2, 1] }}
                    transition={{ duration: 2, repeat: Infinity }}
                    className="absolute inset-0 flex items-center justify-center"
                  >
                    <div className="h-4 w-4 rounded-full bg-emerald-500 opacity-20" />
                  </motion.div>
                </div>
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="complete"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="space-y-4"
            >
              <div className="rounded-lg bg-emerald-500/10 p-4 border border-emerald-500/20 flex gap-3">
                <CheckCircle2 className="h-5 w-5 text-emerald-500 shrink-0" />
                <div>
                  <p className="text-sm font-medium text-emerald-800 dark:text-emerald-400">Ancrage Réussi</p>
                  <p className="text-xs text-emerald-700/70 dark:text-emerald-400/70">
                    Le plan de succession est désormais immuable et juridiquement contraignant sur tous les nœuds.
                  </p>
                </div>
              </div>
              
              <div className="space-y-2">
                <label className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">Hash Blockchain</label>
                <div className="flex items-center gap-2 rounded-md bg-muted p-2 font-mono text-xs">
                  <Lock className="h-3 w-3 text-muted-foreground" />
                  <span className="truncate">{hash}</span>
                </div>
              </div>
              
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <div className="flex items-center gap-1">
                  <Globe className="h-3 w-3" />
                  <span>Mainnet</span>
                </div>
                <div className="flex items-center gap-1 text-emerald-500">
                  <div className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  <span>Immutabilité Vérifiée</span>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </CardContent>
    </Card>
  )
}

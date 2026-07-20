"use client"

import { motion } from "framer-motion"
import { Check, X, User, Scale } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Heir } from "@/lib/api/types"

interface HeirVotingPanelProps {
  heirs: Heir[]
  onVote: (heirId: string, approved: boolean) => Promise<void>
  disabled?: boolean
}

export function HeirVotingPanel({ heirs, onVote, disabled }: HeirVotingPanelProps) {
  const safeHeirs = heirs || []
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold flex items-center gap-2">
          <Scale className="h-4 w-4 text-emerald" />
          Statut d&apos;Approbation des Héritiers
        </h3>
        <span className="text-xs text-muted-foreground">
          {safeHeirs.filter(h => h.validated).length} / {safeHeirs.length} approuvés
        </span>
      </div>
      
      <div className="grid gap-3">
        {safeHeirs.map((heir, index) => (
          <motion.div
            key={heir.id}
            initial={{ opacity: 0, y: 5 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
            className={`flex items-center justify-between rounded-lg border p-3 ${
              heir.validated 
                ? "bg-emerald-500/5 border-emerald-500/20" 
                : "bg-background border-muted"
            }`}
          >
            <div className="flex items-center gap-3">
              <div className={`rounded-full p-2 ${heir.validated ? "bg-emerald-500/20 text-emerald-600" : "bg-muted text-muted-foreground"}`}>
                <User className="h-4 w-4" />
              </div>
              <div>
                <p className="text-sm font-medium">{heir.fullName || "Héritier inconnu"}</p>
                <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold">
                  {heir.relation || "Relation non spécifiée"} • Part {heir.sharePercentage || 0}%
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              {heir.validated ? (
                <div className="flex items-center gap-1.5 text-emerald-600 text-xs font-bold">
                  <Check className="h-4 w-4" />
                  APPROUVÉ
                </div>
              ) : (
                <div className="flex items-center gap-1">
                  <Button
                    size="sm"
                    variant="ghost"
                    className="h-8 w-8 p-0 text-red-500 hover:text-red-600 hover:bg-red-50"
                    onClick={() => onVote(heir.id, false)}
                    disabled={disabled}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="h-8 gap-1.5 text-emerald-600 border-emerald-200 hover:bg-emerald-50"
                    onClick={() => onVote(heir.id, true)}
                    disabled={disabled}
                  >
                    <Check className="h-4 w-4" />
                    Approuver
                  </Button>
                </div>
              )}
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  )
}

"use client"

import { motion } from "framer-motion"
import { format } from "date-fns"
import { fr } from "date-fns/locale"
import { CheckCircle2, UserPlus, FileText, Vote, Shield, Lock, AlertTriangle, type LucideIcon } from "lucide-react"
import { SuccessionAuditEvent, SuccessionAuditType } from "@/lib/api/types"

interface SuccessionTimelineProps {
  events: SuccessionAuditEvent[]
}

const EVENT_CONFIG: Record<SuccessionAuditType, { icon: LucideIcon, color: string, label: string }> = {
  PLAN_CREATED: { icon: FileText, color: "text-blue-500", label: "Plan de Succession Initié" },
  HEIR_ADDED: { icon: UserPlus, color: "text-indigo-500", label: "Héritier Enregistré" },
  HEIR_REMOVED: { icon: AlertTriangle, color: "text-amber-500", label: "Héritier Retiré" },
  SUBMITTED_FOR_VOTING: { icon: Vote, color: "text-purple-500", label: "Soumis au Vote" },
  VOTE_CAST: { icon: CheckCircle2, color: "text-emerald-500", label: "Vote Effectué" },
  PLAN_VALIDATED: { icon: Shield, color: "text-emerald-600", label: "Plan Validé par tous" },
  BLOCKCHAIN_ANCHORED: { icon: Lock, color: "text-indigo-600", label: "Ancrage Blockchain" },
  PLAN_REJECTED: { icon: AlertTriangle, color: "text-red-600", label: "Plan Rejeté" },
  OWNERSHIP_TRANSFERRED: { icon: CheckCircle2, color: "text-emerald-600", label: "Propriété Transférée" },
  ASSET_TRANSFERRED: { icon: CheckCircle2, color: "text-emerald-600", label: "Transfert Finalisé" },
  }


export function SuccessionTimeline({ events }: SuccessionTimelineProps) {
  const safeEvents = events || []
  if (safeEvents.length === 0) {
    return (
      <div className="flex h-40 flex-col items-center justify-center rounded-lg border border-dashed border-muted-foreground/20 bg-muted/5">
        <p className="text-sm text-muted-foreground">Aucun historique d&apos;audit disponible pour le moment.</p>
      </div>
    )
  }

  const sortedEvents = [...safeEvents].sort((a, b) => {
    const da = a.createdAt ? new Date(a.createdAt).getTime() : 0
    const db = b.createdAt ? new Date(b.createdAt).getTime() : 0
    return db - da
  })

  return (
    <div className="space-y-6 relative before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-0.5 before:bg-muted">
      {sortedEvents.map((event, index) => {
        const config = EVENT_CONFIG[event.type as SuccessionAuditType] || { icon: FileText, color: "text-muted-foreground", label: event.type }
        const Icon = config.icon

        return (
          <motion.div
            key={event.id}
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.1 }}
            className="relative pl-8"
          >
            <div className={`absolute left-0 top-1 rounded-full bg-background p-1 shadow-sm border ${config.color.replace('text-', 'border-')}`}>
              <Icon className={`h-4 w-4 ${config.color}`} />
            </div>
            
            <div className="flex flex-col gap-0.5">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-semibold">{config.label}</h4>
                <time className="text-[10px] text-muted-foreground font-mono">
                  {event.createdAt ? format(new Date(event.createdAt), "dd MMM yyyy HH:mm", { locale: fr }) : "Date inconnue"}
                </time>
              </div>
              
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <span className="font-medium text-foreground/70">Acteur :</span>
                <span className="rounded bg-muted px-1.5 py-0.5">{event.actor || "Inconnu"}</span>
              </div>
              
              {event.metadata && Object.keys(event.metadata).length > 0 && (
                <div className="mt-1.5 rounded-md bg-muted/50 p-2 border border-muted-foreground/10">
                  <div className="grid grid-cols-2 gap-x-4 gap-y-1">
                    {Object.entries(event.metadata).map(([key, value]) => (
                      <div key={key} className="flex flex-col">
                        <span className="text-[10px] uppercase font-bold text-muted-foreground/60 leading-none mb-1">{key}</span>
                        <span className="text-[11px] font-medium truncate" title={value}>{value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        )
      })}
    </div>
  )
}

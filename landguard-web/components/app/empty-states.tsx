"use client"

import { FileQuestion, Search, ShieldAlert, type LucideIcon } from "lucide-react"
import { cn } from "@/lib/utils"

interface EmptyStateProps {
  icon?: LucideIcon
  title: string
  description: string
  className?: string
  action?: React.ReactNode
}

export function PremiumEmptyState({
  icon: Icon = FileQuestion,
  title,
  description,
  className,
  action
}: EmptyStateProps) {
  return (
    <div className={cn(
      "flex min-h-[400px] flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card p-12 text-center shadow-card animate-in fade-in zoom-in duration-500",
      className
    )}>
      <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-emerald-soft text-emerald ring-8 ring-emerald-soft/40">
        <Icon className="h-9 w-9" />
      </div>
      <h3 className="mt-6 font-display text-xl font-medium text-foreground tracking-tight">
        {title}
      </h3>
      <p className="mt-2 max-w-sm text-sm text-muted-foreground leading-relaxed">
        {description}
      </p>
      {action && (
        <div className="mt-8">
          {action}
        </div>
      )}
    </div>
  )
}

export function NoResultsState({ query }: { query?: string }) {
  return (
    <PremiumEmptyState
      icon={Search}
      title="Aucun résultat trouvé"
      description={query 
        ? `Nous n'avons trouvé aucune parcelle correspondant à "${query}". Essaie de modifier tes filtres.` 
        : "Aucune donnée ne correspond aux critères de sélection actuels."
      }
    />
  )
}

export function ErrorState({ message, retry }: { message?: string; retry?: () => void }) {
  return (
    <PremiumEmptyState
      icon={ShieldAlert}
      title="Erreur de chargement"
      description={message || "Impossible de récupérer les données du registre foncier. Vérifie ta connexion ou l'état des services gouvernementaux."}
      action={retry && (
        <button 
          onClick={retry}
          className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors"
        >
          Réessayer la connexion
        </button>
      )}
    />
  )
}

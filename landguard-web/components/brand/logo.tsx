import { cn } from "@/lib/utils"

interface LogoProps {
  className?: string
  showWordmark?: boolean
  /** Use on dark surfaces (navy sidebar) so the wordmark stays legible. */
  inverted?: boolean
}

/** LA BOUSSOLE wordmark — a stylized compass with cardinal points. */
export function Logo({ className, showWordmark = true, inverted = false }: LogoProps) {
  return (
    <div className={cn("flex items-center gap-2.5", className)}>
      <div className="relative flex h-8 w-8 items-center justify-center">
        <svg
          viewBox="0 0 32 32"
          className="h-8 w-8"
          fill="none"
          aria-hidden="true"
        >
          <circle
            cx="16"
            cy="16"
            r="14"
            stroke="var(--emerald)"
            strokeWidth="1.5"
            opacity="0.8"
          />
          <circle
            cx="16"
            cy="16"
            r="9"
            stroke="var(--emerald)"
            strokeWidth="0.75"
            opacity="0.4"
          />
          <path
            d="M16 4 L18.5 16 L16 28 L13.5 16 Z"
            fill="var(--emerald)"
            opacity="0.95"
          />
          <path
            d="M16 4 L18.5 16 L16 16 Z"
            fill="var(--gold)"
          />
          <circle cx="16" cy="16" r="1.4" fill="var(--background)" />
        </svg>
      </div>
      {showWordmark && (
        <div className="flex flex-col leading-none">
          <span
            className={cn(
              "font-display text-[15px] font-semibold tracking-tight",
              inverted ? "text-white" : "text-foreground",
            )}
          >
            LA BOUSSOLE
          </span>
          <span
            className={cn(
              "mt-0.5 font-mono text-[9px] uppercase tracking-[0.18em]",
              inverted ? "text-white/55" : "text-muted-foreground",
            )}
          >
            Land Intelligence
          </span>
        </div>
      )}
    </div>
  )
}

import Link from "next/link"
import { Logo } from "@/components/brand/logo"

const cols = [
  {
    title: "Plateforme",
    links: ["Vision Live", "Surveillance", "Registre", "Blockchain", "Application terrain"],
  },
  {
    title: "Profils",
    links: ["Citoyen", "Notaire", "Banque", "Tribunal", "Investisseur", "Héritier"],
  },
  {
    title: "Ressources",
    links: ["Documentation", "API", "Sécurité", "Conformité OHADA", "Statut"],
  },
  {
    title: "Société",
    links: ["À propos", "Carrières", "Presse", "Contact", "Mentions légales"],
  },
]

export function LandingFooter() {
  return (
    <footer className="border-t border-border/40 bg-background">
      <div className="mx-auto max-w-7xl px-6 py-16">
        <div className="grid gap-10 lg:grid-cols-[1.4fr_2fr]">
          <div>
            <Logo />
            <p className="mt-4 max-w-xs text-sm text-muted-foreground">
              LA BOUSSOLE est une plateforme africaine d&apos;intelligence foncière —
              conçue pour les citoyens, certifiée pour les institutions.
            </p>
            <p className="mt-6 font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
              Bamako · Dakar · Abidjan · Lomé · Ouagadougou
            </p>
          </div>

          <div className="grid grid-cols-2 gap-6 sm:grid-cols-4">
            {cols.map((c) => (
              <div key={c.title}>
                <h4 className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
                  {c.title}
                </h4>
                <ul className="mt-3 space-y-2">
                  {c.links.map((l) => (
                    <li key={l}>
                      <Link
                        href="#"
                        className="text-sm text-foreground/80 transition-colors hover:text-emerald"
                      >
                        {l}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-12 flex flex-col items-start justify-between gap-3 border-t border-border/60 pt-6 sm:flex-row sm:items-center">
          <p className="text-xs text-muted-foreground">
            © 2026 LA BOUSSOLE Land Intelligence. Tous droits réservés.
          </p>
          <p className="font-mono text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
            Sécuriser la terre · Préserver l&apos;avenir
          </p>
        </div>
      </div>
    </footer>
  )
}

import { Lock, KeyRound, EyeOff, Timer, FileLock2, History } from "lucide-react"

// Only mechanisms actually implemented by the platform are listed here.
const features = [
  {
    icon: KeyRound,
    title: "Accès par rôle",
    description:
      "Citoyen, agent, notaire, banquier, administrateur : chaque profil n'accède qu'aux fonctions et aux données de sa mission.",
  },
  {
    icon: EyeOff,
    title: "Cloisonnement des données",
    description:
      "Un propriétaire ne voit que ses propres parcelles. Le registre complet est réservé aux agents habilités.",
  },
  {
    icon: Timer,
    title: "Sessions à durée limitée",
    description:
      "Jetons d'accès signés et à expiration courte : toute session expirée exige une reconnexion.",
  },
  {
    icon: Lock,
    title: "Mots de passe protégés",
    description:
      "Les mots de passe sont hachés avec un algorithme robuste et ne sont jamais conservés en clair.",
  },
  {
    icon: FileLock2,
    title: "Ancrage blockchain",
    description:
      "Les étapes clés d'un dossier sont ancrées avec une empreinte SHA-256 horodatée et vérifiable.",
  },
  {
    icon: History,
    title: "Traçabilité des successions",
    description:
      "Chaque action d'un plan de succession est journalisée et consultable dans le dossier.",
  },
]

export function LandingSecurity() {
  return (
    <section id="securite" className="relative overflow-hidden border-b border-border/40 py-24 sm:py-32">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-grid opacity-[0.12]"
      />
      <div className="relative mx-auto max-w-7xl px-6">
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-mono text-xs uppercase tracking-[0.22em] text-gold">
            Sécurité de niveau souverain
          </span>
          <h2 className="mt-4 font-display text-4xl font-medium tracking-tight text-balance text-foreground sm:text-5xl">
            Conçu comme une infrastructure d&apos;État.
          </h2>
          <p className="mt-4 text-pretty text-muted-foreground">
            Chaque couche — de l&apos;authentification à la blockchain — est pensée pour
            protéger votre patrimoine foncier.
          </p>
        </div>

        <div className="mt-16 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f) => (
            <div
              key={f.title}
              className="rounded-xl border border-border bg-card/60 p-5 transition-colors hover:border-emerald/40 hover:bg-card"
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-md border border-border bg-secondary text-gold">
                <f.icon className="h-5 w-5" />
              </div>
              <h3 className="mt-4 font-display text-lg font-medium tracking-tight text-foreground">
                {f.title}
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                {f.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

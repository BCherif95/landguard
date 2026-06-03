import { Fingerprint, Lock, KeyRound, ScanFace, FileLock2, History } from "lucide-react"

const features = [
  {
    icon: Fingerprint,
    title: "Biométrie & MFA",
    description: "Reconnaissance faciale, empreinte et codes OTP pour chaque opération sensible.",
  },
  {
    icon: KeyRound,
    title: "RBAC granulaire",
    description: "Permissions par rôle, par parcelle et par opération — auditées en continu.",
  },
  {
    icon: Lock,
    title: "Chiffrement AES-256",
    description: "Données chiffrées au repos et en transit avec rotation de clés HSM.",
  },
  {
    icon: ScanFace,
    title: "Détection comportementale",
    description: "Modèle IA qui repère les anomalies d'usage et bloque les sessions suspectes.",
  },
  {
    icon: FileLock2,
    title: "Hash blockchain",
    description: "Chaque document est ancré on-chain avec preuve d'horodatage opposable.",
  },
  {
    icon: History,
    title: "Audit logs immuables",
    description: "Traçabilité complète, exportable pour les autorités et les régulateurs.",
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
            Chaque couche — du chiffrement à la blockchain — protège votre patrimoine
            avec des standards que les ministères du foncier exigent.
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

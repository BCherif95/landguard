"use client"

import { Suspense, useState } from "react"
import Link from "next/link"
import { motion } from "framer-motion"
import { Fingerprint, ScanFace, ShieldCheck, Sparkles, ArrowRight } from "lucide-react"
import { Logo } from "@/components/brand/logo"
import { LoginForm } from "@/components/auth/login-form"
import { RegisterForm } from "@/components/auth/register-form"
import { cn } from "@/lib/utils"

const methods = [
  { icon: Fingerprint, label: "Empreinte biométrique", desc: "FIDO2 · WebAuthn (à venir)" },
  { icon: ScanFace, label: "Reconnaissance faciale", desc: "Liveness check 3D (à venir)" },
  { icon: ShieldCheck, label: "Carte d'identité numérique", desc: "eID nationale UEMOA (à venir)" },
]

type Tab = "login" | "register"

export default function AuthPage() {
  const [tab, setTab] = useState<Tab>("login")

  return (
    <main className="grid min-h-screen lg:grid-cols-2">
      {/* Left — visual */}
      <div className="relative hidden overflow-hidden border-r border-border/60 bg-navy lg:block">
        <div
          className="absolute inset-0 opacity-30"
          style={{
            backgroundImage:
              "radial-gradient(circle at 30% 30%, rgba(16,138,90,0.4), transparent 50%), radial-gradient(circle at 70% 70%, rgba(212,175,55,0.25), transparent 55%)",
          }}
        />
        <div
          className="absolute inset-0 opacity-[0.07]"
          style={{
            backgroundImage:
              "linear-gradient(rgba(255,255,255,0.4) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.4) 1px, transparent 1px)",
            backgroundSize: "48px 48px",
          }}
        />
        <div className="relative z-10 flex h-full flex-col p-12 text-white">
          <Logo />
          <div className="flex flex-1 flex-col justify-center">
            <motion.h2
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              className="max-w-md text-4xl font-medium leading-tight tracking-tight"
            >
              Sécuriser la terre.
              <br />
              <span className="text-gold">Préserver l&apos;avenir.</span>
            </motion.h2>
            <p className="mt-6 max-w-sm text-sm leading-relaxed text-white/70">
              LA BOUSSOLE est la première plateforme africaine d&apos;intelligence foncière qui combine cadastre
              numérique, surveillance satellite et registre blockchain certifié.
            </p>

            <div className="mt-10 flex flex-wrap gap-2">
              {["OHADA", "ISO 27001", "BCEAO", "Chiffrement E2E"].map((b) => (
                <span
                  key={b}
                  className="rounded-full border border-white/15 bg-white/5 px-3 py-1 text-[11px] text-white/70 backdrop-blur"
                >
                  {b}
                </span>
              ))}
            </div>
          </div>
          <div className="text-xs text-white/40">© 2026 LA BOUSSOLE — Plateforme certifiée souveraine</div>
        </div>
      </div>

      {/* Right — form */}
      <div className="flex items-center justify-center p-6 lg:p-12">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md"
        >
          <div className="lg:hidden">
            <Logo />
            <div className="my-8 h-px bg-border" />
          </div>

          <p className="text-xs uppercase tracking-[0.3em] text-accent">Connexion sécurisée</p>
          <h1 className="mt-3 text-3xl font-medium tracking-tight">
            {tab === "login" ? "Accédez à votre espace" : "Créer un compte vérifié"}
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {tab === "login"
              ? "L'authentification multi-facteurs garantit l'intégrité de chaque action sur vos parcelles."
              : "Votre compte donne accès au cadastre numérique et au registre blockchain de LA BOUSSOLE."}
          </p>

          <div className="mt-6 inline-flex rounded-xl border border-border bg-card/40 p-1 text-xs">
            <TabButton active={tab === "login"} onClick={() => setTab("login")}>
              Connexion
            </TabButton>
            <TabButton active={tab === "register"} onClick={() => setTab("register")}>
              Inscription
            </TabButton>
          </div>

          <div className="mt-6">
            <Suspense fallback={<div className="h-40 flex items-center justify-center">Chargement...</div>}>
              {tab === "login" ? <LoginForm /> : <RegisterForm />}
            </Suspense>
          </div>

          <div className="my-8 flex items-center gap-3 text-xs text-muted-foreground">
            <div className="h-px flex-1 bg-border" />
            <span>ou par méthode forte</span>
            <div className="h-px flex-1 bg-border" />
          </div>

          <div className="space-y-2">
            {methods.map((m) => (
              <button
                key={m.label}
                disabled
                className="group flex w-full items-center gap-4 rounded-xl border border-border/60 bg-card/40 p-4 text-left opacity-60"
              >
                <div className="flex size-10 items-center justify-center rounded-xl bg-accent/10 text-accent">
                  <m.icon className="size-5" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium">{m.label}</p>
                  <p className="text-xs text-muted-foreground">{m.desc}</p>
                </div>
                <ArrowRight className="size-4 text-muted-foreground" />
              </button>
            ))}
          </div>

          <div className="mt-8 flex items-center gap-2 rounded-xl border border-border/60 bg-card/40 p-4 text-xs text-muted-foreground">
            <Sparkles className="size-4 shrink-0 text-gold" />
            <span>
              {tab === "login" ? (
                <>
                  Première visite ?{" "}
                  <button
                    type="button"
                    onClick={() => setTab("register")}
                    className="text-foreground underline-offset-2 hover:underline"
                  >
                    Créer un compte vérifié
                  </button>{" "}
                  en quelques minutes.
                </>
              ) : (
                <>
                  Déjà inscrit ?{" "}
                  <button
                    type="button"
                    onClick={() => setTab("login")}
                    className="text-foreground underline-offset-2 hover:underline"
                  >
                    Se connecter
                  </button>
                </>
              )}
            </span>
          </div>

          <div className="mt-6 text-center">
            <Link href="/" className="text-xs text-muted-foreground hover:text-foreground">
              ← Retour à l&apos;accueil
            </Link>
          </div>
        </motion.div>
      </div>
    </main>
  )
}

function TabButton({
  active,
  children,
  onClick,
}: {
  active: boolean
  children: React.ReactNode
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "rounded-lg px-4 py-1.5 transition-all",
        active
          ? "bg-accent text-accent-foreground shadow-sm"
          : "text-muted-foreground hover:text-foreground",
      )}
    >
      {children}
    </button>
  )
}

"use client"

import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Sparkles, Send, FileText, Scale, Gavel, BookOpen, ShieldAlert, ArrowRight, Bot, User } from "lucide-react"
import { Button } from "@/components/ui/button"

const suggestions = [
  {
    icon: Scale,
    title: "Vérifier la conformité",
    body: "Analyser un titre foncier vis-à-vis du Code domanial OHADA.",
  },
  {
    icon: Gavel,
    title: "Préparer un litige",
    body: "Constituer un dossier de revendication avec preuves blockchain.",
  },
  {
    icon: BookOpen,
    title: "Synthèse jurisprudentielle",
    body: "Identifier les précédents pertinents pour mon dossier.",
  },
  {
    icon: ShieldAlert,
    title: "Clause à risque",
    body: "Détecter les clauses abusives dans un acte de vente.",
  },
]

type Message = {
  role: "user" | "assistant"
  content: string
}

const initialMessages: Message[] = [
  {
    role: "assistant",
    content:
      "Bonjour, je suis BOUSSOLE Juris, votre conseil juridique IA spécialisé en droit foncier OHADA. Je peux analyser vos titres, anticiper les litiges et préparer vos actes. Comment puis-je vous assister ?",
  },
]

// No AI backend is wired yet: rather than fabricate a legal analysis, the
// assistant answers honestly that the service is not available.
const UNAVAILABLE_RESPONSE =
  "Le conseiller juridique automatisé n'est pas encore disponible sur cette plateforme. " +
  "Votre question n'a pas été analysée. Pour toute étude de titre ou préparation de dossier, " +
  "rapprochez-vous d'un notaire ou d'un agent habilité depuis le module Vérification."

export default function JuridiquePage() {
  const [messages, setMessages] = useState<Message[]>(initialMessages)
  const [input, setInput] = useState("")
  const [thinking, setThinking] = useState(false)

  function handleSend(text: string) {
    if (!text.trim()) return
    setMessages((m) => [...m, { role: "user", content: text }])
    setInput("")
    setThinking(true)
    setTimeout(() => {
      setMessages((m) => [...m, { role: "assistant", content: UNAVAILABLE_RESPONSE }])
      setThinking(false)
    }, 400)
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-accent">Conseil IA</p>
          <h1 className="mt-2 font-serif text-3xl font-medium tracking-tight">BOUSSOLE Juris</h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Assistant dédié au droit foncier. Service en cours de développement :
            aucune analyse automatisée n&apos;est encore réalisée.
          </p>
        </div>
        <div className="flex items-center gap-2 rounded-full border border-border bg-secondary px-4 py-2 text-xs text-muted-foreground">
          Service à venir
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Chat */}
        <div className="glass flex flex-col rounded-3xl border border-border/60 lg:col-span-2 lg:h-[640px]">
          <div className="flex items-center justify-between border-b border-border/60 px-6 py-4">
            <div className="flex items-center gap-3">
              <div className="flex size-9 items-center justify-center rounded-xl bg-accent/10">
                <Sparkles className="size-4 text-accent" />
              </div>
              <div>
                <p className="text-sm font-medium">Conversation juridique</p>
                <p className="text-xs text-muted-foreground">Service en cours de développement</p>
              </div>
            </div>
          </div>

          <div className="flex-1 space-y-4 overflow-y-auto p-6">
            {messages.map((m, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                className={`flex gap-3 ${m.role === "user" ? "flex-row-reverse" : ""}`}
              >
                <div
                  className={`flex size-8 shrink-0 items-center justify-center rounded-xl ${
                    m.role === "user" ? "bg-foreground text-background" : "bg-accent/10 text-accent"
                  }`}
                >
                  {m.role === "user" ? <User className="size-4" /> : <Bot className="size-4" />}
                </div>
                <div
                  className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                    m.role === "user"
                      ? "bg-foreground text-background"
                      : "border border-border/60 bg-card/60 text-foreground"
                  }`}
                >
                  <p className="whitespace-pre-line">{m.content}</p>
                </div>
              </motion.div>
            ))}

            <AnimatePresence>
              {thinking && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className="flex items-center gap-3"
                >
                  <div className="flex size-8 items-center justify-center rounded-xl bg-accent/10">
                    <Bot className="size-4 text-accent" />
                  </div>
                  <div className="flex gap-1 rounded-2xl border border-border/60 bg-card/60 px-4 py-3">
                    {[0, 1, 2].map((i) => (
                      <motion.span
                        key={i}
                        animate={{ opacity: [0.3, 1, 0.3] }}
                        transition={{ duration: 1.2, delay: i * 0.2, repeat: Number.POSITIVE_INFINITY }}
                        className="size-1.5 rounded-full bg-accent"
                      />
                    ))}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div className="border-t border-border/60 p-4">
            <div className="flex items-end gap-2 rounded-2xl border border-border bg-input p-2">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault()
                    handleSend(input)
                  }
                }}
                placeholder="Posez votre question juridique..."
                rows={1}
                className="flex-1 resize-none bg-transparent px-3 py-2 text-sm outline-none placeholder:text-muted-foreground"
              />
              <Button
                onClick={() => handleSend(input)}
                size="icon"
                className="size-9 shrink-0 bg-accent text-accent-foreground hover:bg-accent/90"
              >
                <Send className="size-4" />
              </Button>
            </div>
            <p className="mt-2 px-2 text-[10px] text-muted-foreground">
              Les réponses sont indicatives. Pour un avis exécutoire, consultez un notaire ou un avocat partenaire.
            </p>
          </div>
        </div>

        {/* Suggestions sidebar */}
        <div className="space-y-6">
          <div className="glass rounded-3xl border border-border/60 p-6">
            <h3 className="font-serif text-lg tracking-tight">Suggestions</h3>
            <p className="mt-1 text-xs text-muted-foreground">Cas d&apos;usage les plus fréquents</p>
            <div className="mt-5 space-y-2">
              {suggestions.map((s) => (
                <button
                  key={s.title}
                  onClick={() => handleSend(s.body)}
                  className="group w-full rounded-2xl border border-border/60 bg-card/40 p-4 text-left transition-all hover:border-accent/40 hover:bg-card"
                >
                  <div className="flex items-start gap-3">
                    <div className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-accent/10 text-accent">
                      <s.icon className="size-4" />
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{s.title}</p>
                      <p className="mt-0.5 text-xs text-muted-foreground">{s.body}</p>
                    </div>
                    <ArrowRight className="size-4 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100" />
                  </div>
                </button>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}

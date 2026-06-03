"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { ArrowRight, Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { authApi } from "@/lib/api/auth"
import { ApiError } from "@/lib/api/types"
import { useAuthStore } from "@/lib/store/auth-store"

type SelectableRole = "CITIZEN" | "OFFICER" | "LEGAL" | "BANKER"

const ROLES: { value: SelectableRole; label: string; description: string }[] = [
  { value: "CITIZEN", label: "Citoyen", description: "Propriétaire ou héritier" },
  { value: "OFFICER", label: "Agent cadastral", description: "Agent foncier ou de surveillance" },
  { value: "LEGAL", label: "Notaire / Juriste", description: "Génération de preuves légales" },
  { value: "BANKER", label: "Banque / Finance", description: "Évaluation et garanties" },
]

const schema = z
  .object({
    fullName: z
      .string()
      .min(2, "Nom complet requis.")
      .max(200, "Nom trop long."),
    email: z.string().email("Adresse e-mail invalide."),
    password: z
      .string()
      .min(10, "Au moins 10 caractères.")
      .max(128, "Mot de passe trop long."),
    confirmPassword: z.string(),
    role: z.enum(["CITIZEN", "OFFICER", "LEGAL", "BANKER"]),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Les mots de passe ne correspondent pas.",
  })

type FormValues = z.infer<typeof schema>

export function RegisterForm() {
  const router = useRouter()
  const setSession = useAuthStore((s) => s.setSession)
  const [submitting, setSubmitting] = useState(false)

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { role: "CITIZEN" },
  })

  const selectedRole = watch("role")

  const onSubmit = handleSubmit(async (values) => {
    setSubmitting(true)
    try {
      const response = await authApi.register({
        email: values.email,
        fullName: values.fullName,
        password: values.password,
        role: values.role,
      })
      setSession(response)
      toast.success("Compte créé. Bienvenue sur LA BOUSSOLE.")
      router.replace("/dashboard")
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.code === "EMAIL_ALREADY_REGISTERED"
            ? "Un compte existe déjà avec cet e-mail."
            : e.message
          : "Erreur lors de la création du compte."
      toast.error(msg)
    } finally {
      setSubmitting(false)
    }
  })

  return (
    <form className="space-y-4" onSubmit={onSubmit} noValidate>
      <Field
        label="Nom complet"
        autoComplete="name"
        placeholder="Aïcha Diarra"
        error={errors.fullName?.message}
        {...register("fullName")}
      />
      <Field
        label="Adresse e-mail"
        type="email"
        autoComplete="email"
        placeholder="aicha.diarra@example.com"
        error={errors.email?.message}
        {...register("email")}
      />
      <Field
        label="Mot de passe"
        type="password"
        autoComplete="new-password"
        placeholder="Au moins 10 caractères"
        error={errors.password?.message}
        {...register("password")}
      />
      <Field
        label="Confirmer le mot de passe"
        type="password"
        autoComplete="new-password"
        error={errors.confirmPassword?.message}
        {...register("confirmPassword")}
      />

      <div>
        <label className="mb-1.5 block text-xs font-medium text-muted-foreground">
          Rôle sur la plateforme
        </label>
        <div className="grid grid-cols-2 gap-2">
          {ROLES.map((r) => {
            const active = selectedRole === r.value
            return (
              <button
                key={r.value}
                type="button"
                onClick={() => setValue("role", r.value, { shouldValidate: true })}
                className={[
                  "flex flex-col items-start rounded-xl border p-3 text-left text-xs transition-all",
                  active
                    ? "border-accent/60 bg-accent/10"
                    : "border-border/60 bg-card/40 hover:border-accent/40",
                ].join(" ")}
              >
                <span className="font-medium text-foreground">{r.label}</span>
                <span className="mt-0.5 text-muted-foreground">{r.description}</span>
              </button>
            )
          })}
        </div>
      </div>

      <Button
        type="submit"
        disabled={submitting}
        className="group h-12 w-full gap-2 bg-accent text-accent-foreground hover:bg-accent/90"
      >
        {submitting ? (
          <Loader2 className="size-4 animate-spin" />
        ) : (
          <>
            Créer mon compte
            <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
          </>
        )}
      </Button>
    </form>
  )
}

type FieldProps = React.InputHTMLAttributes<HTMLInputElement> & {
  label: string
  error?: string
}

const Field = ({ label, error, ...rest }: FieldProps) => (
  <div>
    <label className="mb-1.5 block text-xs font-medium text-muted-foreground">{label}</label>
    <input
      {...rest}
      className="w-full rounded-xl border border-border bg-input px-4 py-3 text-sm outline-none transition-all focus:border-accent focus:ring-2 focus:ring-accent/20 aria-[invalid=true]:border-destructive"
      aria-invalid={Boolean(error) || undefined}
    />
    {error && <p className="mt-1 text-xs text-destructive">{error}</p>}
  </div>
)

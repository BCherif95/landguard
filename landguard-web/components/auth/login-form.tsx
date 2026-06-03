"use client"

import { useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { ArrowRight, Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { authApi } from "@/lib/api/auth"
import { ApiError } from "@/lib/api/types"
import { useAuthStore } from "@/lib/store/auth-store"

const schema = z.object({
  email: z.string().email("Adresse e-mail invalide."),
  password: z.string().min(1, "Mot de passe requis."),
})

type FormValues = z.infer<typeof schema>

export function LoginForm() {
  const router = useRouter()
  const params = useSearchParams()
  const setSession = useAuthStore((s) => s.setSession)
  const [submitting, setSubmitting] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = handleSubmit(async (values) => {
    setSubmitting(true)
    try {
      const response = await authApi.login(values)
      setSession(response)
      const next = params.get("next") || "/dashboard"
      router.replace(next)
    } catch (e) {
      const msg =
        e instanceof ApiError ? e.message : "Erreur lors de la connexion. Réessayez."
      toast.error(msg)
    } finally {
      setSubmitting(false)
    }
  })

  return (
    <form className="space-y-4" onSubmit={onSubmit} noValidate>
      <Field
        label="Adresse e-mail"
        type="email"
        autoComplete="email"
        placeholder="kouassi.amani@laboussole.africa"
        error={errors.email?.message}
        {...register("email")}
      />
      <Field
        label="Mot de passe"
        type="password"
        autoComplete="current-password"
        placeholder="••••••••••••"
        error={errors.password?.message}
        {...register("password")}
      />

      <Button
        type="submit"
        disabled={submitting}
        className="group h-12 w-full gap-2 bg-accent text-accent-foreground hover:bg-accent/90"
      >
        {submitting ? (
          <Loader2 className="size-4 animate-spin" />
        ) : (
          <>
            Continuer
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

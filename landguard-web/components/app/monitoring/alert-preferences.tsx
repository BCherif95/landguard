"use client"

import { useEffect, useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { useNotificationPreferences } from "@/lib/hooks/use-notification-preferences"
import { BellRing, Loader2, Mail, MessageSquareWarning, Smartphone } from "lucide-react"

interface ChannelRowProps {
  id: string
  icon: React.ReactNode
  label: string
  hint: string
  checked: boolean
  onCheckedChange: (checked: boolean) => void
}

function ChannelRow({ id, icon, label, hint, checked, onCheckedChange }: ChannelRowProps) {
  return (
    <div className="flex items-center justify-between gap-3 py-2">
      <div className="flex items-start gap-3">
        <span className="mt-0.5 text-primary">{icon}</span>
        <div>
          <Label htmlFor={id} className="text-sm font-medium text-white cursor-pointer">
            {label}
          </Label>
          <p className="text-[11px] text-muted-foreground leading-relaxed">{hint}</p>
        </div>
      </div>
      <Switch id={id} checked={checked} onCheckedChange={onCheckedChange} />
    </div>
  )
}

export function AlertPreferences() {
  const { preferences, isLoading, save, isSaving } = useNotificationPreferences()

  const [pushEnabled, setPushEnabled] = useState(true)
  const [emailEnabled, setEmailEnabled] = useState(true)
  const [smsEnabled, setSmsEnabled] = useState(false)
  const [phoneNumber, setPhoneNumber] = useState("")

  useEffect(() => {
    if (preferences) {
      setPushEnabled(preferences.pushEnabled)
      setEmailEnabled(preferences.emailEnabled)
      setSmsEnabled(preferences.smsEnabled)
      setPhoneNumber(preferences.phoneNumber ?? "")
    }
  }, [preferences])

  const smsMissingPhone = smsEnabled && phoneNumber.trim().length === 0

  const handleSave = () => {
    save({
      pushEnabled,
      emailEnabled,
      smsEnabled,
      phoneNumber: phoneNumber.trim() === "" ? null : phoneNumber.trim(),
    })
  }

  return (
    <Card className="p-5 bg-black/60 backdrop-blur-2xl border-primary/20 space-y-4">
      <div className="flex items-center gap-2">
        <BellRing className="w-4 h-4 text-primary" />
        <h3 className="text-xs font-black uppercase tracking-[0.2em] text-white">
          Préférences d'alerte
        </h3>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center gap-2 py-6 text-muted-foreground">
          <Loader2 className="w-4 h-4 animate-spin" />
          <span className="text-xs">Chargement des préférences…</span>
        </div>
      ) : (
        <>
          <div className="divide-y divide-white/5">
            <ChannelRow
              id="alert-channel-push"
              icon={<Smartphone className="w-4 h-4" />}
              label="Notification push"
              hint="Alerte instantanée dans l'application."
              checked={pushEnabled}
              onCheckedChange={setPushEnabled}
            />
            <ChannelRow
              id="alert-channel-email"
              icon={<Mail className="w-4 h-4" />}
              label="E-mail (recommandé pour la diaspora)"
              hint="Rapport détaillé avec lien vers le dossier de preuve."
              checked={emailEnabled}
              onCheckedChange={setEmailEnabled}
            />
            <ChannelRow
              id="alert-channel-sms"
              icon={<MessageSquareWarning className="w-4 h-4" />}
              label="SMS d'urgence"
              hint="Message court en cas de faible couverture Internet."
              checked={smsEnabled}
              onCheckedChange={setSmsEnabled}
            />
          </div>

          {smsEnabled && (
            <div className="space-y-1.5">
              <Label htmlFor="alert-phone-number" className="text-[11px] text-muted-foreground">
                Numéro de mobile (format international)
              </Label>
              <Input
                id="alert-phone-number"
                inputMode="tel"
                placeholder="+223XXXXXXXX"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                className="bg-black/40 border-white/10 font-mono text-xs"
              />
              {smsMissingPhone && (
                <p className="text-[10px] text-orange-400">
                  Un numéro de mobile est requis pour activer les SMS d'urgence.
                </p>
              )}
            </div>
          )}

          <Button
            onClick={handleSave}
            disabled={isSaving || smsMissingPhone}
            className="w-full h-10 text-[10px] font-bold uppercase tracking-widest"
          >
            {isSaving ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Enregistrement…
              </>
            ) : (
              "Enregistrer les préférences"
            )}
          </Button>
        </>
      )}
    </Card>
  )
}

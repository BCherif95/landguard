"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { 
  FileCheck, 
  FileText, 
  ClipboardCheck, 
  Stamp, 
  UserCheck,
  Search,
  AlertCircle,
  ShieldCheck,
  Undo2
} from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

const steps = [
  { id: "depot", title: "Dépôt Dossier", icon: FileText },
  { id: "requisition", title: "Réquisition Domaines", icon: Search },
  { id: "validation", title: "Expertise Notariale", icon: UserCheck },
  { id: "certification", title: "Certification", icon: Stamp },
];

export function TitleVerificationWizard() {
  const [currentStep, setCurrentStep] = useState(0);

  const nextStep = () => setCurrentStep((s) => Math.min(s + 1, steps.length - 1));
  const prevStep = () => setCurrentStep((s) => Math.max(s - 1, 0));

  return (
    <div className="space-y-8">
      {/* Stepper */}
      <div className="flex justify-between items-center bg-card p-8 rounded-2xl border border-border shadow-card relative overflow-hidden">
        <div className="absolute inset-0 bg-grid-fine opacity-60 pointer-events-none" />
        {steps.map((step, idx) => {
          const Icon = step.icon;
          const isActive = idx === currentStep;
          const isCompleted = idx < currentStep;

          return (
            <div key={step.id} className="flex flex-col items-center space-y-4 flex-1 relative z-10">
              <div
                className={`w-14 h-14 rounded-2xl flex items-center justify-center border-2 transition-all duration-500 ${
                  isActive ? "border-primary bg-primary text-primary-foreground scale-110 shadow-md ring-4 ring-emerald-soft" :
                  isCompleted ? "border-emerald/30 bg-emerald-soft text-emerald" : "border-border bg-secondary text-muted-foreground"
                }`}
              >
                {isCompleted ? <FileCheck className="w-7 h-7" /> : <Icon className="w-6 h-6" />}
              </div>
              <span className={`text-[10px] font-black uppercase tracking-[0.2em] transition-colors duration-300 ${isActive ? "text-emerald" : "text-muted-foreground/70"}`}>
                {step.title}
              </span>
              {idx < steps.length - 1 && (
                <div className={`absolute top-7 left-[60%] w-[80%] h-[1px] -z-10 transition-colors duration-1000 ${isCompleted ? "bg-emerald/50" : "bg-border"}`} />
              )}
            </div>
          );
        })}
      </div>

      <AnimatePresence mode="wait">
        <motion.div
          key={currentStep}
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 1.02 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
        >
          {currentStep === 0 && (
            <Card className="bg-card border-border shadow-card overflow-hidden">
              <CardHeader className="bg-secondary border-b border-border pb-6">
                <CardTitle className="text-xl font-black uppercase tracking-tight text-foreground flex items-center gap-3">
                   <div className="w-2 h-6 bg-emerald rounded-full" />
                   Ouverture du Dossier
                </CardTitle>
                <CardDescription className="text-muted-foreground font-medium">Saisie des informations du Titre Foncier (TF) fournies par le client.</CardDescription>
              </CardHeader>
              <CardContent className="pt-8 space-y-8">
                <div className="grid md:grid-cols-2 gap-8">
                  <div className="space-y-3">
                    <Label htmlFor="tfNumber" className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Numéro du Titre Foncier</Label>
                    <Input id="tfNumber" placeholder="Ex: TF-2458-BKO" className="h-12 font-mono text-emerald focus:border-emerald/50 transition-colors" />
                  </div>
                  <div className="space-y-3">
                    <Label htmlFor="conservation" className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Bureau de la Conservation</Label>
                    <Input id="conservation" placeholder="Ex: Bamako, Koulikoro..." className="h-12 focus:border-emerald/50 transition-colors" />
                  </div>
                  <div className="space-y-3">
                    <Label htmlFor="volume" className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Volume</Label>
                    <Input id="volume" placeholder="Ex: 85" className="h-12 focus:border-emerald/50 transition-colors" />
                  </div>
                  <div className="space-y-3">
                    <Label htmlFor="folio" className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Folio</Label>
                    <Input id="folio" placeholder="Ex: 142" className="h-12 focus:border-emerald/50 transition-colors" />
                  </div>
                </div>
                <div className="space-y-3">
                  <Label htmlFor="owner" className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Nom du Propriétaire (tel qu'indiqué sur le TF)</Label>
                  <Input id="owner" placeholder="Ex: Moussa TRAORE" className="h-12 focus:border-emerald/50 transition-colors text-foreground font-bold" />
                </div>
              </CardContent>
            </Card>
          )}

          {currentStep === 1 && (
            <Card className="bg-card border-emerald/20 shadow-card">
              <CardHeader className="bg-emerald-soft border-b border-emerald/10 pb-6">
                <CardTitle className="text-xl font-black uppercase tracking-tight text-foreground flex items-center gap-3">
                  <ClipboardCheck className="text-emerald w-7 h-7" />
                  Réquisition aux Domaines
                </CardTitle>
                <CardDescription className="text-muted-foreground font-medium">Renseignement des conclusions manuelles obtenues auprès du service des Domaines.</CardDescription>
              </CardHeader>
              <CardContent className="pt-8 space-y-8">
                <div className="grid md:grid-cols-2 gap-8">
                  <div className="space-y-3">
                    <Label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Numéro de Réquisition</Label>
                    <Input placeholder="Numéro officiel délivré" className="h-12" />
                  </div>
                  <div className="space-y-3">
                    <Label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Date de Réquisition</Label>
                    <Input type="date" className="h-12" />
                  </div>
                  <div className="space-y-3">
                    <Label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Agent Vérificateur</Label>
                    <Input placeholder="Nom de l'agent aux Domaines" className="h-12" />
                  </div>
                  <div className="space-y-3">
                    <Label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Bureau Domanial</Label>
                    <Input placeholder="Ex: DNDC Bamako" className="h-12" />
                  </div>
                </div>

                <div className="p-6 rounded-2xl border border-border bg-secondary space-y-6">
                  <h4 className="text-[10px] font-black uppercase text-emerald tracking-[0.2em]">Conclusions de la vérification</h4>
                  <div className="grid grid-cols-2 gap-6">
                    <div className="flex items-center space-x-3 border border-border p-4 rounded-xl bg-card hover:bg-emerald-soft hover:border-emerald/30 transition-all cursor-pointer group">
                      <input type="checkbox" id="auth" className="w-5 h-5 accent-emerald rounded border-border" />
                      <Label htmlFor="auth" className="cursor-pointer font-bold text-foreground group-hover:text-emerald transition-colors">Authenticité Confirmée</Label>
                    </div>
                    <div className="flex items-center space-x-3 border border-border p-4 rounded-xl bg-card hover:bg-emerald-soft hover:border-emerald/30 transition-all cursor-pointer group">
                      <input type="checkbox" id="litige" className="w-5 h-5 accent-emerald rounded border-border" />
                      <Label htmlFor="litige" className="cursor-pointer font-bold text-foreground group-hover:text-emerald transition-colors">Absence de Litige</Label>
                    </div>
                  </div>
                </div>

                <div className="space-y-3">
                  <Label className="text-[10px] font-black uppercase tracking-widest text-muted-foreground">Observations de l'Agent</Label>
                  <Textarea placeholder="Notes complémentaires du livre foncier..." className="min-h-[120px]" />
                </div>
              </CardContent>
            </Card>
          )}

          {currentStep === 2 && (
            <div className="space-y-8">
              <Card className="border-info/30 bg-card shadow-card overflow-hidden relative">
                <div className="absolute top-0 right-0 p-8 opacity-[0.06]">
                   <UserCheck className="w-32 h-32 text-info" />
                </div>
                <CardHeader className="bg-info-soft border-b border-info/10 pb-6">
                  <CardTitle className="flex items-center gap-3 text-foreground uppercase font-black tracking-tight">
                    <div className="p-2 rounded-lg bg-info/20">
                      <ShieldCheck className="w-6 h-6 text-info" />
                    </div>
                    Validation Notariale
                  </CardTitle>
                </CardHeader>
                <CardContent className="pt-8 space-y-8 relative z-10">
                   <div className="flex items-start gap-6 p-6 rounded-2xl border border-info/20 bg-info/5 shadow-inner">
                      <div className="w-12 h-12 rounded-xl bg-info/20 flex items-center justify-center text-info shrink-0 border border-info/30">
                        <FileCheck className="w-7 h-7" />
                      </div>
                      <div>
                        <p className="font-black text-foreground text-lg tracking-tight">Conformité du Dossier</p>
                        <p className="text-muted-foreground text-sm leading-relaxed mt-1">La réquisition n°REQ-2024-001 confirme que le titre TF-2458-BKO est valide et appartient bien à Moussa TRAORE.</p>
                      </div>
                   </div>

                   <div className="space-y-4">
                      <Label className="text-[10px] font-black uppercase tracking-widest text-info">Avis motivé de l'expert</Label>
                      <Textarea placeholder="Rédigez ici les conclusions finales pour la certification..." className="min-h-[150px] focus:border-info/50" />
                   </div>
                </CardContent>
              </Card>
            </div>
          )}
        </motion.div>
      </AnimatePresence>

      <div className="flex justify-between items-center pt-8 border-t border-border">
        <Button variant="ghost" onClick={prevStep} disabled={currentStep === 0} className="text-muted-foreground hover:text-foreground hover:bg-secondary font-bold uppercase tracking-widest text-[10px]">
          <Undo2 className="w-4 h-4 mr-2" />
          Retour
        </Button>
        <div className="flex gap-4">
          {currentStep === 1 && (
            <Button variant="outline" className="border-danger/30 text-danger bg-danger/5 hover:bg-danger/10 font-bold uppercase tracking-widest text-[10px] px-6 h-12">
              <AlertCircle className="w-4 h-4 mr-2" />
              Signaler une Anomalie
            </Button>
          )}
          <Button onClick={nextStep} className="px-10 h-12 bg-primary text-primary-foreground hover:bg-primary/90 font-black uppercase tracking-[0.2em] text-[10px] shadow-md transition-all active:scale-[0.98]">
            {currentStep === steps.length - 1 ? "Générer Certificat" : "Étape Suivante"}
          </Button>
        </div>
      </div>
    </div>
  );
}

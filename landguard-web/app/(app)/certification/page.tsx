import { TitleVerificationWizard } from "@/components/app/certification/certification-wizard";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Scale, ShieldCheck, History } from "lucide-react";

export default function CertificationPage() {
  return (
    <div className="container mx-auto py-8 space-y-8 bg-grid-fine min-h-screen">
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-black tracking-tighter uppercase text-white font-display">
            Vérification & Certification TF
          </h1>
          <p className="text-muted-foreground mt-2 font-medium">Plateforme professionnelle d'expertise foncière et notariale du Mali.</p>
        </div>
        <div className="flex gap-3">
          <Badge variant="outline" className="px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.2em] border-emerald/30 bg-emerald/5 text-emerald shadow-[0_0_15px_rgba(16,185,129,0.1)]">
            DNDC MALI
          </Badge>
          <Badge variant="outline" className="px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.2em] border-info/30 bg-info/5 text-info">
            NOTARIAT
          </Badge>
        </div>
      </div>

      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-8">
          <TitleVerificationWizard />
        </div>

        <div className="space-y-6">
          <Card className="bg-black/40 backdrop-blur-xl border-white/5 shadow-2xl relative overflow-hidden group">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-emerald to-transparent opacity-50" />
            <CardHeader>
              <CardTitle className="text-sm font-black uppercase tracking-widest flex items-center gap-3 text-white/90">
                <ShieldCheck className="w-5 h-5 text-emerald animate-pulse-soft" />
                Cadre Réglementaire
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6 text-sm">
              <div className="flex gap-4">
                <div className="h-10 w-10 rounded-xl bg-emerald/10 flex items-center justify-center border border-emerald/20 shadow-inner shrink-0">
                   <History className="w-5 h-5 text-emerald" />
                </div>
                <div>
                  <p className="font-bold text-white tracking-tight">Code Domanial & Foncier</p>
                  <p className="text-muted-foreground text-xs leading-relaxed mt-1">Conforme aux dispositions de la loi foncière malienne sur la preuve de propriété.</p>
                </div>
              </div>
              <div className="flex gap-4">
                <div className="h-10 w-10 rounded-xl bg-info/10 flex items-center justify-center border border-info/20 shadow-inner shrink-0">
                   <Scale className="w-5 h-5 text-info" />
                </div>
                <div>
                  <p className="font-bold text-white tracking-tight">Preuve de Réquisition</p>
                  <p className="text-muted-foreground text-xs leading-relaxed mt-1">Chaque certification repose sur une réquisition officielle vérifiée aux Domaines.</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-black/20 backdrop-blur-md border-white/5">
            <CardHeader className="pb-2">
              <CardTitle className="text-[10px] font-black uppercase tracking-[0.25em] text-muted-foreground">Statistiques Réseau</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-5">
                 <div className="flex justify-between items-end border-b border-white/5 pb-3">
                    <span className="text-xs font-medium text-white/60 uppercase tracking-tighter">TF Certifiés (Mois)</span>
                    <span className="font-black text-2xl font-mono text-white">1,240</span>
                 </div>
                 <div className="flex justify-between items-end">
                    <span className="text-xs font-medium text-white/60 uppercase tracking-tighter">Fraude Bloquée</span>
                    <span className="font-black text-2xl font-mono text-danger">42</span>
                 </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

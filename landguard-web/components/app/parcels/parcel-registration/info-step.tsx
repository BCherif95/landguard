"use client"

import {useForm} from "react-hook-form"
import {zodResolver} from "@hookform/resolvers/zod"
import {z} from "zod"
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form"
import {Input} from "@/components/ui/input"
import {Button} from "@/components/ui/button"
import {useRegistrationFlowStore} from "@/lib/store/registration-flow.store"
import {isValidCadastralReference} from "@/lib/domain/cadastral-reference"

/**
 * Zod schema with strict validation matching backend CadastralReference pattern.
 * Pattern: ^[A-Z]{3}-[A-Z]{2}-\d{4}-\d{4,6}$
 */
const infoStepSchema = z.object({
    reference: z
        .string()
        .min(1, "Référence requise")
        .refine(
            (ref) => isValidCadastralReference(ref),
            "Format invalide. Attendu: XXX-YY-YYYY-NNNNNN (ex: BSL-ML-2024-654321)"
        ),
    name: z
        .string()
        .min(1, "Nom du domaine requis")
        .min(2, "Nom trop court")
        .max(200, "Nom trop long"),
    regionLabel: z
        .string()
        .min(1, "Région requise")
        .min(2, "Région trop courte")
        .max(200, "Région trop longue"),
    ownerLabel: z
        .string()
        .min(1, "Propriétaire requis")
        .min(2, "Propriétaire trop court")
        .max(200, "Propriétaire trop long"),
    areaHectares: z
        .coerce
        .number()
        .min(0.0001, "Superficie minimale: 0.0001 ha")
        .max(1000000, "Superficie maximale dépassée"),
    estimatedValueXof: z
        .coerce
        .number()
        .min(0, "Valeur doit être ≥ 0")
        .max(999999999999, "Valeur maximale dépassée"),
})

export function InfoStep() {
    const {formData, updateFormData, nextStep} = useRegistrationFlowStore()

    const form = useForm<z.infer<typeof infoStepSchema>>({
        resolver: zodResolver(infoStepSchema),
        defaultValues: formData,
        mode: "onChange", // Real-time validation feedback
    })

    const onSubmit = (values: z.infer<typeof infoStepSchema>) => {
        updateFormData(values)
        nextStep()
    }

    return (
        <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-4">
                <FormField
                    control={form.control}
                    name="reference"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                Référence Cadastrale
                            </FormLabel>
                            <FormControl>
                                <Input
                                    {...field}
                                    readOnly
                                    className="bg-muted/50 font-mono text-emerald cursor-not-allowed"
                                    title="Référence générée automatiquement au format standardisé"
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="name"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                Nom du domaine / Titre
                            </FormLabel>
                            <FormControl>
                                <Input
                                    placeholder="Ex: Lotissement N'Tabakoro"
                                    {...field}
                                    className="h-11"
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="regionLabel"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                Cercle et Commune
                            </FormLabel>
                            <FormControl>
                                <Input
                                    placeholder="Ex: Kati, Commune de Kalabancoro"
                                    {...field}
                                    className="h-11"
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="ownerLabel"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                Propriétaire (Nom complet)
                            </FormLabel>
                            <FormControl>
                                <Input
                                    placeholder="Ex: Famille Traoré"
                                    {...field}
                                    className="h-11"
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <div className="grid grid-cols-2 gap-4">
                    <FormField
                        control={form.control}
                        name="areaHectares"
                        render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                    Superficie (ha)
                                </FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        step="0.0001"
                                        {...field}
                                        className="bg-white/5 border-white/10 h-11"
                                    />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                    <FormField
                        control={form.control}
                        name="estimatedValueXof"
                        render={({field}) => (
                            <FormItem>
                                <FormLabel className="text-xs uppercase tracking-widest text-muted-foreground">
                                    Valeur (FCFA)
                                </FormLabel>
                                <FormControl>
                                    <Input
                                        type="number"
                                        {...field}
                                        className="h-11 font-mono text-emerald"
                                    />
                                </FormControl>
                                <FormMessage/>
                            </FormItem>
                        )}
                    />
                </div>
                <Button
                    type="submit"
                    size="lg"
                    className="mt-6 w-full font-semibold uppercase tracking-widest"
                    disabled={!form.formState.isValid}
                >
                    Étape suivante
                </Button>
            </form>
        </Form>
    )
}
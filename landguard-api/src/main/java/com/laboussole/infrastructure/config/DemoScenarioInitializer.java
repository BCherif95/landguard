package com.laboussole.infrastructure.config;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.geo.UtmCoordinate;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.legal.DisputeSeverity;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelStatus;
import com.laboussole.domain.port.in.DetectMonitoringEventUseCase;
import com.laboussole.domain.port.in.IssueLandTitleUseCase;
import com.laboussole.domain.port.in.RegisterLandParcelUseCase;
import com.laboussole.domain.port.in.TransitionParcelStatusUseCase;
import com.laboussole.domain.port.in.VerifyLandParcelUseCase;
import com.laboussole.domain.port.in.geo.ConvertUtmPolygonUseCase;
import com.laboussole.domain.port.in.heritage.AddHeirUseCase;
import com.laboussole.domain.port.in.heritage.AnchorSuccessionUseCase;
import com.laboussole.domain.port.in.heritage.CreateSuccessionPlanUseCase;
import com.laboussole.domain.port.in.heritage.SubmitSuccessionForVotingUseCase;
import com.laboussole.domain.port.in.heritage.VoteSuccessionPlanUseCase;
import com.laboussole.domain.port.in.legal.OpenLegalDisputeUseCase;
import com.laboussole.domain.port.in.notification.UpdateNotificationPreferencesUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.PasswordHasher;
import com.laboussole.domain.port.out.StorageService;
import com.laboussole.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a complete, coherent end-to-end demonstration scenario by driving the
 * real use cases in order — never by writing rows directly.
 *
 * <p>That distinction is the point of this class. Every artefact it produces is
 * genuine: the polygons come out of the UTM converter fed with situation-plan
 * coordinates, the document fingerprints are real SHA-256 digests computed from
 * files actually written to storage, the ledger anchors are real chained seals,
 * and the alerts are real queued deliveries the dispatcher will pick up. A demo
 * built on hand-written rows would prove nothing about the workflow.
 *
 * <p>Guarded by {@code @Profile("demo")} so it never runs in staging or
 * production, and idempotent: it does nothing if its parcels already exist.
 *
 * <p>Run with: {@code --spring.profiles.active=dev,demo}
 *
 * <p><strong>All persons, parcels and title numbers below are fictional.</strong>
 * The place names are real Malian localities, and the document formats follow
 * Malian land-administration practice, so the scenario reads as it would in
 * Bamako — but nothing here corresponds to a real person or a real land record.
 */
@Component
@Profile("demo")
@Order(100) // After BootstrapDevUsersInitializer.
public class DemoScenarioInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoScenarioInitializer.class);

    /** Presence of this reference means the scenario is already seeded. */
    private static final String SENTINEL_REFERENCE = "BSL-ML-2026-000101";

    private static final String DEMO_PASSWORD_PROPERTY = "${laboussole.bootstrap.dev-users.password:Boussole2026!}";

    private final UserRepository users;
    private final LandParcelRepository parcels;
    private final PasswordHasher passwordHasher;
    private final StorageService storage;
    private final ConvertUtmPolygonUseCase convertUtmPolygon;
    private final RegisterLandParcelUseCase registerParcel;
    private final TransitionParcelStatusUseCase transitionStatus;
    private final VerifyLandParcelUseCase verifyParcel;
    private final IssueLandTitleUseCase issueTitle;
    private final DetectMonitoringEventUseCase detectEvent;
    private final CreateSuccessionPlanUseCase createSuccessionPlan;
    private final AddHeirUseCase addHeir;
    private final SubmitSuccessionForVotingUseCase submitForVoting;
    private final VoteSuccessionPlanUseCase voteOnPlan;
    private final AnchorSuccessionUseCase anchorSuccession;
    private final OpenLegalDisputeUseCase openDispute;
    private final UpdateNotificationPreferencesUseCase updatePreferences;
    private final String demoPassword;

    public DemoScenarioInitializer(
            UserRepository users,
            LandParcelRepository parcels,
            PasswordHasher passwordHasher,
            StorageService storage,
            ConvertUtmPolygonUseCase convertUtmPolygon,
            RegisterLandParcelUseCase registerParcel,
            TransitionParcelStatusUseCase transitionStatus,
            VerifyLandParcelUseCase verifyParcel,
            IssueLandTitleUseCase issueTitle,
            DetectMonitoringEventUseCase detectEvent,
            CreateSuccessionPlanUseCase createSuccessionPlan,
            AddHeirUseCase addHeir,
            SubmitSuccessionForVotingUseCase submitForVoting,
            VoteSuccessionPlanUseCase voteOnPlan,
            AnchorSuccessionUseCase anchorSuccession,
            OpenLegalDisputeUseCase openDispute,
            UpdateNotificationPreferencesUseCase updatePreferences,
            @Value(DEMO_PASSWORD_PROPERTY) String demoPassword) {
        this.users = users;
        this.parcels = parcels;
        this.passwordHasher = passwordHasher;
        this.storage = storage;
        this.convertUtmPolygon = convertUtmPolygon;
        this.registerParcel = registerParcel;
        this.transitionStatus = transitionStatus;
        this.verifyParcel = verifyParcel;
        this.issueTitle = issueTitle;
        this.detectEvent = detectEvent;
        this.createSuccessionPlan = createSuccessionPlan;
        this.addHeir = addHeir;
        this.submitForVoting = submitForVoting;
        this.voteOnPlan = voteOnPlan;
        this.anchorSuccession = anchorSuccession;
        this.openDispute = openDispute;
        this.updatePreferences = updatePreferences;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (parcels.existsByReference(CadastralReference.of(SENTINEL_REFERENCE))) {
            log.info("Demo scenario already seeded — nothing to do.");
            return;
        }

        log.info("=== Seeding LA BOUSSOLE end-to-end demo scenario ===");
        var personas = seedPersonas();
        var summary = new ArrayList<String>();

        summary.add(scenarioOne(personas));
        summary.add(scenarioTwo(personas));
        summary.add(scenarioThree(personas));

        log.warn("""

                ╔══════════════════════════════════════════════════════════════════════════╗
                ║  SCÉNARIO DE DÉMONSTRATION CHARGÉ — DONNÉES FICTIVES                      ║
                ╚══════════════════════════════════════════════════════════════════════════╝
                {}

                Comptes de démonstration (mot de passe partagé : "{}") :
                  fatoumata.coulibaly@demo.laboussole.ml   Propriétaire diaspora (Paris)
                  ibrahim.sissoko@demo.laboussole.ml       Héritier — fils aîné
                  aminata.sissoko@demo.laboussole.ml       Héritière — fille
                  moussa.sissoko@demo.laboussole.ml        Héritier — fils
                  kadiatou.sissoko@demo.laboussole.ml      Héritière — fille

                À observer ensuite :
                  • GET /api/v1/blockchain/verify   → registre INTACT, plan successoral ancré
                  • GET /api/v1/monitoring/events   → l'alerte CRITICAL de Safo
                  • Table alert_deliveries          → le dispatcher draine la file toutes les 10 s.
                    PUSH aboutit ; EMAIL et SMS échouent tant que SMTP/Twilio ne sont pas
                    configurés, ce qui est voulu : le journal montre alors le backoff
                    exponentiel puis la mise en DEAD_LETTER.

                PROFIL demo UNIQUEMENT — ne jamais activer hors développement.""",
                String.join("\n", summary), demoPassword);
    }

    // ---------------------------------------------------------------------
    // Scenario 1 — "Le fait accompli" : Safo (cercle de Kati)
    //
    // A diaspora owner in Paris holds a titled plot near Safo. Sentinel-2
    // detects fresh construction on it. This is the core promise of the
    // product: the owner learns of the encroachment from 6 000 km away, in
    // time to act, with dated evidence for the bailiff.
    // ---------------------------------------------------------------------
    private String scenarioOne(Personas personas) {
        var geometry = fromUtm(List.of(
                utm(608_620, 1_402_450),
                utm(608_720, 1_402_450),
                utm(608_720, 1_402_530),
                utm(608_620, 1_402_530)));

        var parcel = register(
                "BSL-ML-2026-000101",
                "Parcelle de Safo — lot 47",
                "Safo, Cercle de Kati, Région de Koulikoro",
                "Fatoumata COULIBALY",
                personas.diasporaOwner(),
                new BigDecimal("0.8000"),
                42_000_000L,
                geometry,
                List.of(
                        doc("TF", "Titre Foncier n° 24 517 CK", "tf-24517-ck.pdf",
                                "TITRE FONCIER N° 24 517 CK — Cercle de Kati\n"
                                        + "Titulaire : Fatoumata COULIBALY\n"
                                        + "Superficie : 80 a 00 ca — Livre Foncier de Kati, volume 62, folio 118\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION"),
                        doc("PLAN", "Plan de bornage du géomètre", "plan-bornage-047.pdf",
                                "PLAN DE BORNAGE — lot 47, Safo\n"
                                        + "Système : UTM zone 29N (WGS-84)\n"
                                        + "Sommets : 608620/1402450 · 608720/1402450 ·"
                                        + " 608720/1402530 · 608620/1402530\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION"),
                        doc("ID", "Carte NINA du titulaire", "nina-coulibaly.pdf",
                                "CARTE NINA — COULIBALY Fatoumata\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION")));

        // Full administrative path through to an issued Titre Foncier.
        advanceTo(parcel, ParcelStatus.SUBMITTED, ParcelStatus.UNDER_SURVEY,
                ParcelStatus.UNDER_VERIFICATION, ParcelStatus.UNDER_NOTARY_REVIEW,
                ParcelStatus.UNDER_ADMIN_REVIEW, ParcelStatus.CERTIFIED);
        verifyParcel.execute(new VerifyLandParcelUseCase.Command(parcel.id(), 92));
        issueTitle.execute(new IssueLandTitleUseCase.Command(parcel.id()));

        // The owner is in Paris: push + e-mail for her, SMS for the caretaker
        // number she left on file. This is what fans the alert out below.
        updatePreferences.execute(new UpdateNotificationPreferencesUseCase.Command(
                personas.diasporaOwner(), true, true, true, "+22376451208"));

        // Sentinel-2 differential detection → this call queues the alerts.
        var event = detectEvent.execute(new DetectMonitoringEventUseCase.Command(
                parcel.id(),
                MonitoringEventType.CONSTRUCTION,
                MonitoringSeverity.CRITICAL,
                94,
                new ParcelGeometry.Coordinate(-7.9021, 12.6841),
                "Fondations en parpaings coulées sur la moitié nord du lot entre le"
                        + " 04/07/2026 et le 19/07/2026. Aucune autorisation de construire"
                        + " enregistrée. Emprise estimée : 210 m².",
                MonitoringSource.SATELLITE,
                null));

        openDispute.open(parcel.id(),
                "Occupation illicite et construction sans droit ni titre sur le TF n° 24 517 CK."
                        + " Constat d'huissier requis en vue d'une ordonnance de déguerpissement.",
                DisputeSeverity.CRITICAL);
        transitionStatus.execute(
                new TransitionParcelStatusUseCase.Command(parcel.id(), ParcelStatus.DISPUTED));

        log.info("Scenario 1 ready: parcel {} titled, event {} raised, dispute opened",
                parcel.reference().value(), event.id().value());
        return "  1. BSL-ML-2026-000101 — Safo (Kati) : Titre Foncier délivré, puis construction\n"
                + "     illicite détectée par satellite → alerte CRITICAL + litige ouvert.";
    }

    // ---------------------------------------------------------------------
    // Scenario 2 — "L'indivision successorale" : Sirakoro, Bamako Commune VI
    //
    // The father died; four children inherit undivided. Each must validate
    // before anything can move — no heir can sell behind the others' backs.
    // The validated plan is sealed on the ledger.
    // ---------------------------------------------------------------------
    private String scenarioTwo(Personas personas) {
        var geometry = fromUtm(List.of(
                utm(612_180, 1_395_240),
                utm(612_265, 1_395_240),
                utm(612_265, 1_395_310),
                utm(612_180, 1_395_310)));

        var parcel = register(
                "BSL-ML-2026-000102",
                "Concession familiale SISSOKO — Sirakoro",
                "Sirakoro, Commune VI, District de Bamako",
                "Succession Bakary SISSOKO",
                personas.eldestHeir(),
                new BigDecimal("0.5950"),
                78_000_000L,
                geometry,
                List.of(
                        doc("TF", "Titre Foncier n° 18 902 CB", "tf-18902-cb.pdf",
                                "TITRE FONCIER N° 18 902 CB — District de Bamako\n"
                                        + "Titulaire : Bakary SISSOKO (décédé)\n"
                                        + "Livre Foncier de Bamako, volume 41, folio 77\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION"),
                        doc("CESSION", "Acte de notoriété après décès", "notoriete-sissoko.pdf",
                                "ACTE DE NOTORIÉTÉ APRÈS DÉCÈS — Succession Bakary SISSOKO\n"
                                        + "Quatre héritiers réservataires désignés.\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION")));

        advanceTo(parcel, ParcelStatus.SUBMITTED, ParcelStatus.UNDER_VERIFICATION,
                ParcelStatus.UNDER_NOTARY_REVIEW, ParcelStatus.UNDER_ADMIN_REVIEW,
                ParcelStatus.CERTIFIED);
        verifyParcel.execute(new VerifyLandParcelUseCase.Command(parcel.id(), 88));

        SuccessionPlan plan = createSuccessionPlan.execute(parcel.id());
        plan = addHeir.execute(new AddHeirUseCase.Command(plan.id(),
                "Ibrahim SISSOKO", "Fils aîné", 25, "ibrahim.sissoko@demo.laboussole.ml"));
        plan = addHeir.execute(new AddHeirUseCase.Command(plan.id(),
                "Aminata SISSOKO", "Fille", 25, "aminata.sissoko@demo.laboussole.ml"));
        plan = addHeir.execute(new AddHeirUseCase.Command(plan.id(),
                "Moussa SISSOKO", "Fils", 25, "moussa.sissoko@demo.laboussole.ml"));
        plan = addHeir.execute(new AddHeirUseCase.Command(plan.id(),
                "Kadiatou SISSOKO", "Fille", 25, "kadiatou.sissoko@demo.laboussole.ml"));

        submitForVoting.submit(plan.id(), "ibrahim.sissoko@demo.laboussole.ml");

        // Every heir approves in turn; the plan only reaches VALIDATED on the
        // last vote — the "conseil de famille" rule made executable.
        for (var heir : plan.heirs()) {
            voteOnPlan.vote(plan.id(), heir.id(), true, "196.200.88.14");
        }

        var ledgerHash = anchorSuccession.execute(plan.id());

        log.info("Scenario 2 ready: succession plan {} anchored at {}", plan.id().value(), ledgerHash);
        return "  2. BSL-ML-2026-000102 — Sirakoro (Bamako VI) : indivision à 4 héritiers,\n"
                + "     validée à l'unanimité puis scellée sur le registre (" + ledgerHash.substring(0, 18)
                + "…).";
    }

    // ---------------------------------------------------------------------
    // Scenario 3 — "La double attribution" : Kalabancoro
    //
    // The classic Malian conflict: a Lettre d'Attribution from the Mairie over
    // land already covered by a Titre Foncier from the Domaines. Two papers,
    // both looking authentic, one plot. The parcel is parked in DISPUTED.
    // ---------------------------------------------------------------------
    private String scenarioThree(Personas personas) {
        var geometry = fromUtm(List.of(
                utm(605_940, 1_390_120),
                utm(606_030, 1_390_120),
                utm(606_030, 1_390_195),
                utm(605_940, 1_390_195)));

        var parcel = register(
                "BSL-ML-2026-000103",
                "Parcelle de Kalabancoro — îlot 12, lot 8",
                "Kalabancoro, Cercle de Kati, Région de Koulikoro",
                "Sékou KEÏTA",
                personas.contestedOwner(),
                new BigDecimal("0.6750"),
                31_500_000L,
                geometry,
                List.of(
                        doc("LETTRE_ATTRIBUTION", "Lettre d'attribution de la Mairie",
                                "lettre-attribution-kalabancoro.pdf",
                                "LETTRE D'ATTRIBUTION N° 0417/M-K — Mairie de Kalabancoro\n"
                                        + "Attributaire : Sékou KEÏTA — îlot 12, lot 8\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION"),
                        doc("ATTESTATION_COUTUMIERE", "Attestation de vente coutumière",
                                "attestation-coutumiere-kalabancoro.pdf",
                                "ATTESTATION DE VENTE COUTUMIÈRE\n"
                                        + "Établie par le chef de village, témoins à l'appui.\n"
                                        + "DOCUMENT FICTIF — SCÉNARIO DE DÉMONSTRATION")));

        advanceTo(parcel, ParcelStatus.SUBMITTED, ParcelStatus.UNDER_VERIFICATION);

        openDispute.open(parcel.id(),
                "Double attribution : lettre d'attribution n° 0417/M-K délivrée par la Mairie de"
                        + " Kalabancoro sur une emprise déjà couverte par un Titre Foncier inscrit"
                        + " au Livre Foncier. Vérification DNGF requise avant toute certification.",
                DisputeSeverity.HIGH);
        transitionStatus.execute(
                new TransitionParcelStatusUseCase.Command(parcel.id(), ParcelStatus.DISPUTED));

        log.info("Scenario 3 ready: parcel {} parked in DISPUTED", parcel.reference().value());
        return "  3. BSL-ML-2026-000103 — Kalabancoro (Kati) : conflit Mairie / Domaines,\n"
                + "     parcelle bloquée en DISPUTED en attente de vérification DNGF.";
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** UTM 29N — the zone covering Bamako, Kati and Koulikoro. */
    private static UtmCoordinate utm(double easting, double northing) {
        return UtmCoordinate.of(easting, northing, "29N");
    }

    /**
     * Runs the situation-plan vertices through the real converter, so the demo
     * geometry is derived exactly as an operator keying a paper plan would get
     * it — never hard-coded lat/lon.
     */
    private ParcelGeometry fromUtm(List<UtmCoordinate> vertices) {
        var ring = convertUtmPolygon.execute(new ConvertUtmPolygonUseCase.Command(vertices)).stream()
                .map(point -> new ParcelGeometry.Coordinate(point.longitude(), point.latitude()))
                .toList();
        return new ParcelGeometry(ring);
    }

    /**
     * Writes the document to storage and returns the registration command for
     * it. Writing the bytes matters: the registration service fingerprints the
     * file it finds in storage, so this is what makes the SHA-256 seal real
     * rather than null.
     */
    private RegisterLandParcelUseCase.DocumentCommand doc(
            String type, String label, String fileName, String content) {
        var key = storage.store(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                fileName,
                "application/pdf");
        return new RegisterLandParcelUseCase.DocumentCommand(type, label, key, fileName);
    }

    private LandParcel register(
            String reference, String name, String region, String ownerLabel, UserId ownerUserId,
            BigDecimal areaHectares, long valueXof, ParcelGeometry geometry,
            List<RegisterLandParcelUseCase.DocumentCommand> documents) {
        return registerParcel.execute(new RegisterLandParcelUseCase.Command(
                reference, name, region, ownerLabel, ownerUserId,
                areaHectares, valueXof, geometry, documents));
    }

    private void advanceTo(LandParcel parcel, ParcelStatus... path) {
        for (ParcelStatus status : path) {
            transitionStatus.execute(new TransitionParcelStatusUseCase.Command(parcel.id(), status));
        }
    }

    private Personas seedPersonas() {
        return new Personas(
                persona("fatoumata.coulibaly@demo.laboussole.ml", "Fatoumata COULIBALY"),
                persona("ibrahim.sissoko@demo.laboussole.ml", "Ibrahim SISSOKO"),
                persona("aminata.sissoko@demo.laboussole.ml", "Aminata SISSOKO"),
                persona("moussa.sissoko@demo.laboussole.ml", "Moussa SISSOKO"),
                persona("kadiatou.sissoko@demo.laboussole.ml", "Kadiatou SISSOKO"),
                persona("sekou.keita@demo.laboussole.ml", "Sékou KEÏTA"));
    }

    private UserId persona(String email, String fullName) {
        var address = Email.of(email);
        return users.findByEmail(address)
                .map(User::id)
                .orElseGet(() -> users.save(User.register(
                        address, fullName, passwordHasher.hash(demoPassword), Role.CITIZEN)).id());
    }

    private record Personas(
            UserId diasporaOwner,
            UserId eldestHeir,
            UserId secondHeir,
            UserId thirdHeir,
            UserId fourthHeir,
            UserId contestedOwner) {
    }
}

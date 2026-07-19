package com.laboussole.application.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.laboussole.application.usecase.legal.LegalDossier;
import com.laboussole.domain.model.certification.TitleVerificationCase;
import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.out.StorageService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * PDF generation for the three user-facing documents: the LandGuard registry
 * attestation, the Certificat de Vigilance (Feature 02.3, with dynamic QR) and
 * the court-ready evidence dossier (Feature 03.3).
 *
 * <p>LandGuard is a technological trusted third party, not the State (PRD 1.3):
 * no document generated here claims governmental authority, and every claim it
 * prints (hashes, timestamps, statuses) comes from the database.
 */
@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    // Charte graphique (PRD 5.1)
    private static final Color NAVY = new Color(0x0B, 0x19, 0x2C);
    private static final Color EMERALD = new Color(0x00, 0x87, 0x5A);
    private static final Color ALERT_RED = new Color(0xD3, 0x2F, 0x2F);

    private static final DateTimeFormatter DATE_TIME_FR = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)
            .withZone(ZoneId.of("Africa/Bamako"));

    private final StorageService storage;

    public PdfGenerationService(StorageService storage) {
        this.storage = storage;
    }

    /**
     * Attestation d'enregistrement au registre LandGuard. Explicitly not a
     * state-issued land title: it attests what the LandGuard registry holds.
     */
    public byte[] generateRegistryAttestation(LandParcel parcel) {
        return renderDocument(document -> {
            addBrandHeader(document, "ATTESTATION D'ENREGISTREMENT", EMERALD);

            var body = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            var strong = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NAVY);

            document.add(new Paragraph(
                    "La plateforme LandGuard (« La Boussole ») atteste que la parcelle désignée "
                            + "ci-après est inscrite à son registre de surveillance numérique.", body));
            document.add(spacer());

            document.add(new Paragraph("NOM DU DOMAINE : " + parcel.name(), strong));
            document.add(new Paragraph("RÉFÉRENCE CADASTRALE : " + parcel.reference().value(), body));
            if (parcel.titleNumber() != null) {
                document.add(new Paragraph("NUMÉRO DE TITRE DÉCLARÉ : " + parcel.titleNumber(), body));
            }
            document.add(new Paragraph("LOCALISATION : " + parcel.regionLabel(), body));
            document.add(new Paragraph("SUPERFICIE DÉCLARÉE : " + parcel.area().value() + " ha", body));
            document.add(new Paragraph("PROPRIÉTAIRE DÉCLARÉ : " + parcel.ownerLabel(), strong));
            document.add(new Paragraph("STATUT DE VIGILANCE : " + parcel.status().name(), body));

            document.add(spacer());
            document.add(new Paragraph("COORDONNÉES GÉODÉSIQUES (WGS-84) :", strong));
            for (var coord : parcel.geometry().outerRing()) {
                document.add(new Paragraph(
                        String.format(Locale.ROOT, "- Lat : %.6f, Lon : %.6f",
                                coord.latitude(), coord.longitude()), body));
            }

            if (!parcel.documents().isEmpty()) {
                document.add(spacer());
                document.add(new Paragraph("DOCUMENTS SCELLÉS (EMPREINTES SHA-256) :", strong));
                var mono = FontFactory.getFont(FontFactory.COURIER, 8, Color.DARK_GRAY);
                for (var doc : parcel.documents()) {
                    document.add(new Paragraph("- " + doc.label() + " (" + doc.type() + ")", body));
                    document.add(new Paragraph("  " + (doc.sha256Hash() != null
                            ? doc.sha256Hash() : "empreinte indisponible"), mono));
                }
            }

            addDisclaimerFooter(document,
                    "Cette attestation reflète le contenu du registre LandGuard à la date d'émission. "
                            + "Elle ne se substitue pas au Titre Foncier délivré par l'État malien.");
        });
    }

    /**
     * Certificat de Vigilance (Feature 02.3): issued once the human expert has
     * certified the case; the dynamic QR code resolves to the public
     * verification page backed by the live database.
     */
    public byte[] generateVigilanceCertificate(TitleVerificationCase kase, String verificationUrl) {
        return renderDocument(document -> {
            addBrandHeader(document, "CERTIFICAT DE VIGILANCE", EMERALD);

            var body = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            var strong = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NAVY);
            var title = kase.reportedTitle();

            document.add(new Paragraph("RÉFÉRENCE DU DOSSIER : " + kase.caseReference(), strong));
            document.add(new Paragraph("STATUT : " + kase.status().name(), strong));
            if (kase.certifiedAt() != null) {
                document.add(new Paragraph(
                        "CERTIFIÉ LE : " + DATE_TIME_FR.format(kase.certifiedAt()), body));
            }
            document.add(spacer());

            document.add(new Paragraph("TITRE FONCIER VÉRIFIÉ", strong));
            document.add(new Paragraph("Numéro de TF : " + title.tfNumber(), body));
            document.add(new Paragraph("Volume / Folio : " + title.volume() + " / " + title.folio(), body));
            document.add(new Paragraph("Bureau de conservation : " + title.conservationOffice(), body));
            document.add(new Paragraph("Propriétaire : " + title.ownerName(), body));
            document.add(new Paragraph("Superficie : " + title.area().value() + " ha", body));
            document.add(new Paragraph("Localisation : " + title.location(), body));

            var requisition = kase.requisition();
            if (requisition != null) {
                document.add(spacer());
                document.add(new Paragraph("CONTRÔLE DOMANIAL (VÉRIFICATION HUMAINE)", strong));
                document.add(new Paragraph("Réquisition n° " + requisition.requisitionNumber()
                        + " — " + requisition.domainOffice(), body));
                document.add(new Paragraph("Vérificateur : " + requisition.verifierName()
                        + " (" + requisition.verifierRole() + ")", body));
                document.add(checklistLine("Authenticité de la souche confirmée",
                        requisition.authenticityConfirmed()));
                document.add(checklistLine("Absence de conflit détecté",
                        !requisition.conflictDetected()));
                document.add(checklistLine("Absence de litige en cours",
                        !requisition.litigationDetected()));
                document.add(checklistLine("Absence d'hypothèque bloquante",
                        !requisition.mortgageDetected()));
                document.add(checklistLine("Concordance du nom avec le répertoire national",
                        requisition.nameMatchConfirmed()));
            }

            document.add(spacer());
            document.add(new LineSeparator());
            document.add(spacer());

            var qr = qrCodeImage(verificationUrl);
            if (qr != null) {
                qr.setAlignment(Element.ALIGN_CENTER);
                document.add(qr);
            }
            var caption = new Paragraph(
                    "Scannez ce code pour vérifier l'authenticité de ce certificat.\n"
                            + verificationUrl,
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
            caption.setAlignment(Element.ALIGN_CENTER);
            document.add(caption);

            addDisclaimerFooter(document,
                    "Toute version imprimée de ce certificat se vérifie exclusivement par le code "
                            + "QR ci-dessus : la page de vérification affiche l'état actuel de la base "
                            + "de données sécurisée LandGuard.");
        });
    }

    /**
     * Dossier de preuve pour tribunal (Feature 03.3): identité certifiée,
     * coordonnées exactes, chronologie des événements de surveillance et
     * images satellites avant/après horodatées.
     */
    public byte[] generateLegalDossier(LegalDossier dossier) {
        return renderDocument(document -> {
            addBrandHeader(document, "DOSSIER DE PREUVE — SURVEILLANCE FONCIÈRE", NAVY);

            var body = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            var strong = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, NAVY);
            var alert = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ALERT_RED);

            document.add(new Paragraph("IDENTIFICATION DU BIEN ET DU PROPRIÉTAIRE", strong));
            document.add(new Paragraph("Nom de la parcelle : " + dossier.parcel().name(), body));
            document.add(new Paragraph("Référence cadastrale : "
                    + dossier.parcel().reference().value(), body));
            document.add(new Paragraph("Propriétaire enregistré : "
                    + dossier.parcel().ownerLabel(), body));
            if (dossier.ownerEmail() != null) {
                document.add(new Paragraph("Compte certifié : " + dossier.ownerEmail(), body));
            }
            document.add(new Paragraph("Superficie : "
                    + dossier.parcel().area().value() + " ha", body));
            document.add(new Paragraph("Statut : " + dossier.parcel().status().name(), body));

            document.add(spacer());
            document.add(new Paragraph("COORDONNÉES GÉOGRAPHIQUES EXACTES (WGS-84)", strong));
            for (var coord : dossier.parcel().geometry().outerRing()) {
                document.add(new Paragraph(
                        String.format(Locale.ROOT, "- Lat : %.6f, Lon : %.6f",
                                coord.latitude(), coord.longitude()), body));
            }

            document.add(spacer());
            document.add(new Paragraph("CHRONOLOGIE DES ÉVÉNEMENTS DE SURVEILLANCE", strong));
            if (dossier.monitoringEvents().isEmpty()) {
                document.add(new Paragraph("Aucun événement de surveillance enregistré.", body));
            } else {
                for (var event : dossier.monitoringEvents()) {
                    var font = switch (event.severity()) {
                        case HIGH, CRITICAL -> alert;
                        default -> body;
                    };
                    document.add(new Paragraph(String.format("[%s] %s — %s (confiance %d%%)",
                            DATE_TIME_FR.format(event.detectedAt()),
                            event.severity().name(),
                            event.description(),
                            event.confidenceScore()), font));
                }
            }

            document.add(spacer());
            document.add(new Paragraph("IMAGES SATELLITES AVANT / APRÈS (HORODATAGE CERTIFIÉ)", strong));
            var snapshots = dossier.snapshots();
            if (snapshots.size() < 2) {
                document.add(new Paragraph(
                        "Historique d'images insuffisant pour une comparaison avant/après.", body));
            } else {
                addSnapshotEvidence(document, "AVANT", snapshots.get(snapshots.size() - 2), body);
                addSnapshotEvidence(document, "APRÈS", snapshots.get(snapshots.size() - 1), body);
            }

            if (!dossier.heirs().isEmpty() || !dossier.auditTrail().isEmpty()) {
                document.add(spacer());
                document.add(new Paragraph("HISTORIQUE DES SUCCESSIONS", strong));
                for (var event : dossier.auditTrail()) {
                    document.add(new Paragraph(String.format("[%s] %s par %s",
                            DATE_TIME_FR.format(event.createdAt()),
                            event.type().name(),
                            event.actor()), body));
                }
            }

            document.add(spacer());
            document.add(new Paragraph("SCELLEMENT CRYPTOGRAPHIQUE", strong));
            document.add(new Paragraph("Empreinte d'ancrage : "
                    + (dossier.blockchainHash() != null ? dossier.blockchainHash() : "EN ATTENTE"), body));
            document.add(new Paragraph("Contenu QR de vérification : " + dossier.qrCodeContent(), body));

            addDisclaimerFooter(document,
                    "Extrait numérique du registre LandGuard généré le "
                            + DATE_TIME_FR.format(dossier.generatedAt())
                            + ". Destiné à être remis à un huissier de justice pour constat.");
        });
    }

    // ------------------------------------------------------------------
    // Building blocks
    // ------------------------------------------------------------------

    private interface DocumentContent {
        void render(Document document) throws DocumentException;
    }

    private byte[] renderDocument(DocumentContent content) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            content.render(document);
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
        return out.toByteArray();
    }

    private void addBrandHeader(Document document, String title, Color accent)
            throws DocumentException {
        var brand = new Paragraph("LANDGUARD MALI — « LA BOUSSOLE »",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, NAVY));
        brand.setAlignment(Element.ALIGN_CENTER);
        document.add(brand);

        var subtitle = new Paragraph("Tiers de confiance technologique de surveillance foncière",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        document.add(spacer());
        var heading = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, accent));
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);
        document.add(spacer());
        document.add(new LineSeparator());
        document.add(spacer());
    }

    private void addDisclaimerFooter(Document document, String text) throws DocumentException {
        document.add(spacer());
        document.add(new LineSeparator());
        var footer = new Paragraph(text + "\nGénéré le " + DATE_TIME_FR.format(Instant.now())
                + " — LandGuard n'est pas une autorité étatique.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addSnapshotEvidence(
            Document document, String label, SatelliteSnapshot snapshot,
            com.lowagie.text.Font body) throws DocumentException {
        document.add(new Paragraph(String.format(
                "%s — capturée le %s (indice de mouvement %d%%, indice d'anomalie %d%%)",
                label, DATE_TIME_FR.format(snapshot.capturedAt()),
                snapshot.movementScore(), snapshot.anomalyScore()), body));
        var image = snapshotImage(snapshot);
        if (image != null) {
            image.scaleToFit(320, 240);
            document.add(image);
        } else {
            document.add(new Paragraph("Image de référence : " + snapshot.imageUrl(), body));
        }
    }

    /** Loads a snapshot image from storage; the URL's last segment is its key. */
    private Image snapshotImage(SatelliteSnapshot snapshot) {
        String url = snapshot.imageUrl();
        String key = url.substring(url.lastIndexOf('/') + 1);
        try (InputStream in = storage.load(key)) {
            var buffered = ImageIO.read(in);
            return buffered == null ? null : Image.getInstance(buffered, null);
        } catch (Exception e) {
            log.warn("Snapshot image {} not embeddable in dossier: {}", key, e.getMessage());
            return null;
        }
    }

    private Image qrCodeImage(String content) {
        try {
            var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 160, 160);
            return Image.getInstance(MatrixToImageWriter.toBufferedImage(matrix), null);
        } catch (WriterException | java.io.IOException | BadElementException e) {
            log.error("QR code generation failed: {}", e.getMessage());
            return null;
        }
    }

    private Paragraph checklistLine(String label, boolean ok) {
        var font = FontFactory.getFont(FontFactory.HELVETICA, 11, ok ? EMERALD : ALERT_RED);
        return new Paragraph((ok ? "[CONFORME] " : "[NON CONFORME] ") + label, font);
    }

    private static Paragraph spacer() {
        return new Paragraph(" ");
    }
}

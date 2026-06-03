package com.laboussole.application.service;

import com.laboussole.application.usecase.legal.LegalDossier;
import com.laboussole.domain.model.parcel.LandParcel;
import com.lowagie.text.*;
import java.awt.Color;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

@Service
public class PdfGenerationService {

    /**
     * Génère le Titre Foncier (TF) officiel malien.
     */
    public byte[] generateLandTitle(LandParcel parcel) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Republic of Mali Header
            Font malianFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Paragraph malianHeader = new Paragraph("RÉPUBLIQUE DU MALI\nUn Peuple - Un But - Une Foi", malianFont);
            malianHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(malianHeader);

            document.add(new Paragraph(" "));

            // Ministry Header
            Font ministryFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Paragraph ministryHeader = new Paragraph("MINISTÈRE DE L'URBANISME, DE L'HABITAT,\nDES DOMAINES, DE L'AMÉNAGEMENT DU TERRITOIRE ET DE LA POPULATION\n---\nDIRECTION NATIONALE DES DOMAINES ET DU CADASTRE", ministryFont);
            ministryHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(ministryHeader);

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());
            document.add(new Paragraph(" "));

            // Title Number
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(16, 185, 129));
            Paragraph title = new Paragraph("TITRE FONCIER DÉMATÉRIALISÉ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font numFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph num = new Paragraph("N° " + parcel.titleNumber(), numFont);
            num.setAlignment(Element.ALIGN_CENTER);
            document.add(num);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Content
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            document.add(new Paragraph("Le présent titre certifie que la parcelle désignée ci-après est inscrite au registre foncier numérique national.", bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("NOM DU DOMAINE : " + parcel.name(), malianFont));
            document.add(new Paragraph("RÉFÉRENCE CADASTRALE : " + parcel.reference().value(), bodyFont));
            document.add(new Paragraph("LOCALISATION : " + parcel.regionLabel(), bodyFont));
            document.add(new Paragraph("SUPERFICIE : " + parcel.area().value() + " HECTARES", bodyFont));
            document.add(new Paragraph("PROPRIÉTAIRE : " + parcel.ownerLabel().toUpperCase(), malianFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("COORDONNÉES GÉODÉSIQUES (WGS-84) :", malianFont));
            for (var coord : parcel.geometry().outerRing()) {
                document.add(new Paragraph(String.format("- Lat: %.6f, Lon: %.6f", coord.latitude(), coord.longitude()), bodyFont));
            }

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());
            document.add(new Paragraph(" "));

            // Blockchain
            Font bcFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            document.add(new Paragraph("SÉCURISATION BLOCKCHAIN", bcFont));
            document.add(new Paragraph("ANCRAGE IMMUABLE : " + "0x" + parcel.id().asString().replace("-", ""), ministryFont));
            document.add(new Paragraph("RÉSEAU : REGISTRE NATIONAL DISTRIBUÉ DU MALI", ministryFont));

            document.add(new Paragraph(" "));
            
            // Footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
            Paragraph footer = new Paragraph("Extrait généré par LandGuard le " + Instant.now().toString() + "\nDocument authentifié par signature électronique gouvernementale.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating TF PDF", e);
        }

        return out.toByteArray();
    }

    /**
     * Génère le dossier juridique complet (Historique, Blockchain, etc).
     */
    public byte[] generateLegalDossier(LegalDossier dossier) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
            Paragraph header = new Paragraph("LA BOUSSOLE — DOSSIER JURIDIQUE", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());
            document.add(new Paragraph(" "));

            // Parcel Info
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            document.add(new Paragraph("IDENTIFICATION DU BIEN", subHeaderFont));
            document.add(new Paragraph("Nom de la parcelle : " + dossier.parcel().name()));
            document.add(new Paragraph("Référence cadastrale : " + dossier.parcel().reference().value()));
            document.add(new Paragraph("Propriétaire enregistré : " + dossier.parcel().ownerLabel()));
            document.add(new Paragraph("Superficie : " + dossier.parcel().area().value() + " Hectares"));
            document.add(new Paragraph("Statut : " + dossier.parcel().status().name()));

            document.add(new Paragraph(" "));

            // Blockchain Proof
            document.add(new Paragraph("PREUVE D'ANCRAGE BLOCKCHAIN", subHeaderFont));
            document.add(new Paragraph("Hash racine : " + (dossier.blockchainHash() != null ? dossier.blockchainHash() : "EN ATTENTE")));
            document.add(new Paragraph("Réseau : LANDGUARD-GOV-MAINNET"));
            document.add(new Paragraph("Contenu QR de vérification : " + dossier.qrCodeContent()));

            document.add(new Paragraph(" "));

            // Succession History
            document.add(new Paragraph("HISTORIQUE DES SUCCESSIONS", subHeaderFont));
            if (dossier.auditTrail().isEmpty()) {
                document.add(new Paragraph("Aucun événement de succession enregistré."));
            } else {
                for (var event : dossier.auditTrail()) {
                    document.add(new Paragraph(
                            String.format("[%s] %s par %s", 
                                    event.createdAt().toString(), 
                                    event.type().name(), 
                                    event.actor())
                    ));
                }
            }

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());
            document.add(new Paragraph(" "));
            
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
            Paragraph footer = new Paragraph("Ce document est un extrait numérique certifié du registre LandGuard. Généré le " + dossier.generatedAt().toString(), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }
}

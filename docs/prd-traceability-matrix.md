# Matrice de traçabilité PRD → Implémentation

> Référence : PRD v2.0 (`cahier_des_charges_la_boussole.pdf`).
> Statuts : ✅ couvert · 🟡 partiel (écart documenté) · ❌ non couvert.
> Complément : [spécification fonctionnelle](functional-specification.md).

## Épic 1 — Vision Live (jumeau numérique cadastral)

| Feature PRD | Exigence | Implémentation | Statut |
| --- | --- | --- | --- |
| 01.1 Conversion UTM → GeoJSON | Zones UTM 29N/30N converties pour affichage carte ; le polygone se dessine à partir du plan papier | `UtmToWgs84Converter`, `ConvertUtmPolygonService`, `POST /api/v1/geo/utm-to-geojson` ; saisie dans `/registre/nouveau` — tests : `UtmToWgs84ConverterTest`, `ConvertUtmPolygonServiceTest`, `UtmCoordinateTest` | ✅ |
| 01.2 Carte hybride haute performance | Carte plein écran, tracés vectoriels sur fond satellite, panneau latéral (TF, superficie calculée vs officielle, statut de vigilance) | Leaflet + react-leaflet, page `/carte`, fiche parcelle latérale ; géométrie servie par `GET /api/v1/parcels` | ✅ |
| 01.3 Curseur temporel | Rejouer l'historique visuel du terrain, dater l'apparition d'une construction | `satellite-timeline.tsx` + composant slider sur `/surveillance` ; historique servi par `GET /api/v1/monitoring/snapshots` (clichés horodatés persistés) | ✅ |

## Épic 2 — Certification « Double Clé »

| Feature PRD | Exigence | Implémentation | Statut |
| --- | --- | --- | --- |
| 02.1 Tunnel de dépôt structuré | Upload + catégorisation selon la nomenclature malienne (TF, Lettre d'Attribution, Permis d'Occuper, Attestation Coutumière) ; statut `PENDING_VERIFICATION` | `POST /api/v1/documents/upload`, scellement SHA-256 (`LandDocument`), statut porté par le dossier (`VerificationStatus.PENDING_VERIFICATION`) ; `DocumentType` couvre la nomenclature complète (`TF`, `LETTRE_ATTRIBUTION`, `PERMIS_OCCUPER`, `ATTESTATION_COUTUMIERE`, plus `PLAN`, `ID`, `CESSION`, `TAX`) et le tunnel web propose le choix de la nature du titre | ✅ |
| 02.2 Console d'instruction experts | Backoffice notaires/juristes ; checklist obligatoire : absence de litige, absence d'hypothèque bloquante, concordance du nom | Workflow `TitleVerificationCase` (`DRAFT → PENDING_VERIFICATION → DOMAIN_CONTROL → TF_VERIFIED/DISPUTE_SIGNALED/TF_REJECTED → CERTIFIED`) ; checklist complète portée par `TitleVerificationRequisition` (authenticité, litige, **hypothèque bloquante**, **concordance du nom**) — `TF_VERIFIED` exige `checklistPassed()` ; console `/certification`, formulaire `/verification` — tests : `TitleVerificationCaseTest` | ✅ |
| 02.3 Scellement + Certificat de Vigilance | Statut `VERIFIED`, certificat avec QR code dynamique renvoyant à la base sécurisée | `certify()` (réservé aux dossiers `TF_VERIFIED`), PDF de certification avec QR code (`PdfGenerationService`), endpoint public `GET /api/v1/public/certificates/{caseReference}` | ✅ |

## Épic 3 — Surveillance active et alerte intrusion

| Feature PRD | Exigence | Implémentation | Statut |
| --- | --- | --- | --- |
| 03.1 Moteur d'analyse des écarts pixels | Ignorer les changements saisonniers (hivernage/saison sèche), détecter uniquement les anomalies structurelles | `PixelDifferenceAnalyzer` : score sur la structure en luminance (insensible aux teintes), signal `seasonalShiftDetected` séparé ; pipeline `IngestSatelliteSnapshotService` | ✅ |
| 03.2 Routage des alertes multi-canaux | Push, e-mail (diaspora), SMS d'urgence | `MultiChannelAlertRoutingService` + `PushAlertAdapter`, `EmailAlertAdapter`, `SmsAlertAdapter`/`TwilioSmsAdapter` ; préférences par utilisateur ; canaux isolés en cas de panne — tests : `MultiChannelAlertRoutingServiceTest`, `TwilioSmsAdapterTest` | ✅ |
| 03.3 Dossier de preuve pour tribunal | PDF : identité certifiée, coordonnées exactes, images avant/après horodatées, remis à huissier | `GenerateLegalProofService`, `LegalDossier`, `POST /api/v1/legal/generate-proof`, `GET /api/v1/legal/export-dossier` ; pages `/preuves`, `/juridique` | ✅ |

## Épic 4 — Conseil de famille (successions)

| Feature PRD | Exigence | Implémentation | Statut |
| --- | --- | --- | --- |
| 04.1 Espace de co-propriété familiale | Associer plusieurs comptes (héritiers) à un même titre | `SuccessionPlan` + `Heir.linkedUserId`, `POST /api/v1/heritage/plans/{id}/heirs`, quote-parts ≤ 100 %, vote unanime, ancrage puis transfert ; page `/heritage` | ✅ |
| 04.2 Notification partagée | Alertes envoyées simultanément à tous les membres inscrits ; aucun membre ne peut agir en cachette | `MultiChannelAlertRoutingService.resolveRecipients()` : propriétaire + héritiers liés, dédupliqués — tests `alertsLinkedHeirsAlongsideTheOwner`, `doesNotAlertTheSameUserTwiceWhenOwnerIsAlsoAnHeir` ; audit `GET /api/v1/heritage/plans/{id}/audit` | ✅ |

## Section 4 — Intelligence artificielle

| Exigence PRD | Implémentation | Statut |
| --- | --- | --- |
| 4.1 IA de vision : indice de confiance d'intrusion, alerte au-delà de 75 % | `IngestSatelliteSnapshotService.ALERT_CONFIDENCE_THRESHOLD = 75` ; événement `MonitoringEvent` → routage multi-canaux | ✅ |
| 4.2 OCR intelligent : extraction (n° TF, nom, superficie), pré-remplissage, détection d'anomalies (polices, tampons) | `TesseractOcrAdapter` (OCR réel), `OcrTextFieldExtractor` (entités clés), `OcrLayoutAnalyzer` (anomalies de mise en page), `GET /api/v1/documents/{id}/ocr-extraction` — tests : `ExtractDocumentDataServiceTest`, `OcrTextFieldExtractorTest`, `OcrLayoutAnalyzerTest` | ✅ |
| IA absente des décisions juridiques pures | La certification exige `TitleVerificationRequisition` saisie par un vérificateur humain ; `certify()` requiert une autorité humaine | ✅ |

## Section 2 — Charges techniques et sécurité

| Exigence PRD | Implémentation | Statut |
| --- | --- | --- |
| 2.1 SHA-256 des documents et titres ; scellement, toute modification rompt la chaîne | `Sha256` (empreinte de chaque document), `HashChainLedgerAdapter` (chaîne `hash = SHA-256(précédent ∥ charge)` persistée dans `blockchain_records`) | ✅ |
| 2.1 Chiffrement au repos AES-256 des titres et plans | Stockage fichier local (`LocalFileSystemStorageService`) **sans chiffrement applicatif** ; à couvrir par chiffrement applicatif ou au niveau du volume/cloud de déploiement | ❌ |
| 2.1 Flux satellite Sentinel-2 (10 m / 5 jours), flux haute résolution premium | Pipeline d'ingestion réel (`POST /api/v1/monitoring/snapshots` : stockage, comparaison, score, alerte) ; **l'acquisition automatique auprès de l'API Sentinel-2 n'est pas branchée** — l'ingestion est déclenchée par dépôt d'image | 🟡 |
| 2.2 Réseau physique (clercs, enquêteurs, géomètres) | Modélisé côté système : réquisition aux Domaines (`TitleVerificationRequisition` avec agent, bureau, rôle), rôles `NOTARY`/`SURVEYOR` | ✅ (part logicielle) |

## Section 5 — UX/UI

| Exigence PRD | Implémentation | Statut |
| --- | --- | --- |
| 5.1 Charte graphique (bleu marine, vert émeraude, rouge alerte, gris technique) | Thème Tailwind de `landguard-web` | ✅ |
| 5.2 Scannabilité, responsive mobile | Dashboard à statuts visuels, Next.js/Tailwind responsive | ✅ |
| 5.2 Conformité linguistique (code en anglais, UI en français) | Code et fichiers en anglais ; écrans, alertes et PDF en français | ✅ |

## Synthèse des écarts

Les épics 1 à 4 sont couverts à 100 %. Écarts restants, hors backlog métier
(section 2 du PRD — infrastructure) :

| # | Écart | Feature | Action proposée |
| --- | --- | --- | --- |
| 1 | Pas de chiffrement AES-256 au repos des documents | 2.1 | Chiffrer côté `StorageService` (AES-256-GCM, clé gérée en configuration) |
| 2 | Acquisition Sentinel-2 non automatisée | 2.1 / 03.1 | Connecteur d'acquisition planifiée (hors périmètre du socle actuel ; l'ingestion et l'analyse sont prêtes) |

# LA BOUSSOLE — Spécification fonctionnelle

> Document de référence fonctionnel de la plateforme LandGuard Mali (« La Boussole »),
> aligné sur le PRD v2.0 (`cahier_des_charges_la_boussole.pdf`).
> La couverture exigence par exigence est détaillée dans la
> [matrice de traçabilité](prd-traceability-matrix.md).

## 1. Vue d'ensemble

La Boussole est un tiers de confiance technologique pour la sécurisation foncière au
Mali. Elle transforme un document papier et une parcelle physique en **jumeau numérique
surveillé** : cartographie précise, certification par des experts humains, scellement
cryptographique, surveillance satellite et gestion des successions en indivision.

La plateforme se compose de :

| Composant | Technologie | Rôle |
| --- | --- | --- |
| `landguard-api` | Java 21 / Spring Boot 3.5, architecture hexagonale | API métier, règles de domaine, persistance MySQL |
| `landguard-web` | Next.js 16 (App Router), Tailwind v4, shadcn/ui, Leaflet | Interface utilisateur (français), carte interactive |
| Registre local | Chaîne de hachage SHA-256 en base (`blockchain_records`) | Ancrage inviolable des certificats et successions |

Conformité linguistique (PRD 5.2) : code, fichiers et architecture **en anglais** ;
interface, alertes, notifications et PDF **en français**.

## 2. Acteurs

| Acteur | Rôle applicatif | Capacités principales |
| --- | --- | --- |
| Propriétaire / diaspora | `CITIZEN` | Enregistrer une parcelle, déposer des documents, suivre les alertes, gérer sa succession |
| Héritier | `CITIZEN` (compte lié à un plan de succession) | Recevoir toutes les alertes de la parcelle, voter sur le plan de succession |
| Expert (notaire, juriste, géomètre) | `NOTARY` / `SURVEYOR` | Instruire les dossiers de vérification, enregistrer la réquisition aux Domaines, certifier |
| Administrateur | `ADMIN` | Revue finale, émission du titre, résolution des litiges |
| Tiers vérificateur | (aucun compte) | Vérifier un certificat via le QR code public |

## 3. Workflows fonctionnels

### 3.1 Enregistrement d'une parcelle (Épic 1 & 2)

1. **Saisie** — L'utilisateur crée la parcelle (`POST /api/v1/parcels`) : référence
   cadastrale `BSL-ML-…`, commune, superficie, valeur estimée (XOF).
2. **Géométrie** — Les coordonnées UTM du plan papier (zones 29N/30N) sont converties
   en GeoJSON (`POST /api/v1/geo/utm-to-geojson`, `ConvertUtmPolygonService`) ; le
   polygone se dessine automatiquement sur la carte du Mali.
3. **Dépôt des pièces** — Chaque document (`POST /api/v1/documents/upload`) est
   catégorisé selon la nomenclature malienne — la nature du titre se choisit parmi
   **Titre Foncier**, **Lettre d'Attribution**, **Permis d'Occuper** ou
   **Attestation de Vente Coutumière** —, scellé par empreinte **SHA-256**
   (`LandDocument.sha256Hash`) puis stocké. L'OCR (Tesseract) lit le document, extrait les entités clés (numéro de TF,
   nom, superficie) et pré-remplit le formulaire ; l'analyse de mise en page signale
   les anomalies structurelles (polices incohérentes, tampons suspects).
4. **Cycle de vie** — La parcelle suit la machine à états `ParcelStatus` :
   `UNDER_SURVEY → UNDER_VERIFICATION → UNDER_NOTARY_REVIEW → UNDER_ADMIN_REVIEW →
   CERTIFIED → TITLE_ISSUED`, avec bifurcations `DISPUTED` / `REJECTED` / `ARCHIVED`.

Interface : `/registre`, `/registre/nouveau`, `/carte`.

### 3.2 Certification « Double Clé » (Épic 2)

La certitude juridique est établie **avant** d'activer la surveillance.

1. **Ouverture du dossier** (`POST /api/v1/title-verifications`) — le titre déclaré
   est enregistré, le dossier reçoit une référence unique (`caseReference`).
2. **Instruction** — workflow `VerificationStatus` :
   `DRAFT → PENDING_VERIFICATION → DOMAIN_CONTROL → TF_VERIFIED | DISPUTE_SIGNALED |
   TF_REJECTED → CERTIFIED`.
3. **Réquisition aux Domaines** — un vérificateur physique (clerc de notaire,
   enquêteur foncier) consigne le contrôle de la « souche » au bureau des Domaines
   (`TitleVerificationRequisition`) avec la **checklist obligatoire** : authenticité
   de la souche confirmée, absence de litige en cours, absence d'hypothèque
   bloquante, concordance du nom avec le répertoire national. Le titre n'est
   `TF_VERIFIED` que si tous les contrôles passent ; un litige le bascule en
   `DISPUTE_SIGNALED`, tout autre échec en `TF_REJECTED` avec motif obligatoire.
4. **Certification** — seul un dossier `TF_VERIFIED` peut être certifié
   (`certify()` lève une exception sinon) ; l'identité de l'autorité certifiante et
   l'horodatage sont conservés.
5. **Certificat de Vigilance** — le PDF de certification embarque un **QR code
   dynamique** pointant vers `GET /api/v1/public/certificates/{caseReference}`
   (endpoint public sans authentification) : le scan d'un certificat imprimé, même
   falsifié, affiche la vérité de la base LandGuard.

Interface : `/certification` (console d'instruction), `/verification` (vérification
d'un titre).

### 3.3 Surveillance active et alerte intrusion (Épic 3)

1. **Ingestion d'imagerie** (`POST /api/v1/monitoring/snapshots`,
   `IngestSatelliteSnapshotService`) — l'image est stockée, comparée au cliché
   précédent de la parcelle par le moteur d'analyse de différence de pixels.
2. **Analyse structurelle adaptée au Mali** (`PixelDifferenceAnalyzer`) — le score
   est calculé sur la **structure locale en luminance**, pas sur la couleur brute :
   le verdissement de l'hivernage ou l'assèchement de la saison sèche décalent les
   teintes sans créer d'arêtes structurelles (murs, briques, tas de sable). Un
   décalage global de teinte est signalé (`seasonalShiftDetected`) sans lever
   d'alerte.
3. **Seuil PRD** — au-delà de **75 %** de confiance d'intrusion
   (`ALERT_CONFIDENCE_THRESHOLD`), un événement de surveillance est créé.
4. **Routage multi-canaux** (`MultiChannelAlertRoutingService`) — les événements
   `HIGH`/`CRITICAL` sont diffusés en parallèle sur les canaux activés par chaque
   destinataire : **push**, **e-mail** (diaspora), **SMS** (faible couverture
   Internet, passerelle Twilio). Les canaux sont isolés en cas de panne : si la
   passerelle SMS tombe, l'e-mail part quand même. Les préférences par utilisateur
   sont gérées via `/api/v1/notifications/preferences` (défaut : push + e-mail).
5. **Destinataires** — le propriétaire **et** tous les héritiers dont le compte est
   lié au plan de succession de la parcelle (Épic 4), dédupliqués.
6. **Temps réel** — flux SSE `GET /api/v1/monitoring/stream` pour le tableau de bord.
7. **Dossier de preuve pour tribunal** (`POST /api/v1/legal/generate-proof`,
   `GET /api/v1/legal/export-dossier`) — PDF officiel : identité certifiée du
   propriétaire, coordonnées exactes, images satellites avant/après horodatées,
   destiné à un huissier de justice.

Interface : `/surveillance` (dont curseur temporel rejouant l'historique visuel du
terrain), `/preuves`, `/juridique`.

### 3.4 Conseil de famille — successions (Épic 4)

1. **Plan de succession** (`POST /api/v1/heritage/plans`) — rattaché à une parcelle,
   il recense les héritiers : nom, lien de parenté, quote-part (total ≤ 100 %),
   et le **compte utilisateur lié** (`Heir.linkedUserId`) quand l'héritier est
   inscrit sur la plateforme.
2. **Vote familial** — cycle `SuccessionStatus` :
   `DRAFT → IN_VOTING → VALIDATED | REJECTED → ANCHORED → TRANSFERRED`.
   Chaque héritier vote ; un refus rejette le plan, l'unanimité le valide.
3. **Ancrage** — un plan validé est ancré sur le registre de hachage local
   (`POST …/anchor`) ; le transfert de propriété n'est possible qu'après ancrage.
4. **Notification partagée** — toute alerte de surveillance ou changement de statut
   est envoyée **simultanément** à tous les membres inscrits : aucun membre ne peut
   agir en cachette. Un audit du plan est consultable (`GET …/plans/{id}/audit`).

Interface : `/heritage`.

### 3.5 Scellement cryptographique

- **Empreintes** : SHA-256 (`Sha256`) sur chaque document déposé.
- **Registre local en chaîne** (`HashChainLedgerAdapter`) : chaque ancrage calcule
  `hash = SHA-256(hash_précédent ∥ charge_utile)` et le persiste dans
  `blockchain_records`. La modification d'un seul enregistrement invalide tous les
  hachages suivants — « toute modification rompt la chaîne de validation » (PRD 2.1)
  — sans dépendre d'un réseau blockchain externe.
- **Consultation** : `GET /api/v1/blockchain/records`, interface `/blockchain`.

## 4. Sécurité applicative

- Authentification JWT sans état avec rotation du jeton de rafraîchissement
  (`/api/v1/auth/*`).
- Visibilité des parcelles restreinte au principal authentifié (correctif IDOR).
- Seuls les endpoints publics déclarés (`PublicEndpoints`) sont accessibles sans
  jeton — dont la vérification de certificat par QR code, volontairement publique.
- Compte administrateur d'amorçage configurable (`BootstrapAdminInitializer`),
  aucun identifiant en dur.

## 5. Écrans de l'application web

| Route | Écran | Épic |
| --- | --- | --- |
| `/dashboard` | Statut de sécurité foncière en un coup d'œil (scannabilité) | Transverse |
| `/carte` | Carte hybride Leaflet plein écran, panneau latéral de fiche parcelle | 1 |
| `/registre`, `/registre/nouveau` | Registre des parcelles, tunnel d'enregistrement | 1 & 2 |
| `/certification` | Console d'instruction des dossiers (experts) | 2 |
| `/verification` | Vérification d'un titre / certificat | 2 |
| `/surveillance` | Événements satellite, curseur temporel, flux temps réel | 3 |
| `/preuves`, `/juridique` | Dossiers de preuve, litiges | 3 |
| `/heritage` | Conseil de famille, plans de succession, votes | 4 |
| `/blockchain` | Registre d'ancrage (chaîne de hachage) | 2 & 4 |
| `/notifications` (préférences via profil) | Canaux d'alerte par utilisateur | 3 |

Charte graphique (PRD 5.1) : bleu marine `#0B192C` (confiance), vert émeraude
`#00875A` (conformité), rouge alerte `#D32F2F` (anomalie), gris technique `#F4F5F7`.

## 6. Règles métier couvertes par des tests automatisés

| Règle | Test |
| --- | --- |
| Conversion UTM 29N/30N → WGS84 / GeoJSON | `UtmToWgs84ConverterTest`, `ConvertUtmPolygonServiceTest`, `UtmCoordinateTest` |
| Routage multi-canaux, isolation des pannes, préférences, seuils de sévérité | `MultiChannelAlertRoutingServiceTest` |
| Héritiers liés alertés avec le propriétaire, déduplication | `MultiChannelAlertRoutingServiceTest` |
| Checklist d'instruction (litige, hypothèque, concordance du nom) et transitions de certification | `TitleVerificationCaseTest` |
| Préférences de notification (défauts push + e-mail) | `NotificationPreferencesTest` |
| Extraction OCR des entités (TF, nom, superficie) | `ExtractDocumentDataServiceTest`, `OcrTextFieldExtractorTest` |
| Détection d'anomalies de mise en page documentaire | `OcrLayoutAnalyzerTest` |
| Envoi SMS Twilio | `TwilioSmsAdapterTest` |

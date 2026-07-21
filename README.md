# LA BOUSSOLE

Plateforme africaine de sécurisation, surveillance et valorisation foncière.

> **Status — slice 1 livré :** auth complète bout-en-bout (Spring Boot 3 + MySQL + JWT
> rotatifs, Next.js + Zustand + TanStack Query). Les modules métier (parcelles,
> surveillance satellite, blockchain, etc.) suivent dans les slices suivants.

## Layout

| Dossier            | Description                                                                  |
| ------------------ | ---------------------------------------------------------------------------- |
| `landguard-api/`   | Backend Java 21 / Spring Boot 3.5. Architecture hexagonale (DDD).             |
| `landguard-web/`   | Frontend Next.js 16 (App Router) + Tailwind v4 + shadcn/ui.                   |

## Prérequis

- **JDK 21+**
- **MySQL 8** en écoute sur `localhost:3306` (la base `laboussole` est créée au premier
  démarrage)
- **Node.js 20+**

## Démarrage rapide

```bash
# 1. Backend — renseigner le mot de passe de votre MySQL local, une seule fois
cd landguard-api
cp application-local.yml.example application-local.yml
$EDITOR application-local.yml          # y mettre spring.datasource.password
./mvnw spring-boot:run

# 2. Frontend, dans un second terminal
cd landguard-web
cp .env.local.example .env.local
npm install
npm run dev
```

`application-local.yml` est git-ignoré : votre mot de passe MySQL n'atteint jamais un
commit. Le profil `dev` (actif par défaut) fournit déjà des clés JWT et AES jetables, donc
c'est en général la seule valeur à renseigner.

Alternative sans fichier, si vous préférez les variables d'environnement :

```bash
DATASOURCE_PASSWORD='<mot de passe MySQL>' ./mvnw spring-boot:run
```

### Déploiement (hors profil `dev`)

Aucune valeur sensible n'a de défaut : chaque variable doit être fournie par
l'environnement, sinon l'API refuse de démarrer.

```bash
export DATASOURCE_URL='jdbc:mysql://…'
export DATASOURCE_USERNAME='…'
export DATASOURCE_PASSWORD='…'
export JWT_SECRET="$(openssl rand -base64 48)"
export JWT_REFRESH_SECRET="$(openssl rand -base64 48)"
export STORAGE_ENCRYPTION_KEY="$(openssl rand -base64 32)"
export STORAGE_ROOT='/var/lib/laboussole/uploads'
export SPRING_PROFILES_ACTIVE=prod
java -jar landguard-api/target/landguard-api.jar
```

> **`STORAGE_ENCRYPTION_KEY` est irremplaçable.** Elle déchiffre les titres fonciers et
> les pièces d'identité stockés. La perdre rend tous les documents illisibles : sauvegardez-la
> hors du dépôt, dans un gestionnaire de secrets.

### Politique de secrets

`application.yml` ne contient plus aucune valeur par défaut sensible. Conséquences :

- un déploiement qui oublie une variable d'environnement **refuse de démarrer** au lieu de
  se rabattre sur une valeur lisible dans le dépôt ;
- `SecretsHardeningValidator` interrompt le démarrage, hors profil `dev`, si un secret est
  absent, trop court, ou correspond à une valeur de développement ou déjà divulguée. La
  comparaison se fait par empreinte SHA-256 : aucun secret n'est réintroduit dans le code.

- Frontend : <http://localhost:3000>
- API : <http://localhost:8080/api/v1>
- OpenAPI Swagger : <http://localhost:8080/swagger-ui.html>

## Comptes de démonstration (profil `dev`)

Au démarrage en profil `dev` (profil par défaut), l'API crée automatiquement **un compte actif par rôle**
via `BootstrapDevUsersInitializer`. Idempotent : les e-mails déjà présents ne sont jamais réécrits.

| E-mail                 | Rôle      | Mot de passe    |
| ---------------------- | --------- | --------------- |
| `admin@laboussole.ml`  | `ADMIN`   | `Boussole2026!` |
| `officer@laboussole.ml`| `OFFICER` | `Boussole2026!` |
| `legal@laboussole.ml`  | `LEGAL`   | `Boussole2026!` |
| `banker@laboussole.ml` | `BANKER`  | `Boussole2026!` |
| `citizen@laboussole.ml`| `CITIZEN` | `Boussole2026!` |

> ⚠️ **Développement uniquement.** Le seeder est annoté `@Profile("dev")` : il ne s'exécute
> **jamais** en staging/production, où les comptes proviennent de `BootstrapAdminInitializer`
> (identifiants fournis par l'environnement — aucune credential n'est jamais commitée).

Réglages (dans `application.yml`, section `laboussole.bootstrap.dev-users`) :

| Variable d'env                 | Défaut          | Rôle                                      |
| ------------------------------ | --------------- | ----------------------------------------- |
| `BOOTSTRAP_DEV_USERS_ENABLED`  | `true`          | Mettre à `false` pour désactiver le seed. |
| `BOOTSTRAP_DEV_USERS_PASSWORD` | `Boussole2026!` | Mot de passe partagé des comptes de démo. |

## Scénario de démonstration bout-en-bout (profil `demo`)

```bash
cd landguard-api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

`DemoScenarioInitializer` déroule trois dossiers fonciers complets **en passant par les
vrais use cases** — jamais par des `INSERT` directs. Les polygones sortent donc du
convertisseur UTM 29N, les empreintes des actes sont de vrais SHA-256 calculés sur des
fichiers réellement écrits en stockage, et l'ancrage successoral est un sceau chaîné
authentique. Idempotent, et `@Profile("demo")` : jamais actif hors développement.

| Référence            | Localité              | Ce qu'il démontre                                              |
| -------------------- | --------------------- | -------------------------------------------------------------- |
| `BSL-ML-2026-000101` | Safo (Kati)           | Titre Foncier délivré → construction illicite détectée par satellite → alerte CRITICAL + litige |
| `BSL-ML-2026-000102` | Sirakoro (Bamako VI)  | Indivision à 4 héritiers, vote unanime, plan scellé sur le registre |
| `BSL-ML-2026-000103` | Kalabancoro (Kati)    | Double attribution Mairie / Domaines, parcelle bloquée en `DISPUTED` |

Comptes du scénario (même mot de passe que les comptes `dev`) :
`fatoumata.coulibaly@`, `ibrahim.sissoko@`, `aminata.sissoko@`, `moussa.sissoko@`,
`kadiatou.sissoko@`, `sekou.keita@` — tous sur `demo.laboussole.ml`.

À observer une fois démarré :

- `GET /api/v1/blockchain/verify` → `INTACT`, le plan successoral est scellé et rejouable.
- `GET /api/v1/monitoring/events` → l'alerte CRITICAL de Safo.
- Table `alert_deliveries` → le dispatcher draine la file toutes les 10 s. `PUSH` aboutit ;
  `EMAIL` et `SMS` échouent tant que SMTP/Twilio ne sont pas configurés — c'est voulu, le
  journal montre alors le backoff exponentiel puis la mise en `DEAD_LETTER`.

> Toutes les personnes, parcelles et références de titres sont **fictives**. Les localités
> sont réelles et les formats d'actes suivent la pratique foncière malienne.

## Endpoints d'authentification

| Méthode | Route                       | Auth requise | Description                           |
| ------- | --------------------------- | ------------ | ------------------------------------- |
| POST    | `/api/v1/auth/register`     | ❌            | Création de compte (renvoie session)  |
| POST    | `/api/v1/auth/login`        | ❌            | Connexion par e-mail + mot de passe   |
| POST    | `/api/v1/auth/refresh`      | ❌ (refresh) | Rotation du refresh + nouvel access   |
| POST    | `/api/v1/auth/logout`       | ❌            | Révocation idempotente du refresh     |
| GET     | `/api/v1/auth/me`           | ✅ (Bearer)  | Profil de l'utilisateur courant       |

## Architecture backend

```
landguard-api/src/main/java/com/laboussole/
├── domain/                 # pur Java, zéro Spring/JPA
│   ├── model/              # User, RefreshToken, Email, Role, …
│   ├── port/in/            # use case interfaces (driving ports)
│   ├── port/out/           # repos, hasher, token issuer (driven ports)
│   └── exception/          # exceptions de domaine porteuses d'un code stable
├── application/usecase/    # implémentations des input ports, transactionnelles
├── infrastructure/
│   ├── persistence/        # JPA entities + adapters traduisant en domaine
│   ├── security/           # JwtTokenIssuer, BCryptPasswordHasher, SecurityConfig
│   └── config/             # OpenAPI, etc.
└── interfaces/rest/        # controllers + DTOs + GlobalExceptionHandler
```

Règles : le domaine ne dépend de rien ; l'application dépend du domaine ; l'infrastructure
implémente les ports de sortie ; les controllers sont des adapters d'entrée.

## Sécurité

- **Access tokens** JWT signés HS256, TTL 15 min, claims `sub`, `role`, `iss`, `exp`.
- **Refresh tokens** opaques (Base64URL, 48 octets), stockés en SHA-256, TTL 30 jours,
  rotation à chaque usage avec **détection de réutilisation** (la chaîne entière est
  révoquée si un token déjà utilisé est représenté).
- **BCrypt** pour les mots de passe (cost 12).
- CORS configurable par env (`CORS_ALLOWED_ORIGINS`).
- Stateless — aucune session HTTP côté serveur.

## Architecture frontend

- `lib/api/` — client Axios + interceptors (refresh automatique sur 401, retry une fois).
- `lib/store/auth-store.ts` — Zustand persisté (localStorage) ; bootstrap du client API au
  module load.
- `lib/providers/` — `QueryProvider` (TanStack Query) + `AuthBootstrap` (charge `/me`).
- `components/auth/` — `LoginForm`, `RegisterForm`, `AuthGuard`.
- `app/(app)/layout.tsx` — protégé par `AuthGuard` côté client (les tokens vivent en
  localStorage, hors de portée du middleware).

## Roadmap

- Slice 2 — Parcelles + carte Mapbox (LandParcel domaine, PostGIS, polygones réels).
- Slice 3 — Surveillance satellite + flux d'alertes simulé.
- Slice 4 — Preuves blockchain + export PDF juridique.
- Slice 5 — Évaluation bancaire + scoring de fraude IA.
- Slice 6 — Mode terrain mobile (offline, QR, voix).

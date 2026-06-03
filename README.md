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
| `docker-compose.yml` | Stack locale : MySQL 8, Redis, API.                                          |

## Démarrage rapide

```bash
# 1. Backend + DB via Docker
cp .env.example .env
docker compose up --build

# 2. Frontend
cd landguard-web
cp .env.local.example .env.local
npm install
npm run dev
```

- Frontend : <http://localhost:3000>
- API : <http://localhost:8080/api/v1>
- OpenAPI Swagger : <http://localhost:8080/swagger-ui.html>

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

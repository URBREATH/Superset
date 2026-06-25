# Superset Keycloak Staging (safe rollout)

This guide lets you test Superset + Keycloak login on a separate instance,
while keeping the current embedded flow unchanged.

## Goal

- Keep current stack (`docker-compose-v6.yml`) for embedded dashboards.
- Run the Superset instance on `http://localhost:17789` with Keycloak OAuth enabled.
- Validate login, user provisioning, and role mapping before any production switch.

## Files

- `docker-compose-v6.yml`: current embedded stack (unchanged)
- `docker-compose-v6-keycloak.yml`: staging Superset with OAuth enabled
- `.env.v6.keycloak.example`: placeholders to copy and fill
- `superset_config.py`: OAuth logic controlled by `ENABLE_KEYCLOAK_OAUTH`

## 1) Create env file

Copy and edit the template:

```powershell
Set-Location "C:\Progetti\URBreath\Superset"
Copy-Item ".env.v6.keycloak.example" ".env.v6.keycloak"
```

## 2) Fill placeholders (where to get each value)

- `SUPERSET_SECRET_KEY`
  - Generate a long random secret (internal app secret).
- `KEYCLOAK_BASE_URL`
  - Keycloak base URL from your IAM team/admin.
  - Example: `https://keycloak.company.it`
- `KEYCLOAK_REALM`
  - Realm name shown in Keycloak Admin Console.
- `KEYCLOAK_CLIENT_ID`
  - In Keycloak: `Clients` -> your client -> `Client ID`.
- `KEYCLOAK_CLIENT_SECRET`
  - In Keycloak: `Clients` -> your client -> `Credentials`.
- `CORS_ALLOWED_ORIGINS`
  - Allowed browser origins that can call Superset APIs.
  - Include Superset public host and toolbox host.

## 3) Keycloak client requirements

In Keycloak client `superset`:

- Client type: confidential (OIDC)
- Valid redirect URIs:
  - `http://localhost:17789/oauth-authorized/keycloak`
  - and/or your public HTTPS URL equivalent
- Web origins:
  - explicit domains (avoid `*` outside local tests)
- Role claims in token:
  - include `superset_admin`, `superset_alpha`, `superset_gamma`

## 4) Start staging instance

Prerequisite: base stack up (must provide `db_v6` and `redis_v6` on network).

```powershell
Set-Location "C:\Progetti\URBreath\Superset"
docker compose --env-file ".env.v6" -f "docker-compose-v6.yml" up -d db_v6 redis_v6
```

Run Keycloak staging Superset:

```powershell
Set-Location "C:\Progetti\URBreath\Superset"
docker compose --env-file ".env.v6.keycloak" -f "docker-compose-v6-keycloak.yml" up -d --build
docker compose --env-file ".env.v6.keycloak" -f "docker-compose-v6-keycloak.yml" logs -f superset_keycloak_v6
```

## 5) Validation checklist

Open `http://localhost:17789` and verify:

1. `Login with keycloak` button is available.
2. Redirect to Keycloak works.
3. After login, user is created in Superset.
4. Role assignment matches token roles (`Admin/Alpha/Gamma`).
5. Existing embedded app on `http://localhost:3001` still works as before.

## 6) Stop staging

```powershell
Set-Location "C:\Progetti\URBreath\Superset"
docker compose --env-file ".env.v6.keycloak" -f "docker-compose-v6-keycloak.yml" down
```


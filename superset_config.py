import os
import json
import base64

from flask_appbuilder.security.manager import AUTH_DB, AUTH_OAUTH
from superset.security import SupersetSecurityManager


def _env_bool(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


SECRET_KEY = os.getenv("SUPERSET_SECRET_KEY", "CHANGE_ME_SUPERSET_SECRET_KEY")
SUPERSET_ENV = os.getenv("SUPERSET_ENV", "production")
SUPERSET_LOAD_EXAMPLES = os.getenv("SUPERSET_LOAD_EXAMPLES", "no")
APP_NAME = os.getenv("SUPERSET_APP_NAME", "URBreath")

SQLALCHEMY_DATABASE_URI = os.getenv(
    "SQLALCHEMY_DATABASE_URI",
    "postgresql+psycopg2://superset:superset@db_v6:5432/superset",
)
MAPBOX_API_KEY = os.getenv("MAPBOX_API_KEY", "")
PYTHONPATH = "/app/pythonpath"

# Keycloak-first mode: if env loading fails in runtime, keep OAuth enabled by default.
ENABLE_KEYCLOAK_OAUTH = _env_bool("ENABLE_KEYCLOAK_OAUTH", True)

FEATURE_FLAGS = {
    "ALERT_REPORTS": True,
    "EMBEDDED_SUPERSET": True,
}

TALISMAN_ENABLED = _env_bool("TALISMAN_ENABLED", False)
ENABLE_CORS = _env_bool("ENABLE_CORS", True)
ENABLE_PROXY_FIX = _env_bool("ENABLE_PROXY_FIX", True)
WTF_CSRF_ENABLED = _env_bool("WTF_CSRF_ENABLED", False)

# Dev-friendly defaults; restrict in production with explicit origins.
HTTP_HEADERS = {"X-Frame-Options": os.getenv("X_FRAME_OPTIONS", "ALLOWALL")}
CORS_OPTIONS = {
    "supports_credentials": True,
    "allow_headers": ["*"],
    "expose_headers": ["*"],
    "resources": ["/*"],
    "origins": [x.strip() for x in os.getenv("CORS_ALLOWED_ORIGINS", "*").split(",") if x.strip()],
}

SUPERSET_WEBSERVER_TIMEOUT = int(os.getenv("SUPERSET_WEBSERVER_TIMEOUT", "300"))
SQLLAB_ASYNC_TIME_LIMIT_SEC = int(os.getenv("SQLLAB_ASYNC_TIME_LIMIT_SEC", "300"))
GUEST_ROLE_NAME = os.getenv("GUEST_ROLE_NAME", "Gamma")


class KeycloakSecurityManager(SupersetSecurityManager):

    @staticmethod
    def _decode_jwt_payload(token):
        if not token or token.count(".") < 2:
            return {}

        try:
            payload_part = token.split(".")[1]
            padding = "=" * (-len(payload_part) % 4)
            decoded = base64.urlsafe_b64decode(payload_part + padding)
            return json.loads(decoded.decode("utf-8"))
        except Exception:  # noqa: BLE001
            return {}

    def oauth_user_info(self, provider, response=None):
        if provider != "keycloak":
            return {}

        me = self.appbuilder.sm.oauth_remotes[provider].get("userinfo").json()
        role_keys = []

        realm_access = me.get("realm_access") or {}
        if isinstance(realm_access, dict):
            role_keys.extend(realm_access.get("roles") or [])

        resource_access = me.get("resource_access") or {}
        if isinstance(resource_access, dict):
            client_roles = resource_access.get(os.getenv("KEYCLOAK_CLIENT_ID", "superset"), {})
            if isinstance(client_roles, dict):
                role_keys.extend(client_roles.get("roles") or [])

        # Keycloak userinfo may not include role claims. Fallback to access token payload.
        if not role_keys and isinstance(response, dict):
            token_claims = self._decode_jwt_payload(response.get("access_token"))

            token_realm_access = token_claims.get("realm_access") or {}
            if isinstance(token_realm_access, dict):
                role_keys.extend(token_realm_access.get("roles") or [])

            token_resource_access = token_claims.get("resource_access") or {}
            if isinstance(token_resource_access, dict):
                token_client_roles = token_resource_access.get(os.getenv("KEYCLOAK_CLIENT_ID", "superset"), {})
                if isinstance(token_client_roles, dict):
                    role_keys.extend(token_client_roles.get("roles") or [])

        username = me.get("email") or me.get("preferred_username") or me.get("sub") or ""

        return {
            "username": username,
            "email": me.get("email", ""),
            "first_name": me.get("given_name", ""),
            "last_name": me.get("family_name", ""),
            "role_keys": list(dict.fromkeys(role_keys)),
        }


AUTH_USER_REGISTRATION = True
AUTH_USER_REGISTRATION_ROLE = os.getenv("AUTH_USER_REGISTRATION_ROLE", "Gamma")
AUTH_ROLES_SYNC_AT_LOGIN = _env_bool("AUTH_ROLES_SYNC_AT_LOGIN", True)
AUTH_ROLES_MAPPING = {
    "superset_admin": ["Admin"],
    "superset_alpha": ["Alpha"],
    "superset_gamma": ["Gamma"],
}

if ENABLE_KEYCLOAK_OAUTH:
    AUTH_TYPE = AUTH_OAUTH
    CUSTOM_SECURITY_MANAGER = KeycloakSecurityManager

    keycloak_host = os.getenv("KEYCLOAK_BASE_URL", "https://keycloak.example.org")
    keycloak_realm = os.getenv("KEYCLOAK_REALM", "urbreath")
    keycloak_client_id = os.getenv("KEYCLOAK_CLIENT_ID", "superset")
    keycloak_client_secret = os.getenv("KEYCLOAK_CLIENT_SECRET", "CHANGE_ME_KEYCLOAK_CLIENT_SECRET")
    keycloak_realm_base = f"{keycloak_host}/realms/{keycloak_realm}"
    keycloak_oidc_base = f"{keycloak_realm_base}/protocol/openid-connect"
    keycloak_metadata_url = f"{keycloak_realm_base}/.well-known/openid-configuration"
    keycloak_jwks_uri = os.getenv("KEYCLOAK_JWKS_URI", f"{keycloak_oidc_base}/certs")

    OAUTH_PROVIDERS = [
        {
            "name": "keycloak",
            "token_key": "access_token",
            "icon": "fa-key",
            "remote_app": {
                "client_id": keycloak_client_id,
                "client_secret": keycloak_client_secret,
                "client_kwargs": {"scope": "openid profile email"},
                "server_metadata_url": keycloak_metadata_url,
                "jwks_uri": keycloak_jwks_uri,
                "access_token_url": f"{keycloak_oidc_base}/token",
                "authorize_url": f"{keycloak_oidc_base}/auth",
                "api_base_url": f"{keycloak_oidc_base}/",
                "userinfo_endpoint": f"{keycloak_oidc_base}/userinfo",
            },
        }
    ]
else:
    AUTH_TYPE = AUTH_DB
    # Current behavior kept for embedded flow while OAuth rollout is staged.
    AUTH_ROLE_PUBLIC = os.getenv("AUTH_ROLE_PUBLIC", "Public")

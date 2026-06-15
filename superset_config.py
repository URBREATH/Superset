# superset_config.py – compatibile con Apache Superset 6.x
# NOTA: rimosso "from flask_talisman import Talisman" e la chiamata Talisman()
# In Superset 6.x Talisman è gestito internamente tramite la variabile TALISMAN_ENABLED

SECRET_KEY = "eRrH2s+nvWP1oiiViUELBCuXsUHr0TRf4VlRolsDkzss5qRviF2n08jY"
SUPERSET_ENV = "production"
SUPERSET_LOAD_EXAMPLES = "no"
APP_NAME = "URBreath"

SQLALCHEMY_DATABASE_URI = "postgresql+psycopg2://superset:superset@db_v6:5432/superset"
MAPBOX_API_KEY = "pk.eyJ1IjoiZXNwbzMiLCJhIjoiY21iN3V6eXdwMDAyNDJscXQ5cnR2MjZ0ayJ9.kMZ3pI6upOC4NEuC5H-e0g"
PYTHONPATH = "/app/pythonpath"

# Ruolo pubblico per embedding (solo dev/test)
AUTH_ROLE_PUBLIC = "Admin"

# CSRF disabilitato per le chiamate embedded
WTF_CSRF_ENABLED = False

# Feature flags
FEATURE_FLAGS = {
    "ALERT_REPORTS": True,
    "EMBEDDED_SUPERSET": True,
}

# Talisman: in Superset 6.x si disabilita tramite questa variabile
# NON usare Talisman() direttamente – causerebbe un ImportError/RuntimeError all'avvio
TALISMAN_ENABLED = False

# Permette embedding in iframe da qualsiasi origine
HTTP_HEADERS = {"X-Frame-Options": "ALLOWALL"}

# CORS
ENABLE_CORS = True
ENABLE_PROXY_FIX = True
CORS_OPTIONS = {
    "supports_credentials": True,
    "allow_headers": ["*"],
    "expose_headers": ["*"],
    "resources": ["/*"],
    "origins": ["*"],
}

SUPERSET_WEBSERVER_TIMEOUT = 300
SQLLAB_ASYNC_TIME_LIMIT_SEC = 300

GUEST_ROLE_NAME = "Gamma"
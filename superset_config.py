from flask_talisman import Talisman

SECRET_KEY = "eRrH2s+nvWP1oiiViUELBCuXsUHr0TRf4VlRolsDkzss5qRviF2n08jY"
SUPERSET_ENV="production"
SUPERSET_LOAD_EXAMPLES="no"
SUPERSET_ADMIN_USERNAME="admin"
SUPERSET_ADMIN_EMAIL="admin@superset.local"
SUPERSET_ADMIN_PASSWORD="admin"
SUPERSET_ADMIN_FIRSTNAME="Admin"
SUPERSET_ADMIN_LASTNAME="User"
SQLALCHEMY_DATABASE_URI="postgresql+psycopg2://superset:superset@db:5432/superset"
MAPBOX_API_KEY="pk.eyJ1IjoiZXNwbzMiLCJhIjoiY21iN3V6eXdwMDAyNDJscXQ5cnR2MjZ0ayJ9.kMZ3pI6upOC4NEuC5H-e0g"
PYTHONPATH="/app/pythonpath"
APP_NAME = "URBreath"

AUTH_ROLE_PUBLIC = "Admin"  # Tutti possono fare tutto

# Disattiva CSRF per le embed request
WTF_CSRF_ENABLED = False

FEATURE_FLAGS = {"ALERT_REPORTS": True,"EMBEDDED_SUPERSET": True}

# Abilita CORS se frontend e superset sono su domini diversi
TALISMAN_ENABLED = False
HTTP_HEADERS={"X-Frame-Options":"ALLOWALL"}
ENABLE_CORS = True
ENABLE_PROXY_FIX = True
CORS_OPTIONS = {
    "supports_credentials": True,
    "allow_headers": ["*"],
    "expose_headers": ["*"],
    "resources": ["/*"],
    "origins": ["*"],  
}

# Disabilita X-Frame-Options (solo dev / embedding)
Talisman(
    content_security_policy=None,
    frame_options=None
)

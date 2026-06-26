FROM apache/superset:6.1.0
USER root

# Superset 6.x usa uv come package manager.
# Il venv è in /app/.venv e non ha pip installato.
# Usare uv pip install con --python per targetare il venv corretto.
RUN uv pip install --python /app/.venv/bin/python --no-cache psycopg2-binary authlib

# Rendiamo l'immagine autonoma per i deploy prod/Portainer:
# la config OAuth e il logo non dipendono più da bind mount locali.
COPY superset_config.py /app/pythonpath/superset_config.py
COPY assets/superset-logo-horiz.png /app/superset/static/assets/images/superset-logo-horiz.png

USER superset

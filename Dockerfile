FROM apache/superset:6.1.0
USER root

# Superset 6.x usa uv come package manager.
# Il venv è in /app/.venv e non ha pip installato.
# Usare uv pip install con --python per targetare il venv corretto.
RUN uv pip install --python /app/.venv/bin/python --no-cache psycopg2-binary

USER superset

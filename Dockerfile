FROM apache/superset:4.1.2
USER root
# Aggiorna pip
RUN pip install --upgrade pip

# Installa pacchetti aggiuntivi necessari
RUN pip install --no-cache-dir flask-cors



RUN pip install psycopg2-binary
USER superset




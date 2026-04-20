# WMS Batch Flow

Questo modulo ora copre tutto il flusso WMS:

1. `WmsUrlService` genera le URL per city/call-type/measure.
2. `WmsHttpClientService` esegue la GET HTTP e mappa il JSON in DTO (`eu.urbreathdsjobs.client.wms.dto`).
3. `WmsResponseHandlerService` riceve la risposta mappata e contiene il punto di estensione per la logica business.
4. `WmsImportTasklet` cicla tutte le URL, richiama HTTP e invoca l'handler.
5. `wmsImportJob` (configurato in `WmsImportJobConfig`) esegue il tasklet.
6. `JobWSMScheduler` schedula il job con protezione anti-overlap (lock + check JobExplorer).

## Cron scheduler

Di default:

- `app.scheduler.wms.cron=0/30 * * * * *`

Puoi sovrascriverlo in `application.yaml`.

## Classi principali

- `eu.urbreathdsjobs.client.wms.WmsUrlService`
- `eu.urbreathdsjobs.client.wms.WmsHttpClientService`
- `eu.urbreathdsjobs.client.wms.WmsResponseHandlerService`
- `eu.urbreathdsjobs.job.WmsImportTasklet`
- `eu.urbreathdsjobs.config.WmsImportJobConfig`
- `eu.urbreathdsjobs.job.JobWSMScheduler`

## Test esistenti

- `WmsUrlServiceTest`
- `WmsHttpClientServiceTest`
- `WmsImportTaskletTest`


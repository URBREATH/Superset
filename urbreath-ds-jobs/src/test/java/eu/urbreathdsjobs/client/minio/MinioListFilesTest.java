package eu.urbreathdsjobs.client.minio;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

public class MinioListFilesTest {

    private final String ENDPOINT = "https://minio-api-dev.urbreath.tech/";

    private final String BUCKET = "urbreath-public-repo";

    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(System.getenv().get("ACCESS_KEY"), System.getenv().get("SECRET_KEY"))
                        )
                )
                .forcePathStyle(true) 
                .build();
    }

    /**
     * Test 1 — Verifica connessione bucket
     */
    @Test
    void testListFiles() {

        S3Client client = buildClient();

        listAllFiles(client, BUCKET, "Leuven/Mobility/KPIs").stream().filter(x -> x.contains("raw_data")).forEach(System.out::println);
    }
    
    
    void testGenerateProjectionInsertBatchTask() {
    	// TODO
    	/*
INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod, note) 
VALUES( 1, '{"items": [{"key": "bucket", "value": "urbreath-public-repo", "type": "STRING"}, {"key": "MEASURE_TYPE", "value": "PROJECTION", "type": "STRING"}, {"key": "PARAM_ID", "value": "9996", "type": "NUMBER"}, {"key": "PARAM_ID", "value": "9996", "type": "NUMBER"},{"key": "SENSOR_ID", "value": "1", "type": "NUMBER"},{"key": "SOURCE", "value": "ssp126", "type": "MPI-ESM1-2-HR"},
{"key": "objectKey", "value": "Cluj-Napoca/Climate Information/Projections/Temperature_corrected/02_MPI-ESM1-2-HR/ssp126/Temperature-corrected_MPI-ESM1-2-HR_ssp126_15120099999.txt", "type": "STRING"}]}'::json
, 0, now(), null, NULL);
    	
    	*/
    }


    
    
    public List<String> listAllFiles(S3Client s3Client, String bucket, String prefix) {
        List<String> filePaths = new ArrayList<>();
        
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)  // es. "cartella/sottocartella/"
                .build();
        
        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            
            response.contents().forEach(s3Object -> {
                filePaths.add(s3Object.key());
            });
            
            // Gestisce la paginazione (S3 restituisce max 1000 oggetti per volta)
            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
                    
        } while (response.isTruncated());
        
        return filePaths;
    }
}

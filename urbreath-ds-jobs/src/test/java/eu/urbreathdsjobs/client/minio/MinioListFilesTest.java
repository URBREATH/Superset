package eu.urbreathdsjobs.client.minio;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import eu.urbreathdsjobs.launcher.App;

@SpringBootTest(classes = App.class)
public class MinioListFilesTest {

    @Value("${minio.endpoint}")
    private String ENDPOINT;

    @Value("${minio.access-key}")
    private String ACCESS_KEY;

    @Value("${minio.secret-key}")
    private String SECRET_KEY;

    private final String BUCKET = "urbreath-public-repo";

    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
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

        //listAllFiles(client, BUCKET, "Leuven/Climate Information/Projections/Temperature_corrected").stream().forEach(System.out::println);
        //listAllFiles(client, BUCKET, "Madrid/Climate Information/Projections/Precipitation_corrected").stream().forEach(System.out::println);
        //listAllFiles(client, BUCKET, "Madrid/Climate Information/Projections/Temperature_corrected").stream().forEach(System.out::println);
        //listAllFiles(client, BUCKET, "Tallinn/Climate Information/Projections/Precipitation_corrected").stream().forEach(System.out::println);
        listAllFiles(client, BUCKET, "Tallinn/Climate Information/Projections/Temperature_corrected").stream().forEach(System.out::println);






//       MPI-ESM1-2-HR
//    
//       INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod, note) 
//       VALUES( 2, '{"items": [
//    		   {"key": "bucket", "value": "urbreath-public-repo", "type": "STRING"}, 
//    		   {"key": "MEASURE_TYPE", "value": "PROJECTION", "type": "STRING"}, 
//    		   {"key": "PARAM_ID", "value": "9996", "type": "NUMBER"},
//    		   {"key": "SENSOR_ID", "value": "9900020", "type": "NUMBER"},
//    		   {"key": "SCENARIO", "value": "ssp126", "type": "STRING"},
//    		   {"key": "SOURCE", "value": "MPI-ESM1-2-HR", "type": "STRING"},
//               {"key": "objectKey", "value": "Leuven/Climate Information/Projections/Temperature_corrected/02_MPI-ESM1-2-HR/ssp126/Temperature-corrected_MPI-ESM1-2-HR_ssp126_64510.txt", "type": "STRING"}
//    		   ]}'::json
//       , 0, now(), null, NULL);
//      
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

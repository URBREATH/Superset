package eu.urbreathdsjobs.client.minio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

public class MinioListFilesTest {

    private final String ENDPOINT = "https://minio-api-dev.urbreath.tech/";
    private final String ACCESS_KEY = "Municipia";
    private final String SECRET_KEY = "zS3fx4zeECPuc4K";

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
                .forcePathStyle(true) // fondamentale per MinIO
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

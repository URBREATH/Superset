package eu.urbreathdsjobs.client.minio;



import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class MinioClientTest {

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
    void testBucketExists() {

        S3Client client = buildClient();

        ListBucketsResponse response = client.listBuckets();

        boolean exists = response.buckets()
                .stream()
                .anyMatch(b -> b.name().equals(BUCKET));

        assertTrue(exists, "Bucket non trovato!");
    }

    /**
     * Test 2 — Lista cartelle KPI
     */
    @Test
    void testListKpiFolders() {

        S3Client client = buildClient();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET)
                .prefix("Leuven/Mobility/KPIs/")
                .delimiter("/") // simula cartelle
                .build();

        ListObjectsV2Response response = client.listObjectsV2(request);

        assertFalse(response.commonPrefixes().isEmpty(),
                "Nessuna folder trovata");

        response.commonPrefixes()
                .forEach(p -> System.out.println("Folder: " + p.prefix()));
    }

    /**
     * Test 3 — Download file CSV
     */
//    @Test
    void testDownloadCsv() throws Exception {

        S3Client client = buildClient();

        String key =
            "Leuven/Mobility/KPIs/2025-05-01_05-00-00/data.csv";

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> stream =
                client.getObject(request);

        assertNotNull(stream, "Stream nullo!");

        System.out.println("Download OK");
    }

    /**
     * Test 4 — Legge prime righe CSV
     */
    @Test
    void testReadCsvContent() throws Exception {

        S3Client client = buildClient();

        String key =
            "Leuven/Mobility/KPIs/2025-05-01_05-00-00/raw_data/9000008590.csv";

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> stream =
                     client.getObject(request);
             BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(stream))) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < 5) {
                System.out.println(line);
                count++;
            }

            assertTrue(count > 0, "File vuoto!");
        }
    }
}

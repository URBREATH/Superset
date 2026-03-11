package eu.urbreathdsjobs.client.minio;

import java.io.InputStream;

import org.springframework.core.io.AbstractResource;

import lombok.Data;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Data
public class MinioClientResource extends AbstractResource {

    private final S3Client s3Client;
    private final String bucket;
    private final String objectKey;

    @Override
    public String getDescription() {
        return "MinIO resource: " + bucket + "/" + objectKey;
    }

    @Override
    public InputStream getInputStream() {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        return s3Client.getObject(request);
    }
}
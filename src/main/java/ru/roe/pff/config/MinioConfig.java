package ru.roe.pff.config;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.username}")
    private String minioUsername;
    @Value("${minio.password}")
    private String minioPassword;
    @Value("${minio.bucket-name}")
    private String minioBucketName;

    @Bean
    public MinioClient minioClient() {
        return new MinioClient.Builder()
                .httpClient(new OkHttpClient())
                .credentials(minioUsername, minioPassword)
                .endpoint(minioUrl)
                .build();
    }

    @EventListener
    public void createBuckets(final ApplicationReadyEvent event) throws Exception {
        log.info("Creating MinIO buckets...");
        final var args = MakeBucketArgs
                .builder()
                .bucket(minioBucketName)
                .build();
        try (final var client = minioClient()) {
            client.makeBucket(args);
            log.info("Buckets created");
        } catch (final Exception e) {
            if (e.getMessage().equalsIgnoreCase(BUCKET_EXISTS_MSG)) {
                log.info("Bucket already exists. Skipping...");
            } else {
                throw e;
            }
        }
    }

    private static final String BUCKET_EXISTS_MSG = "Your previous request to create the named bucket succeeded and you already own it.";

}

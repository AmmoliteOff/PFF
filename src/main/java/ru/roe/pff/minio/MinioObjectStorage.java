package ru.roe.pff.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.exception.ApiException;

import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioObjectStorage {

    private final MinioClient minioClient;

    public static final String DEFAULT_BUCKET_NAME = "pff-bucket";

    public boolean exists(String bucket, String objName) {
        try {
            final var soa = StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objName)
                    .build();
            minioClient.statObject(soa);
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error("Error while checking for file existence: ", e);
            return false;
        }
    }

    public void upload(String bucket, String objName, FileInputStream fis) {
        try {
            final var poa = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objName)
                    .stream(fis, fis.getChannel().size(), -1)
                    .build();
            minioClient.putObject(poa);
            log.debug("File uploaded to Minio: {}", poa);
        } catch (Exception e) {
            log.error("Error while uploading the file: ", e);
            throw new ApiException("Error while uploading the file.");
        }
    }

    public void upload(String bucket, String objName, MultipartFile mf) {
        try {
            final var resource = mf.getResource();
            final var poa = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objName)
                    .stream(resource.getInputStream(), resource.contentLength(), -1)
                    .contentType(mf.getContentType())
                    .build();
            minioClient.putObject(poa);
            log.debug("File uploaded to Minio: {}", poa);
        } catch (Exception e) {
            log.error("Error while uploading the file: ", e);
            throw new ApiException("Error while uploading the file.");
        }
    }

    public void delete(String bucket, String objName) {
        try {
            if (!exists(bucket, objName)) {
                return;
            }

            final var roa = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objName)
                    .build();
            minioClient.removeObject(roa);
        } catch (Exception e) {
            log.error("Error while deleting the file: ", e);
            throw new ApiException("Error while deleting the file.");
        }
    }

    public InputStream get(String bucket, String objName) {
        final var goa = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objName)
                .build();

        try {
            return minioClient.getObject(goa);
        } catch (Exception e) {
            throw new ApiException("Error while getting the file.");
        }
    }


}

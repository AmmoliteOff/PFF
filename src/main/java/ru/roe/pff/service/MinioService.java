package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.minio.MinioObjectStorage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import static ru.roe.pff.minio.MinioObjectStorage.DEFAULT_BUCKET_NAME;

@Service
@RequiredArgsConstructor
public class MinioService { //TODO

    private final MinioObjectStorage objectStorage;

    public InputStream getFile(String fileName) {
        return objectStorage.get(DEFAULT_BUCKET_NAME, fileName);
    }

    public void uploadFile(String fileName, ByteArrayInputStream bais, long size) {
        objectStorage.upload(DEFAULT_BUCKET_NAME, fileName, bais, size);
    }

    public void uploadFile(String fileName, MultipartFile file) {
        objectStorage.upload(DEFAULT_BUCKET_NAME, fileName, file);
    }

    public void uploadFile(String fileName, FileInputStream fis) {
        objectStorage.upload(DEFAULT_BUCKET_NAME, fileName, fis);
    }


}

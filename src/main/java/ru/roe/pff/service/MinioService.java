package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.roe.pff.minio.MinioObjectStorage;

import java.io.InputStream;

import static ru.roe.pff.minio.MinioObjectStorage.DEFAULT_BUCKET_NAME;

@Service
@RequiredArgsConstructor
public class MinioService { //TODO

    private final MinioObjectStorage objectStorage;

    public InputStream getFile(String fileName) {
        return objectStorage.get(DEFAULT_BUCKET_NAME, fileName);
    }
    

}

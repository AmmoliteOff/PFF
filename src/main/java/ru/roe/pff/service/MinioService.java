package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService { //TODO
    public InputStream getFile(String fileName) { //TODO заменить на реальную интеграцию с Minio
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }
}

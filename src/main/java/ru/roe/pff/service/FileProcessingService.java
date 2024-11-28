package ru.roe.pff.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.roe.pff.entity.FileRequest;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.files.csv.CsvParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;
import ru.roe.pff.repository.FileRequestRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final FileErrorRepository fileErrorRepository;
    private final MinioService minioService;
    private final FileRequestRepository fileRequestRepository;
    private final FileRepository fileRepository;
    private final ExecutorService executorService;

    private final Queue<FileRequest> queue = new LinkedList<>();

    @PostConstruct
    public void init() {
//        var file = fileRepository.save(new FeedFile(null, "test.csv"));
//        var fileRequest = new FileRequest(null, file, null);
//        fileRequestRepository.save(fileRequest);
//        try {
//            processFile(fileRequest);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Scheduled(fixedRate = 1000)
    public void processFiles() {
        if (!queue.isEmpty()) {
            FileRequest fileRequest = queue.poll();
            executorService.submit(() -> {
                try {
                    processFile(fileRequest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void addFileToQueue(FileRequest file) {
        queue.add(file);
    }

    private void processFile(FileRequest fileRequest) throws IOException {
        // Получение файла из MinIO
        InputStream fileStream = minioService.getFile(fileRequest.getFile().getFileName());
        // Определение типа файла
        String fileType = getFileExtension(fileRequest.getFile().getFileName());
        // Получение парсера по типу файла
        FileParser parser = getParser(fileType);

        DataRowValidator validator = new DataRowValidator(fileRequest, fileErrorRepository);
        List<DataRow> dataRows = parser.parse(validator, fileStream);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private FileParser getParser(String fileType) {
        return switch (fileType) {
            case "csv" -> new CsvParser();
            //case "json" -> new JsonParser();
            //case "xml" -> new XmlParser();
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}


//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import ru.roe.pff.entity.FeedFile;
//import ru.roe.pff.entity.FileError;
//import ru.roe.pff.entity.FileRequest;
//import ru.roe.pff.enums.ErrorType;
//import ru.roe.pff.files.csv.CsvColumnTypeDetector;
//import ru.roe.pff.processing.DataRow;
//import ru.roe.pff.repository.FileErrorRepository;
//import ru.roe.pff.repository.FileRepository;
//import ru.roe.pff.repository.FileRequestRepository;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeParseException;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class FileProcessingService { //TODO добавить и проверить поддержку других типов, кроме CSV (желательно
//    // приводить к виду CSV)
//    private final FileErrorRepository fileErrorRepository;
//    private final MinioService minioService;
//    private final FileRequestRepository fileRequestRepository;
//    private final FileRepository fileRepository;
//    private final List<String> requriedFields = new ArrayList<>() {{
//        add("price");
//        add("sku");
//        add("title");
//    }};
//
//    private final Queue<FileRequest> queue = new LinkedList<>();
//    private boolean isFileProcessing = false;
//    private int internalIndex = 0;
//    private List<Class<?>> types = new ArrayList<>();
//    private List<String> titles = new ArrayList<>();
//    private int skuIndex = 0;
//
//    @PostConstruct// Только для тестов
//    public void init() {
//        var file = fileRepository.save(new FeedFile(null, "test.csv"));
//        var fileRequest = new FileRequest(null, file, null);
//        fileRequestRepository.save(fileRequest);
//        try {
//            processFile(fileRequest);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Scheduled(fixedRate = 1000)
//    public void processFiles() {
//        if (!isFileProcessing && !queue.isEmpty()) {
//            FileRequest file = queue.remove();
//
//            try {
//                processFile(file);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//    }
//
//    public void addFileToQueue(FileRequest file) {
//        queue.add(file);
//    }
//
//    /**
//     * Считывает файл из MinIO, парсит и сразу преобразует в Map<Row, Integer>.
//     *
//     * @return мапа Row -> индекс
//     * @throws Exception если встречается ошибка при чтении/парсинге
//     */
//    private static final int BATCH_SIZE = 1000;
//
//    // Метод для обработки файла
//    public void processFile(FileRequest fileRequest) throws IOException {
//        isFileProcessing = true;
//        Set<String> seenSkus = new HashSet<>();
//        InputStream fileStream = minioService.getFile(fileRequest.getFile().getFileName());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
//        String line;
//        List<DataRow> batch = new ArrayList<>(BATCH_SIZE);
//
//        while ((line = reader.readLine()) != null) {
//            DataRow dataRow = parseRow(line, getFileExtension(fileRequest.getFile().getFileName()));
//            batch.add(dataRow);
//
//            if (batch.size() >= BATCH_SIZE) {
//                processBatch(batch, seenSkus, fileRequest);
//                batch.clear();  // Очистить текущий пакет
//            }
//        }
//        // Обработка оставшихся строк в последнем пакете
//        if (!batch.isEmpty()) {
//            processBatch(batch, seenSkus, fileRequest);
//        }
//        isFileProcessing = false;
//        internalIndex = 0;
//    }
//
//    private String getFileExtension(String fileName) {
//        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
//    }
//
//    // Обработка данных в пакете
//    private void processBatch(List<DataRow> batch, Set<String> seenSkus, FileRequest fileRequest) {
//        batch.parallelStream().forEach(dataRow -> {
//            validateRow(dataRow, seenSkus, fileRequest);
//        });
//    }
//
//    // Основные проверки строки
//    private void validateRow(DataRow dataRow, Set<String> seenSkus, FileRequest fileRequest) {
//        if (dataRow.getIndex() != 0) {
//            var sku = dataRow.get(skuIndex, Double.class);
//            if (sku != null) {
//                if (!seenSkus.add(sku.toString())) {
//                    saveError(fileRequest, "Duplicate SKU", ErrorType.LOGICAL, dataRow.getIndex(), skuIndex);
//                }
//            }
//
//            for (int i = 0; i < types.size(); i++) {
//                var value = dataRow.get(i, types.get(i));
//                if (value == null || value == " " || value == "") {
//                    if (requriedFields.contains(titles.get(i).toLowerCase())) {
//                        saveError(fileRequest, "Required field is missing", ErrorType.TECHNICAL, dataRow.getIndex()
//                        , i);
//                    } else if (titles.get(i).equalsIgnoreCase("category")) {
//                        saveError(fileRequest, "Category field is empty", ErrorType.LOGICAL, dataRow.getIndex(), i);
//                    }
//                } else if (types.get(i) == Double.class && titles.get(i).equalsIgnoreCase("price")) {
//                    validatePrice();
//                } //TODO больше проверок
//            }
//        }
//    }
//
//    private void validatePrice() {
//        //TODO
//    }
//
//    // Метод для создания ошибки и сохранения в репозитории
//    private void saveError(
//        FileRequest fileRequest,
//        String error,
//        ErrorType errorType,
//        Integer index,
//        Integer columnIndex) {
//        FileError fileError = new FileError();
//        fileError.setFileRequest(fileRequest);
//        fileError.setError(error);
//        fileError.setColumnIndex(columnIndex);
//        fileError.setErrorType(errorType);
//        fileError.setRowIndex(index);  // Устанавливаем индекс строки с ошибкой
//        fileErrorRepository.save(fileError);
//    }
//
//
//    private DataRow parseRow(String line, String fileType) {
//        DataRow dataRow = new DataRow();
//        if ("csv".equals(fileType)) {
//            // Разбираем строку как CSV
//            if (line.endsWith(",")) {
//                line += " ,";
//            } else if (line.startsWith(",")) {
//                line = " " + line;
//            }
//            String[] data = line.split(",");
//
//            if (internalIndex == 0) {
//                titles = Arrays.stream(data).map(String::toLowerCase).toList();
//                skuIndex = titles.indexOf("sku");
//            }
//
//            if (internalIndex <= 1) {
//                types = CsvColumnTypeDetector.getColumnTypes(Arrays.asList(data));
//            }
//
//            dataRow.setElements(cast(types, Arrays.asList(data)), types);
//            dataRow.setIndex(internalIndex++);
//
//
//        } else if ("json".equals(fileType)) {
//            // Разбираем строку как JSON с помощью Jackson
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, Object> map = objectMapper.readValue(line, Map.class);
//                dataRow.setElements(new ArrayList<>(map.values()));
//            } catch (Exception e) {
//                throw new RuntimeException("Error parsing JSON line", e);
//            }
//        } else if ("xml".equals(fileType)) {
//            // Разбираем строку как XML (обработка XML через SAX)
//            try {
//                dataRow.setElements(parseXML(line));
//            } catch (Exception e) {
//                throw new RuntimeException("Error parsing XML line", e);
//            }
//        }
//        return dataRow;
//    }
//
//    // Метод для парсинга XML
//    private List<Object> parseXML(String xmlLine) throws SAXException, IOException {
//        List<Object> elements = new ArrayList<>();
//        // Пример простого парсинга XML
//        try {
//            InputSource inputSource = new InputSource(new StringReader(xmlLine));
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document doc = builder.parse(inputSource);
//            NodeList nodes = doc.getDocumentElement().getChildNodes();
//            for (int i = 0; i < nodes.getLength(); i++) {
//                elements.add(nodes.item(i).getTextContent());
//            }
//        } catch (ParserConfigurationException e) {
//            throw new RuntimeException("Error parsing XML", e);
//        }
//        return elements;
//    }
//
//    private List<Object> cast(List<Class<?>> types, List<Object> elements) {
//        var result = new ArrayList<>();
//        for (int i = 0; i < types.size(); i++) {
//            var expectedType = types.get(i);
//            var element = elements.get(i);
//            if (expectedType == String.class) {
//                result.add(expectedType.cast(element));
//            } else if (expectedType == Double.class && element instanceof String) {
//                try {
//                    result.add(Double.parseDouble((String) element));
//                } catch (NumberFormatException e) {
//                    return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
//                }
//            } else if (expectedType == Integer.class && element instanceof String) {
//                try {
//                    result.add(expectedType.cast(Integer.parseInt((String) element)));
//                } catch (NumberFormatException e) {
//                    return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
//                }
//            } else if (expectedType == Boolean.class && element instanceof String) {
//                result.add(expectedType.cast(Boolean.parseBoolean((String) element)));
//            } else if (expectedType == LocalDate.class && element instanceof String) {
//                try {
//                    result.add(expectedType.cast(LocalDate.parse((String) element)));
//                } catch (DateTimeParseException e) {
//                    return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
//                }
//            } else if (expectedType == LocalTime.class && element instanceof String) {
//                try {
//                    result.add(expectedType.cast(LocalTime.parse((String) element)));
//                } catch (DateTimeParseException e) {
//                    return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
//                }
//            } else if (expectedType == LocalDateTime.class && element instanceof String) {
//                try {
//                    result.add(expectedType.cast(LocalDateTime.parse((String) element)));
//                } catch (DateTimeParseException e) {
//                    return null; // TODO техническая ошибка - значение не совпадает с ожидаемым типом
//                }
//            } else {
//                result.add(expectedType.cast(element));
//            }
//        }
//        return result;
//    }
//}

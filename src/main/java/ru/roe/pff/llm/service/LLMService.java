package ru.roe.pff.llm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.roe.pff.llm.client.YandexGptClient;
import ru.roe.pff.llm.dto.PromtItemDto;
import ru.roe.pff.llm.dto.PromtResponseDto;
import ru.roe.pff.llm.utils.Promt;
import ru.roe.pff.processing.DataRow;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LLMService {
    @Value("${application.llm.coverage}")
    private Double coveragePercentage;
    private final Random random = new Random();
    private final YandexGptClient yandexGptClient;
    private final ObjectMapper objectMapper;

    public PromtResponseDto checkForTitleChange(List<DataRow> rows) throws JsonProcessingException {
        var items = rows.parallelStream()
            .filter(row -> random.nextDouble() < coveragePercentage)
            .map(row -> new PromtItemDto(row.getData().get(1), row.getIndex(), 1))
            .toList();
        var promt = Promt.createPrompt(
            0.4,
            2000,
            "Вы должны обработать входной JSON-список, где каждый элемент имеет структуру:\n" +
                "\n" + "public record PromtItemDto (String value, Integer rowIndex, Integer columIndex);\n" +
                "Ваша задача — проверить поле value каждого элемента. Если оно содержит название продукта с ошибкой " +
                "(опечаткой), исправьте его на правильное. Если название корректное или его невозможно однозначно " +
                "исправить, оставьте его без изменений.\n" +
                "\n" + "Верните JSON-список только тех элементов, в которых были исправлены ошибки, сохраняя " +
                "структуру объекта. Например:\n" +
                "\n" + "Входной JSON:\n" +
                "\n" + "json\n" +
                "Копировать код\n" + "[\n" +
                "  {\"value\": \"Мясорубк\", \"rowIndex\": 8, \"columIndex\": 1},\n" +
                "  {\"value\": \"Телевезор\", \"rowIndex\": 3, \"columIndex\": 1},\n" +
                "  {\"value\": \"Яблоко\", \"rowIndex\": 4, \"columIndex\": 1}\n" +
                "]\n" +
                "Ожидаемый ответ:\n" +
                "\n" +
                "json\n" +
                "Копировать код\n" +
                "[\n" +
                "  {\"value\": \"Мясорубка\", \"rowIndex\": 8, \"columIndex\": 1},\n" +
                "  {\"value\": \"Телевизор\", \"rowIndex\": 3, \"columIndex\": 1}\n" +
                "]",
            objectMapper.writeValueAsString(items)
        );
        return yandexGptClient.producePromt(promt);
    }
}

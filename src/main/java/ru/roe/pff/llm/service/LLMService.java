package ru.roe.pff.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.roe.pff.llm.client.YandexGptClient;
import ru.roe.pff.llm.client.YandexIamClient;
import ru.roe.pff.llm.utils.Promt;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.LlmWarnings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {
    @Value("${yandex.yandexPassportOauthToken}")
    private String oAuthToken;
    @Value("${yandex.uri}")
    private String modelUri;
    private final Random random = new Random();
    private final YandexGptClient yandexGptClient;
    private final YandexIamClient yandexIamClient;
    private final ObjectMapper objectMapper;

    public List<LlmWarnings> checkFieldWithAi(List<DataRow> items) {
        log.debug("Retrieving AI suggestions for {} items...", items.size());
        try {
            List<LlmWarnings> warnings = new ArrayList<>();

            List<DataRow> remainingItems = new ArrayList<>(items);
            int requestCount = 0;
            int maxRequests = 5;
            int maxTokenCount = 2000;

            while (!remainingItems.isEmpty() && requestCount < maxRequests) {
                List<DataRow> batch = getBatch(remainingItems, maxTokenCount);
                String batchJson = objectMapper.writeValueAsString(batch);

                var prompt = Promt.createPrompt(
                    modelUri,
                    0.1,
                    maxTokenCount,
                    "Ты - программа-анализатор. Тебе дадут JSON-Объекты, твоя задача определить плохие поля. Под " +
                        "плохим " +
                        "полем подразумевается: цена на товар слишком низкая или высокая, опечатки в " +
                        "названиях или описаниях, орфографические ошибки. Обязательно учти, что если в названии " +
                        "заявлен " +
                        "бренд, то в описаниях и бренде должен быть такой же (upper и lower-case не считается). Длина" +
                        " нового значения не более 255 символов. Если проблема с ценой, обязательно предложи свою. " +
                        "Учти, что каждая ошибка должна быть представлена отдельным объектом, даже если " +
                        "она в одной и той же строке, но разных колонках (полях). Учти, что если все поля всех " +
                        "объектов должны быть одного типа, если какое-то поле не имеет такой же тип, как остальные, " +
                        "то это ошибка. Если таких полей нет, то просто верни " +
                        "[]. Иначе результат представь в формате JSON со следующей структурой: " +
                        "title: <НАЗВАНИЕ ОШИБКИ В 1-2 СЛОВА>\\message: <ОПИСАНИЕ ОШИБКИ>\\nvalue: <НОВОЕ ЗНАЧЕНИЕ " +
                        "ПОЛЯ>\\nrowIndex: <ПОРЯДКОВЫЙ НОМЕР ОБЪЕКТА В " +
                        "JSON>\\ncolumn: <НАЗВАНИЕ ПОЛЯ КОТОРОЕ НУЖНО ИЗМЕНИТЬ (\"id\",\"available\",\"price\"," +
                        "\"currencyId\",\"categoryId\",\"picture\",\"name\",\"vendor\",\"description\",\"barcode\"," +
                        "\"param_Артикул\",\"param_Рейтинг\",\"param_Количество отзывов\",\"param_Скидка\"," +
                        "\"param_Новинка\")>;...].",
                    batchJson
                );

                var token = yandexIamClient.getToken(new YandexIamClient.IamTokenRequest(oAuthToken)).getIamToken();
                var authHeader = String.format("Bearer %s", token);
                var result = yandexGptClient.producePromt(prompt, authHeader);
                var json = result.result().alternatives().get(0).message().text();

                json = json.replaceAll("^```|```$", "");

                Gson gson = new Gson();
                Type listType = new TypeToken<List<LlmWarnings>>() {
                }.getType();
                List<LlmWarnings> batchWarnings = gson.fromJson(json, listType);
                warnings.addAll(batchWarnings);

                remainingItems.subList(0, batch.size()).clear();
                requestCount++;
            }

            log.debug("Retrieved {} AI suggestions", warnings.size());
            return warnings;
        } catch (Exception e) {
            log.error("Error while retrieving AI suggestions", e);
            return List.of();
        }
    }

    private List<DataRow> getBatch(List<DataRow> items, int maxTokenCount) {
        List<DataRow> batch = new ArrayList<>();
        int currentTokenCount = 0;
        for (DataRow item : items) {
            int itemTokenCount = estimateTokenCount(Collections.singletonList(item));
            if (currentTokenCount + itemTokenCount > maxTokenCount) {
                break;
            }
            batch.add(item);
            currentTokenCount += itemTokenCount;
        }
        return batch;
    }

    private int estimateTokenCount(List<DataRow> batch) {
        try {
            String json = objectMapper.writeValueAsString(batch);
            return json.length(); // Using string length as a proxy for token count
        } catch (Exception e) {
            return 0;
        }
    }
}

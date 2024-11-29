package ru.roe.pff.llm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.roe.pff.llm.client.YandexGptClient;
import ru.roe.pff.llm.client.YandexIamClient;
import ru.roe.pff.llm.client.YandexProperties;
import ru.roe.pff.llm.dto.PromtItemDto;
import ru.roe.pff.llm.dto.PromtResponseDto;
import ru.roe.pff.llm.utils.Promt;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.LlmWarnings;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LLMService {
    @Value("${application.llm.coverage}")
    private Double coveragePercentage;
    @Value("${yandex.yandexPassportOauthToken}")
    private String oAuthToken;
    @Value("${yandex.uri}")
    private String modelUri;
    private final Random random = new Random();
    private final YandexGptClient yandexGptClient;
    private final YandexIamClient yandexIamClient;
    private final ObjectMapper objectMapper;

    public List<LlmWarnings> checkForTitleChange(List<DataRow> rows) throws JsonProcessingException {
        var items = rows.parallelStream()
            .filter(row -> random.nextDouble() < coveragePercentage)
            .toList();
        var promt = Promt.createPrompt(modelUri,
            0.4,
            2000,
            "Ты - программа-анализатор. Тебе дадут JSON-Объекты, твоя задача определить плохие поля. Под плохим полем" +
                " подразумевается: цена на товар слишком низкая или высокая, ошибки и логические несостыковки в " +
                "названиях или описаниях, прочие очевидные ошибки. Обязательно учти, что если в названии заявлен " +
                "бренд, то в описаниях и бренде должен быть такой же. Если проблема с ценой, предложи свою на основе " +
                "средней цены за подобные товары. Учти, что каждая ошибка должа быть представлена отдельным объектом, даже если " +
                "она в одной и той же строке, но разных колонках (полях). Если таких полей нет, то просто верни []. " +
                "Иначе результат представь в формате JSON со следующей структурой: message: <ОПИСАНИЕ " +
                "ОШИБКИ>\\nvalue: <НОВОЕ ЗНАЧЕНИЕ ПОЛЯ>\\nrowIndex: <ПОРЯДКОВЫЙ НОМЕР ОБЪЕКТА В JSON>\\ncolumnIndex: " +
                "<НОМЕР ПОЛЯ ПО СЧЁТУ В ОБЪЕКТЕ>;..." +
                "]",
            objectMapper.writeValueAsString(items)
        );

        var token = yandexIamClient.getToken(new YandexIamClient.IamTokenRequest(oAuthToken)).getIamToken();
        var authHeader = String.format("Bearer %s", token);
        var result = yandexGptClient.producePromt(promt, authHeader);
        var json = result.result().alternatives().get(0).message().text();

        json = json.replaceAll("^```|```$", "");

        Gson gson = new Gson();
        Type listType = new TypeToken<List<LlmWarnings>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}

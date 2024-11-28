package ru.roe.pff.llm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.roe.pff.llm.dto.PromtResponseDto;
import ru.roe.pff.llm.utils.Promt;

@FeignClient(name = "yandex-gpt-client", url = "https://iam.api.cloud.yandex.net")
public class YandexGptClient {
    @PostMapping("/iam/v1/tokens")
    public void getToken() {

    }

    @PostMapping("/foundationModels/v1/completion")
    public PromtResponseDto producePromt(@RequestBody Promt promt) {
        return null;
    }
}

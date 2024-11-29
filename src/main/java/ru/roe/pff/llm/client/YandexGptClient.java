package ru.roe.pff.llm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.roe.pff.llm.dto.PromtResponseDto;
import ru.roe.pff.llm.utils.Promt;

@FeignClient(name = "yandex-gpt-client", url = "https://llm.api.cloud.yandex.net")
public interface YandexGptClient {
    @PostMapping("/foundationModels/v1/completion")
    PromtResponseDto producePromt(@RequestBody Promt promt, @RequestHeader(name = "Authorization") String token);
}
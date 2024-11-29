package ru.roe.pff.llm.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "iam-client", url = "https://iam.api.cloud.yandex.net")
public interface YandexIamClient {
    @PostMapping("/iam/v1/tokens")
    IamTokenResponse getToken(@RequestBody IamTokenRequest request);

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class IamTokenRequest {
        private String yandexPassportOauthToken;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class IamTokenResponse {
        private String iamToken;
    }
}


package ru.roe.pff.llm.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "yandex")
public class YandexProperties {
    private String yandexPassportOauthToken;
}
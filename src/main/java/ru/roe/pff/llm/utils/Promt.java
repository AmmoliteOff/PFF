package ru.roe.pff.llm.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class Promt {
    private final CompletionOptions completionOptions;
    private final List<PromtMessage> messages;
    @Value("${spring.llm.uri}")
    private String modelUri;

    private Promt(CompletionOptions completionOptions, List<PromtMessage> messages) {
        this.completionOptions = completionOptions;
        this.messages = messages;
    }

    public static Promt createPrompt(Double temperature, Integer tokens, String systemMessage, String userMessage) {
        var competionOptions = new CompletionOptions(false, temperature, tokens);
        var messages = new ArrayList<PromtMessage>();
        messages.add(new PromtMessage("system", systemMessage));
        messages.add(new PromtMessage("user", userMessage));
        return new Promt(competionOptions, messages);
    }
}

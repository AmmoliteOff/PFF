package ru.roe.pff.llm.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Promt {
    private final CompletionOptions completionOptions;
    private final List<PromtMessage> messages;
    private final String modelUri;

    private Promt(CompletionOptions completionOptions, List<PromtMessage> messages, String modelUri) {
        this.completionOptions = completionOptions;
        this.messages = messages;
        this.modelUri = modelUri;
    }

    public static Promt createPrompt(String modelUri, Double temperature, Integer tokens, String systemMessage, String userMessage) {
        var competionOptions = new CompletionOptions(false, temperature, tokens);
        var messages = new ArrayList<PromtMessage>();
        messages.add(new PromtMessage("system", systemMessage));
        messages.add(new PromtMessage("user", userMessage));
        return new Promt(competionOptions, messages, modelUri);
    }
}

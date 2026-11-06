package pe.edu.utp.ebenezer.ai.provider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.config.AiProperties;
import tools.jackson.databind.json.JsonMapper;

/** Main provider: OpenAI with the store key (OPENAI_API_KEY / OPENAI_MODEL). */
@Component
public class OpenAiProvider implements AiProvider {

    public static final String NAME = "openai";

    private final OpenAiCompatibleClient client;

    public OpenAiProvider(AiProperties aiProperties, RestClient aiRestClient, JsonMapper jsonMapper) {
        this.client = new OpenAiCompatibleClient(
                NAME, aiProperties.openai(), aiProperties, aiRestClient, jsonMapper, Map.of());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean isConfigured() {
        return client.isConfigured();
    }

    @Override
    public AiCompletion complete(AiCompletionRequest request) {
        return client.complete(request);
    }
}

package pe.edu.utp.ebenezer.ai.provider;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.config.AiProperties;
import tools.jackson.databind.json.JsonMapper;

/** Fallback provider: OpenRouter (OPENROUTER_API_KEY / OPENROUTER_MODEL), OpenAI-compatible API. */
@Component
public class OpenRouterProvider implements AiProvider {

    public static final String NAME = "openrouter";

    // Optional attribution header recommended by OpenRouter.
    private static final Map<String, String> HEADERS = Map.of("X-Title", "Eben-Ezer Store Management");

    private final OpenAiCompatibleClient client;

    public OpenRouterProvider(AiProperties aiProperties, RestClient aiRestClient, JsonMapper jsonMapper) {
        this.client = new OpenAiCompatibleClient(
                NAME, aiProperties.openrouter(), aiProperties, aiRestClient, jsonMapper, HEADERS);
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

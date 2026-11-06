package pe.edu.utp.ebenezer.ai.provider;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.exception.AiUnavailableException;

/**
 * Tries the providers in order (OpenAI, then OpenRouter) and returns the first answer. A provider is
 * skipped when it is not configured, and the next one is used when it fails for any reason: no quota
 * or credits (429/402), invalid key (401), outage (5xx), timeout or an unreadable response.
 */
@Primary
@Component
public class FallbackAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(FallbackAiProvider.class);

    private final List<AiProvider> providers;

    @Autowired
    public FallbackAiProvider(OpenAiProvider openAiProvider, OpenRouterProvider openRouterProvider) {
        this(List.of(openAiProvider, openRouterProvider));
    }

    FallbackAiProvider(List<AiProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public String name() {
        return "fallback";
    }

    @Override
    public boolean isConfigured() {
        return providers.stream().anyMatch(AiProvider::isConfigured);
    }

    @Override
    public AiCompletion complete(AiCompletionRequest request) {
        boolean anyConfigured = false;
        for (AiProvider provider : providers) {
            if (!provider.isConfigured()) {
                continue;
            }
            anyConfigured = true;
            try {
                return provider.complete(request);
            } catch (AiProviderException ex) {
                log.warn("AI provider '{}' failed, trying the next one: {}", provider.name(), ex.getMessage());
            }
        }
        throw new AiUnavailableException(anyConfigured
                ? "Every AI provider failed"
                : "No AI provider is configured (OPENAI_API_KEY / OPENROUTER_API_KEY)");
    }
}

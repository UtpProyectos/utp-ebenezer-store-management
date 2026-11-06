package pe.edu.utp.ebenezer.ai.provider;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;

/**
 * A chat-completion provider with tool calling. The application depends only on this interface;
 * the injected bean is {@link FallbackAiProvider} (OpenAI first, OpenRouter as fallback).
 */
public interface AiProvider {

    String name();

    /** False when its API key or model is missing; the provider is then skipped. */
    boolean isConfigured();

    /**
     * @throws AiProviderException when the provider cannot answer (HTTP error, quota, timeout, bad payload)
     */
    AiCompletion complete(AiCompletionRequest request);
}

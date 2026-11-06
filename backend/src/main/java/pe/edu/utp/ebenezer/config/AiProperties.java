package pe.edu.utp.ebenezer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI assistant settings (app.ai). OpenAI is the main provider; OpenRouter is the fallback
 * used when OpenAI is not configured, has no quota left or fails.
 *
 * @param openai         main provider (OPENAI_API_KEY, OPENAI_MODEL)
 * @param openrouter     fallback provider (OPENROUTER_API_KEY, OPENROUTER_MODEL)
 * @param timeoutSeconds read timeout of each provider call
 * @param maxToolRounds  maximum tool-calling rounds per question
 * @param maxTokens      maximum tokens of each model answer
 * @param temperature    low values keep answers factual
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        Provider openai,
        Provider openrouter,
        int timeoutSeconds,
        int maxToolRounds,
        int maxTokens,
        double temperature
) {

    public record Provider(String apiKey, String baseUrl, String model) {

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank() && model != null && !model.isBlank();
        }
    }
}

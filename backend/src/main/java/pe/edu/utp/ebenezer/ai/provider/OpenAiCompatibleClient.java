package pe.edu.utp.ebenezer.ai.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.ai.model.AiMessage;
import pe.edu.utp.ebenezer.ai.model.AiToolCall;
import pe.edu.utp.ebenezer.ai.model.AiToolDefinition;
import pe.edu.utp.ebenezer.config.AiProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Client for the OpenAI Chat Completions API ({@code POST /chat/completions}). OpenRouter exposes the
 * same API, so both providers share this class with their own base URL, key and model.
 */
class OpenAiCompatibleClient {

    private final String providerName;
    private final AiProperties.Provider provider;
    private final AiProperties settings;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final Map<String, String> extraHeaders;

    OpenAiCompatibleClient(String providerName, AiProperties.Provider provider, AiProperties settings,
            RestClient restClient, JsonMapper jsonMapper, Map<String, String> extraHeaders) {
        this.providerName = providerName;
        this.provider = provider;
        this.settings = settings;
        this.restClient = restClient;
        this.jsonMapper = jsonMapper;
        this.extraHeaders = Map.copyOf(extraHeaders);
    }

    boolean isConfigured() {
        return provider != null && provider.isConfigured();
    }

    AiCompletion complete(AiCompletionRequest request) {
        if (!isConfigured()) {
            throw new AiProviderException(providerName + " is not configured");
        }
        String body = jsonMapper.writeValueAsString(buildBody(request));
        String raw;
        try {
            raw = restClient.post()
                    .uri(provider.baseUrl() + "/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.setBearerAuth(provider.apiKey());
                        extraHeaders.forEach(headers::set);
                    })
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            // 401 invalid key, 402/429 no credits or quota, 5xx outage. The body is not logged on purpose.
            throw new AiProviderException(providerName + " returned HTTP " + ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new AiProviderException(providerName + " is unreachable or timed out", ex);
        } catch (RestClientException ex) {
            throw new AiProviderException(providerName + " request failed", ex);
        }
        return parse(raw);
    }

    private ObjectNode buildBody(AiCompletionRequest request) {
        ObjectNode body = jsonMapper.createObjectNode();
        body.put("model", provider.model());
        body.put("temperature", settings.temperature());
        body.put("max_tokens", settings.maxTokens());
        // The system prompt asks for a JSON object; this makes the model stick to it.
        body.putObject("response_format").put("type", "json_object");

        ArrayNode messages = body.putArray("messages");
        for (AiMessage message : request.messages()) {
            ObjectNode node = messages.addObject();
            node.put("role", message.role());
            if (message.content() == null) {
                node.putNull("content");
            } else {
                node.put("content", message.content());
            }
            if (!message.toolCalls().isEmpty()) {
                ArrayNode calls = node.putArray("tool_calls");
                for (AiToolCall call : message.toolCalls()) {
                    ObjectNode callNode = calls.addObject();
                    callNode.put("id", call.id());
                    callNode.put("type", "function");
                    ObjectNode function = callNode.putObject("function");
                    function.put("name", call.name());
                    function.put("arguments", call.arguments());
                }
            }
            if (message.toolCallId() != null) {
                node.put("tool_call_id", message.toolCallId());
            }
        }

        if (!request.tools().isEmpty()) {
            ArrayNode tools = body.putArray("tools");
            for (AiToolDefinition tool : request.tools()) {
                ObjectNode toolNode = tools.addObject();
                toolNode.put("type", "function");
                ObjectNode function = toolNode.putObject("function");
                function.put("name", tool.name());
                function.put("description", tool.description());
                function.set("parameters", jsonMapper.valueToTree(tool.parameters()));
            }
            body.put("tool_choice", "auto");
        }
        return body;
    }

    private AiCompletion parse(String raw) {
        JsonNode root;
        try {
            root = jsonMapper.readTree(raw == null ? "" : raw);
        } catch (JacksonException ex) {
            throw new AiProviderException(providerName + " returned an unreadable response", ex);
        }
        JsonNode message = root.path("choices").path(0).path("message");
        if (message.isMissingNode() || message.isNull()) {
            throw new AiProviderException(providerName + " returned no choices");
        }

        List<AiToolCall> toolCalls = new ArrayList<>();
        for (JsonNode call : message.path("tool_calls")) {
            JsonNode function = call.path("function");
            String arguments = function.path("arguments").asString("");
            toolCalls.add(new AiToolCall(
                    call.path("id").asString(""),
                    function.path("name").asString(""),
                    arguments.isBlank() ? "{}" : arguments));
        }
        JsonNode content = message.path("content");
        String text = content.isString() ? content.asString() : null;
        return new AiCompletion(text, toolCalls, providerName);
    }
}

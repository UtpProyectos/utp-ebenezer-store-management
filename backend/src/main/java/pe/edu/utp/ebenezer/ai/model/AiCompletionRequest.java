package pe.edu.utp.ebenezer.ai.model;

import java.util.List;

/**
 * Input of one model call. With no tools the model must answer with text.
 */
public record AiCompletionRequest(List<AiMessage> messages, List<AiToolDefinition> tools) {

    public AiCompletionRequest {
        messages = List.copyOf(messages);
        tools = tools == null ? List.of() : List.copyOf(tools);
    }
}

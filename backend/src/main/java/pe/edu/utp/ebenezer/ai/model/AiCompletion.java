package pe.edu.utp.ebenezer.ai.model;

import java.util.List;

/**
 * Output of one model call: either text, tool calls, or both.
 *
 * @param provider name of the provider that answered (openai / openrouter)
 */
public record AiCompletion(String content, List<AiToolCall> toolCalls, String provider) {

    public AiCompletion {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
}

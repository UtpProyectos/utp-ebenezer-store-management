package pe.edu.utp.ebenezer.ai.model;

import java.util.List;

/**
 * One message of a chat completion (OpenAI format, also used by OpenRouter).
 *
 * @param role       system, user, assistant or tool
 * @param content    text; null for an assistant message that only calls tools
 * @param toolCalls  tools requested by the assistant (empty otherwise)
 * @param toolCallId id of the call answered by a tool message
 */
public record AiMessage(String role, String content, List<AiToolCall> toolCalls, String toolCallId) {

    public static final String SYSTEM = "system";
    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";
    public static final String TOOL = "tool";

    public AiMessage {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public static AiMessage system(String content) {
        return new AiMessage(SYSTEM, content, List.of(), null);
    }

    public static AiMessage user(String content) {
        return new AiMessage(USER, content, List.of(), null);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage(ASSISTANT, content, List.of(), null);
    }

    public static AiMessage assistant(String content, List<AiToolCall> toolCalls) {
        return new AiMessage(ASSISTANT, content, toolCalls, null);
    }

    public static AiMessage tool(String toolCallId, String content) {
        return new AiMessage(TOOL, content, List.of(), toolCallId);
    }
}

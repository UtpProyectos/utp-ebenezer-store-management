package pe.edu.utp.ebenezer.service.ai.tool;

import java.util.List;
import java.util.Map;

import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;

/**
 * Result of a tool.
 *
 * @param data    compact data serialized to JSON for the model (it writes the text from it)
 * @param blocks  what the user sees under the answer
 * @param actions buttons offered with the answer
 */
public record AssistantToolResult(Object data, List<AssistantBlock> blocks, List<AssistantAction> actions) {

    public AssistantToolResult {
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public static AssistantToolResult error(String message) {
        return new AssistantToolResult(Map.of("error", message), List.of(), List.of());
    }
}

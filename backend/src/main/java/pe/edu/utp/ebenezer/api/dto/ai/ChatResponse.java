package pe.edu.utp.ebenezer.api.dto.ai;

import java.util.List;

/**
 * Answer of the assistant. The text comes from the model; blocks and actions are built by the backend
 * from the tools the model used, so every number shown comes from the database.
 *
 * @param headline   main conclusion
 * @param note       optional advice; null when there is none
 * @param outOfScope true when the question was not about the store (no blocks or actions then)
 * @param blocks     data to show under the text (stats, lists, bars)
 * @param actions    buttons that take the user to a screen or prepare the shopping list
 * @param provider   AI provider that answered (openai / openrouter)
 */
public record ChatResponse(
        String headline,
        String note,
        boolean outOfScope,
        List<AssistantBlock> blocks,
        List<AssistantAction> actions,
        String provider
) {
}

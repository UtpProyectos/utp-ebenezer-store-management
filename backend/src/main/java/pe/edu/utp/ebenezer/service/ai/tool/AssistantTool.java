package pe.edu.utp.ebenezer.service.ai.tool;

import java.util.Map;

import pe.edu.utp.ebenezer.domain.enums.RoleName;
import tools.jackson.databind.JsonNode;

/**
 * A read-only query the model can ask the backend to run. Tools call existing services, never
 * repositories or SQL, and never change data. Register one by making it a Spring {@code @Component}.
 * Checklist to add a tool: AGENTS/ai.md §6.
 */
public interface AssistantTool {

    /** snake_case name the model uses to call it. */
    String name();

    /** Tells the model when to use it (in Spanish, like the prompt). */
    String description();

    /** JSON Schema (type object) of the arguments. */
    Map<String, Object> parameters();

    /**
     * Services are called directly (without the controller {@code @PreAuthorize}), so tools that read
     * admin-only data must return false for other roles.
     */
    default boolean allowedFor(RoleName role) {
        return true;
    }

    /**
     * @throws InvalidToolArgumentException when the arguments are invalid; the message goes back to the model
     */
    AssistantToolResult execute(JsonNode arguments);
}

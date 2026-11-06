package pe.edu.utp.ebenezer.ai.model;

import java.util.Map;

/**
 * A tool offered to the model.
 *
 * @param parameters JSON Schema of the arguments (type object)
 */
public record AiToolDefinition(String name, String description, Map<String, Object> parameters) {
}

package pe.edu.utp.ebenezer.ai.model;

/**
 * A tool the model wants to run.
 *
 * @param id        call id, echoed back in the tool message
 * @param name      tool name (see service.ai.tool)
 * @param arguments JSON object with the arguments, as sent by the model
 */
public record AiToolCall(String id, String name, String arguments) {
}

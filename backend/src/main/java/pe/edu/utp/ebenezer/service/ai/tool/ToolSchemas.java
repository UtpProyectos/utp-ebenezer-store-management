package pe.edu.utp.ebenezer.service.ai.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small builders for the JSON Schema of tool arguments. */
public final class ToolSchemas {

    private ToolSchemas() {
    }

    public static Map<String, Object> noArguments() {
        return object(Map.of(), List.of());
    }

    public static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    public static Map<String, Object> string(String description) {
        return Map.of("type", "string", "description", description);
    }

    public static Map<String, Object> date(String description) {
        return Map.of("type", "string", "description", description + " (yyyy-MM-dd)");
    }

    public static Map<String, Object> integer(String description, int min, int max) {
        return Map.of("type", "integer", "minimum", min, "maximum", max, "description", description);
    }

    public static Map<String, Object> enumeration(String description, List<String> values) {
        return Map.of("type", "string", "enum", values, "description", description);
    }

    /** "from" / "to" properties shared by the period tools. */
    public static Map<String, Object> dateRangeProperties(String defaultDescription) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("from", date("Primer día del período, incluido. " + defaultDescription));
        properties.put("to", date("Último día del período, incluido. Por defecto hoy"));
        return properties;
    }
}

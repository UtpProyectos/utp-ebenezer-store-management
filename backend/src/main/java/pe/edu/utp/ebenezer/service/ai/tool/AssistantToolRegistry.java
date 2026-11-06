package pe.edu.utp.ebenezer.service.ai.tool;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import pe.edu.utp.ebenezer.ai.model.AiToolDefinition;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

/** Every {@link AssistantTool} bean, filtered by the role of the user asking. */
@Component
public class AssistantToolRegistry {

    private final List<AssistantTool> tools;

    public AssistantToolRegistry(List<AssistantTool> tools) {
        this.tools = tools.stream().sorted(Comparator.comparing(AssistantTool::name)).toList();
    }

    public List<AssistantTool> availableFor(RoleName role) {
        return tools.stream().filter(tool -> tool.allowedFor(role)).toList();
    }

    public List<AiToolDefinition> definitionsFor(RoleName role) {
        return availableFor(role).stream()
                .map(tool -> new AiToolDefinition(tool.name(), tool.description(), tool.parameters()))
                .toList();
    }

    /** Empty when the tool does not exist or the role cannot use it. */
    public Optional<AssistantTool> find(String name, RoleName role) {
        return availableFor(role).stream().filter(tool -> tool.name().equals(name)).findFirst();
    }
}

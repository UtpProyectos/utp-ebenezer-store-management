package pe.edu.utp.ebenezer.service.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.ai.model.AiMessage;
import pe.edu.utp.ebenezer.ai.model.AiToolCall;
import pe.edu.utp.ebenezer.ai.model.AiToolDefinition;
import pe.edu.utp.ebenezer.ai.model.AssistantContext;
import pe.edu.utp.ebenezer.ai.prompt.AssistantPromptBuilder;
import pe.edu.utp.ebenezer.ai.prompt.AssistantPrompts;
import pe.edu.utp.ebenezer.ai.provider.AiProvider;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;
import pe.edu.utp.ebenezer.api.dto.ai.ChatHistoryMessage;
import pe.edu.utp.ebenezer.api.dto.ai.ChatRequest;
import pe.edu.utp.ebenezer.api.dto.ai.ChatResponse;
import pe.edu.utp.ebenezer.api.dto.ai.ChatRole;
import pe.edu.utp.ebenezer.config.AiProperties;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.ai.tool.AssistantTool;
import pe.edu.utp.ebenezer.service.ai.tool.AssistantToolRegistry;
import pe.edu.utp.ebenezer.service.ai.tool.AssistantToolResult;
import pe.edu.utp.ebenezer.service.ai.tool.InvalidToolArgumentException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tool-calling loop (AGENTS/ai.md §3): the model receives the system prompt, the recent history and the
 * question, asks for tools, gets their data and writes the final text. Not transactional on purpose:
 * each service opens its own short read-only transaction, so no database connection is held while
 * waiting for the AI provider.
 */
@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantServiceImpl.class);
    private static final int MAX_ACTIONS = 3;
    private static final int MAX_HEADLINE_LENGTH = 600;

    private final AiProvider aiProvider;
    private final AssistantToolRegistry toolRegistry;
    private final AssistantPromptBuilder promptBuilder;
    private final CurrentUserProvider currentUserProvider;
    private final AiProperties aiProperties;
    private final JsonMapper jsonMapper;

    @Override
    public ChatResponse chat(ChatRequest request) {
        User user = currentUserProvider.getCurrentUser();
        RoleName role = user.getRole().getName();

        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(promptBuilder.buildSystemPrompt(
                new AssistantContext(user.getName(), role, LocalDateTime.now()))));
        if (request.history() != null) {
            for (ChatHistoryMessage previous : request.history()) {
                messages.add(previous.role() == ChatRole.USER
                        ? AiMessage.user(previous.content())
                        : AiMessage.assistant(previous.content()));
            }
        }
        messages.add(AiMessage.user(request.message().trim()));

        List<AiToolDefinition> tools = toolRegistry.definitionsFor(role);
        // Latest successful result of each tool; its blocks and actions are shown with the answer.
        Map<String, AssistantToolResult> usedTools = new LinkedHashMap<>();

        AiCompletion completion = aiProvider.complete(new AiCompletionRequest(messages, tools));
        int rounds = 0;
        while (completion.hasToolCalls() && rounds < aiProperties.maxToolRounds()) {
            rounds++;
            messages.add(AiMessage.assistant(completion.content(), completion.toolCalls()));
            for (AiToolCall call : completion.toolCalls()) {
                ToolOutcome outcome = runTool(call, role);
                if (outcome.successful()) {
                    usedTools.remove(call.name());
                    usedTools.put(call.name(), outcome.result());
                }
                messages.add(AiMessage.tool(call.id(), jsonMapper.writeValueAsString(outcome.result().data())));
            }
            // On the last round no tools are offered, so the model has to answer with text.
            boolean lastRound = rounds >= aiProperties.maxToolRounds();
            completion = aiProvider.complete(new AiCompletionRequest(messages, lastRound ? List.of() : tools));
        }
        return toResponse(completion, usedTools);
    }

    private record ToolOutcome(AssistantToolResult result, boolean successful) {
    }

    private ToolOutcome runTool(AiToolCall call, RoleName role) {
        Optional<AssistantTool> tool = toolRegistry.find(call.name(), role);
        if (tool.isEmpty()) {
            return failed("Esa consulta no está disponible para este usuario.");
        }
        JsonNode arguments;
        try {
            arguments = jsonMapper.readTree(call.arguments());
        } catch (JacksonException ex) {
            return failed("Los argumentos no son un JSON válido.");
        }
        try {
            return new ToolOutcome(tool.get().execute(arguments), true);
        } catch (InvalidToolArgumentException ex) {
            return failed(ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Assistant tool '{}' failed", call.name(), ex);
            return failed("No se pudo consultar ese dato en este momento.");
        }
    }

    private static ToolOutcome failed(String message) {
        return new ToolOutcome(AssistantToolResult.error(message), false);
    }

    private ChatResponse toResponse(AiCompletion completion, Map<String, AssistantToolResult> usedTools) {
        ModelAnswer answer = parseAnswer(completion.content());
        if (answer.outOfScope()) {
            return new ChatResponse(
                    orDefault(answer.headline(), AssistantPrompts.OUT_OF_SCOPE_HEADLINE),
                    orDefault(answer.note(), AssistantPrompts.OUT_OF_SCOPE_NOTE),
                    true, List.of(), List.of(), completion.provider());
        }

        List<AssistantBlock> blocks = usedTools.values().stream().flatMap(result -> result.blocks().stream()).toList();
        Map<String, AssistantAction> actions = new LinkedHashMap<>();
        usedTools.values().stream()
                .flatMap(result -> result.actions().stream())
                .sorted((a, b) -> Boolean.compare(b.primary(), a.primary()))
                .forEach(action -> actions.putIfAbsent(action.type() + ":" + action.target(), action));

        return new ChatResponse(
                orDefault(answer.headline(), AssistantPrompts.EMPTY_ANSWER_HEADLINE),
                answer.note(),
                false,
                blocks,
                actions.values().stream().limit(MAX_ACTIONS).toList(),
                completion.provider());
    }

    private record ModelAnswer(String headline, String note, boolean outOfScope) {
    }

    // The model is asked for {"headline", "note", "outOfScope"}; plain text is accepted as the headline.
    ModelAnswer parseAnswer(String content) {
        if (content == null || content.isBlank()) {
            return new ModelAnswer(null, null, false);
        }
        String text = stripCodeFence(content.trim());
        try {
            JsonNode root = jsonMapper.readTree(text);
            if (root.isObject()) {
                return new ModelAnswer(
                        limit(textField(root, "headline")),
                        limit(textField(root, "note")),
                        root.path("outOfScope").asBoolean(false));
            }
        } catch (JacksonException ex) {
            log.debug("Assistant answer is not JSON; using it as plain text");
        }
        return new ModelAnswer(limit(text), null, false);
    }

    private static String textField(JsonNode root, String field) {
        JsonNode value = root.path(field);
        if (!value.isString()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) ? null : text;
    }

    private static String stripCodeFence(String text) {
        if (!text.startsWith("```")) {
            return text;
        }
        int firstLineEnd = text.indexOf('\n');
        int closing = text.lastIndexOf("```");
        if (firstLineEnd < 0 || closing <= firstLineEnd) {
            return text;
        }
        return text.substring(firstLineEnd + 1, closing).trim();
    }

    private static String limit(String text) {
        if (text == null || text.length() <= MAX_HEADLINE_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_HEADLINE_LENGTH).trim() + "…";
    }

    private static String orDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

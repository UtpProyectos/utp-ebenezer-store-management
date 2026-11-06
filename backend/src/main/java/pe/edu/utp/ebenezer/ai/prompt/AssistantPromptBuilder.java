package pe.edu.utp.ebenezer.ai.prompt;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

import pe.edu.utp.ebenezer.ai.model.AssistantContext;
import pe.edu.utp.ebenezer.domain.enums.RoleName;

/** Builds the system prompt for the current user. */
@Component
public class AssistantPromptBuilder {

    private static final Locale SPANISH = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String buildSystemPrompt(AssistantContext context) {
        boolean admin = context.role() == RoleName.ADMIN;
        String capabilities = admin
                ? AssistantPrompts.CAPABILITIES_ALL + AssistantPrompts.CAPABILITIES_ADMIN_EXTRA
                : AssistantPrompts.CAPABILITIES_ALL;
        return AssistantPrompts.SYSTEM_TEMPLATE.formatted(
                AssistantPrompts.STORE_NAME,
                context.userName(),
                admin ? AssistantPrompts.ROLE_ADMIN : AssistantPrompts.ROLE_CASHIER,
                context.now().format(DATE_TIME),
                context.now().getDayOfWeek().getDisplayName(TextStyle.FULL, SPANISH),
                capabilities,
                admin ? AssistantPrompts.PERMISSIONS_ADMIN : AssistantPrompts.PERMISSIONS_CASHIER);
    }
}

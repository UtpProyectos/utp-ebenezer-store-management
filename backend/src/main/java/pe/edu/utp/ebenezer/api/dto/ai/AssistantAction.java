package pe.edu.utp.ebenezer.api.dto.ai;

import java.util.List;

/**
 * A button under the answer. Nothing is changed until the user clicks it.
 *
 * @param target     screen to open (NAVIGATE) or to open after the action
 * @param productIds products to add to the shopping list (GENERATE_SHOPPING_LIST); empty otherwise
 * @param primary    highlighted button
 */
public record AssistantAction(
        AssistantActionType type,
        String label,
        AssistantActionTarget target,
        List<Long> productIds,
        boolean primary
) {

    public static AssistantAction navigate(String label, AssistantActionTarget target) {
        return new AssistantAction(AssistantActionType.NAVIGATE, label, target, List.of(), false);
    }

    public static AssistantAction generateShoppingList(List<Long> productIds) {
        return new AssistantAction(AssistantActionType.GENERATE_SHOPPING_LIST, "Generar lista de compra",
                AssistantActionTarget.SHOPPING_LIST, List.copyOf(productIds), true);
    }
}

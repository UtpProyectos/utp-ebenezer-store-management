package pe.edu.utp.ebenezer.api.dto.ai;

public enum AssistantActionType {
    /** Opens a screen. */
    NAVIGATE,
    /** Adds the products to the open shopping list (POST /api/shopping-lists/generate) and opens it. */
    GENERATE_SHOPPING_LIST
}

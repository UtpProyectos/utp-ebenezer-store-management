package pe.edu.utp.ebenezer.api.dto.ai;

/** Closed set of screens the assistant can link to; the model cannot invent routes. */
public enum AssistantActionTarget {
    DASHBOARD,
    SALES,
    SALES_HISTORY,
    INVENTORY,
    SHOPPING_LIST,
    CONSUMPTION,
    PURCHASES,
    PRODUCTS,
    SUPPLIERS
}

package pe.edu.utp.ebenezer.service.ai.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantActionTarget;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockItem;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockType;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantTone;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.StockStatus;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;
import tools.jackson.databind.JsonNode;

/** Products with LOW or CRITICAL inventory status: the same ones the Inventory and Home screens show to restock. */
@Component
@RequiredArgsConstructor
public class StockAlertsTool implements AssistantTool {

    static final int MAX_ITEMS = 15;

    private final InventoryService inventoryService;

    @Override
    public String name() {
        return "get_stock_alerts";
    }

    @Override
    public String description() {
        return "Productos con stock bajo (en o bajo el mínimo) o crítico (en o bajo la mitad del mínimo), como en Inventario. "
                + "Úsala para: stock crítico, stock bajo, qué se está acabando.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.object(
                Map.of("level", ToolSchemas.enumeration(
                        "CRITICAL solo críticos; ALL bajos y críticos (por defecto)", List.of("ALL", "CRITICAL"))),
                List.of());
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        boolean onlyCritical = "CRITICAL".equals(ToolArguments.text(arguments, "level"));
        List<ProductStockResponse> alerts = inventoryService.findStock().stream()
                .filter(item -> item.status() == StockStatus.LOW || item.status() == StockStatus.CRITICAL)
                .filter(item -> !onlyCritical || isCritical(item))
                .sorted(Comparator.comparing(StockAlertsTool::stockRatio))
                .toList();
        long critical = alerts.stream().filter(StockAlertsTool::isCritical).count();

        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        for (ProductStockResponse item : alerts) {
            boolean isCritical = isCritical(item);
            if (rows.size() < MAX_ITEMS) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("product", item.productName());
                row.put("stock", AssistantFormat.quantity(item.currentStock(), item.baseUnitAbbreviation()));
                row.put("minStock", AssistantFormat.quantity(item.minStock(), item.baseUnitAbbreviation()));
                row.put("level", isCritical ? "crítico" : "bajo");
                rows.add(row);
                items.add(new AssistantBlockItem(
                        item.productName(),
                        AssistantFormat.quantity(item.currentStock(), item.baseUnitAbbreviation())
                                + " / mín. " + AssistantFormat.quantity(item.minStock(), null),
                        isCritical ? AssistantFormat.statusLabel(StockStatus.CRITICAL) : AssistantFormat.statusLabel(StockStatus.LOW),
                        isCritical ? AssistantTone.DANGER : AssistantTone.WARNING,
                        null));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalProducts", alerts.size());
        data.put("criticalProducts", critical);
        data.put("lowProducts", alerts.size() - critical);
        data.put("products", rows);
        if (alerts.size() > MAX_ITEMS) {
            data.put("note", "Solo se listan los " + MAX_ITEMS + " más urgentes.");
        }

        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST, onlyCritical ? "Stock crítico" : "Stock bajo y crítico", items));
        List<AssistantAction> actions = new ArrayList<>();
        if (!alerts.isEmpty()) {
            actions.add(AssistantAction.generateShoppingList(alerts.stream().map(ProductStockResponse::productId).toList()));
        }
        actions.add(AssistantAction.navigate("Ver inventario", AssistantActionTarget.INVENTORY));
        return new AssistantToolResult(data, blocks, actions);
    }

    private static boolean isCritical(ProductStockResponse item) {
        return item.status() == StockStatus.CRITICAL;
    }

    // Lowest stock relative to the minimum first.
    private static BigDecimal stockRatio(ProductStockResponse item) {
        if (item.minStock().signum() <= 0) {
            return BigDecimal.ONE;
        }
        return item.currentStock().divide(item.minStock(), 4, RoundingMode.HALF_UP);
    }
}

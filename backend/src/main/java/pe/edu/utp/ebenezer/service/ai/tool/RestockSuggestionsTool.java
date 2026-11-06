package pe.edu.utp.ebenezer.service.ai.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import pe.edu.utp.ebenezer.service.shoppinglist.RestockCalculator;
import tools.jackson.databind.JsonNode;

/**
 * What to buy: the same products and quantities the shopping list generates (stock at or below the
 * minimum, 2 x minimum - stock), with the cost of the last purchase and its supplier.
 */
@Component
@RequiredArgsConstructor
public class RestockSuggestionsTool implements AssistantTool {

    private final InventoryService inventoryService;

    @Override
    public String name() {
        return "get_restock_suggestions";
    }

    @Override
    public String description() {
        return "Qué productos reponer, cuánto comprar de cada uno, a qué proveedor (el de la última compra) "
                + "y el costo estimado. Úsala para: qué comprar hoy o mañana, reposición, pedido al proveedor.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.noArguments();
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        List<ProductStockResponse> toRestock = inventoryService.findStock().stream()
                .filter(item -> RestockCalculator.needsRestock(item.currentStock(), item.minStock()))
                .sorted(Comparator.comparing(RestockSuggestionsTool::stockRatio))
                .toList();

        BigDecimal totalCost = BigDecimal.ZERO;
        int withoutCost = 0;
        Set<String> suppliers = new LinkedHashSet<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        for (ProductStockResponse item : toRestock) {
            BigDecimal quantity = RestockCalculator.suggestedQuantity(item.minStock(), item.currentStock());
            BigDecimal cost = item.lastUnitCost() == null ? null : AssistantFormat.round(item.lastUnitCost().multiply(quantity));
            if (cost == null) {
                withoutCost++;
            } else {
                totalCost = totalCost.add(cost);
            }
            if (item.lastSupplierName() != null) {
                suppliers.add(item.lastSupplierName());
            }
            boolean high = item.status() == StockStatus.CRITICAL || item.currentStock().signum() <= 0;
            if (rows.size() >= StockAlertsTool.MAX_ITEMS) {
                continue;
            }
            String unit = item.baseUnitAbbreviation();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", item.productName());
            row.put("stock", AssistantFormat.quantity(item.currentStock(), unit));
            row.put("buy", AssistantFormat.quantity(quantity, unit));
            row.put("supplier", item.lastSupplierName());
            row.put("estimatedCost", cost == null ? null : AssistantFormat.money(cost));
            row.put("priority", high ? "alta" : "media");
            rows.add(row);
            items.add(new AssistantBlockItem(
                    item.productName(),
                    "comprar " + AssistantFormat.quantity(quantity, unit),
                    high ? "Alta" : "Media",
                    high ? AssistantTone.DANGER : AssistantTone.WARNING,
                    null));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalProducts", toRestock.size());
        data.put("estimatedTotalCost", AssistantFormat.money(totalCost));
        data.put("productsWithoutPurchaseCost", withoutCost);
        data.put("suppliers", suppliers);
        data.put("rule", "Cantidad para llegar al doble del stock mínimo, al menos 1.");
        data.put("products", rows);

        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST,
                        "Reposición · costo estimado " + AssistantFormat.money(totalCost), items));
        List<AssistantAction> actions = new ArrayList<>();
        if (!toRestock.isEmpty()) {
            actions.add(AssistantAction.generateShoppingList(
                    toRestock.stream().map(ProductStockResponse::productId).toList()));
        }
        actions.add(AssistantAction.navigate("Ver inventario", AssistantActionTarget.INVENTORY));
        return new AssistantToolResult(data, blocks, actions);
    }

    private static BigDecimal stockRatio(ProductStockResponse item) {
        if (item.minStock().signum() <= 0) {
            return BigDecimal.ONE;
        }
        return item.currentStock().divide(item.minStock(), 4, RoundingMode.HALF_UP);
    }
}

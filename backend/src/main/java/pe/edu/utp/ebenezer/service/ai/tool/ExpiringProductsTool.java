package pe.edu.utp.ebenezer.service.ai.tool;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

/**
 * Products with EXPIRED or EXPIRING_SOON inventory status (warning days from the store settings):
 * the same ones the Inventory and Home screens show as expiring.
 */
@Component
@RequiredArgsConstructor
public class ExpiringProductsTool implements AssistantTool {

    private final InventoryService inventoryService;

    @Override
    public String name() {
        return "get_expiring_products";
    }

    @Override
    public String description() {
        return "Productos con lotes vencidos o próximos a vencer, con los días que faltan. "
                + "Úsala para: vencimientos, qué vence pronto, qué retirar.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.noArguments();
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        List<ProductStockResponse> expiring = inventoryService.findStock().stream()
                .filter(item -> item.status() == StockStatus.EXPIRED || item.status() == StockStatus.EXPIRING_SOON)
                .sorted(Comparator.comparing(item -> item.nextExpirationDate() == null
                        ? LocalDate.MIN : item.nextExpirationDate()))
                .toList();

        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        long expiredCount = 0;
        for (ProductStockResponse item : expiring) {
            boolean expired = item.status() == StockStatus.EXPIRED;
            if (expired) {
                expiredCount++;
            }
            if (rows.size() >= StockAlertsTool.MAX_ITEMS) {
                continue;
            }
            String unit = item.baseUnitAbbreviation();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", item.productName());
            row.put("stock", AssistantFormat.quantity(item.currentStock(), unit));
            if (expired) {
                row.put("expiredQuantity", AssistantFormat.quantity(item.expiredQuantity(), unit));
            }
            if (item.nextExpirationDate() != null) {
                row.put("nextExpiration", item.nextExpirationDate().toString());
                row.put("daysLeft", ChronoUnit.DAYS.between(today, item.nextExpirationDate()));
            }
            rows.add(row);
            items.add(new AssistantBlockItem(
                    item.productName(),
                    expired
                            ? AssistantFormat.quantity(item.expiredQuantity(), unit) + " vencido"
                            : AssistantFormat.quantity(item.currentStock(), unit) + " en stock",
                    badge(item, today, expired),
                    expired ? AssistantTone.DANGER : AssistantTone.INFO,
                    null));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("today", today.toString());
        data.put("totalProducts", expiring.size());
        data.put("productsWithExpiredLots", expiredCount);
        data.put("products", rows);

        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST, "Vencimientos", items));
        List<AssistantAction> actions = new ArrayList<>();
        actions.add(AssistantAction.navigate("Ver inventario", AssistantActionTarget.INVENTORY));
        if (expiredCount > 0) {
            actions.add(AssistantAction.navigate("Registrar merma", AssistantActionTarget.CONSUMPTION));
        }
        return new AssistantToolResult(data, blocks, actions);
    }

    private static String badge(ProductStockResponse item, LocalDate today, boolean expired) {
        if (item.nextExpirationDate() == null) {
            return expired ? "Vencido" : "Por vencer";
        }
        long days = ChronoUnit.DAYS.between(today, item.nextExpirationDate());
        if (expired && days < 0) {
            return "Venció hace " + (-days) + " d";
        }
        if (days <= 0) {
            return "Vence hoy";
        }
        return "Vence en " + days + " d";
    }
}

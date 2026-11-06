package pe.edu.utp.ebenezer.service.ai.tool;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantActionTarget;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockItem;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockType;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;
import tools.jackson.databind.JsonNode;

/** Stock, price and status of the active products whose name, barcode or category match a text. */
@Component
@RequiredArgsConstructor
public class ProductStockSearchTool implements AssistantTool {

    private static final int MAX_ITEMS = 10;

    private final InventoryService inventoryService;

    @Override
    public String name() {
        return "search_product_stock";
    }

    @Override
    public String description() {
        return "Busca productos por nombre, código de barras o categoría y devuelve su stock, precio de venta, "
                + "estado, próximo vencimiento y último proveedor. Úsala para: cuánto queda de X, precio de X.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.object(
                Map.of("query", ToolSchemas.string("Parte del nombre, código o categoría, por ejemplo: arroz")),
                List.of("query"));
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        String query = ToolArguments.text(arguments, "query");
        if (query == null) {
            throw new InvalidToolArgumentException("Indica qué producto buscar.");
        }
        String needle = normalize(query);
        List<ProductStockResponse> matches = inventoryService.findStock().stream()
                .filter(item -> normalize(item.productName()).contains(needle)
                        || (item.barcode() != null && item.barcode().contains(query))
                        || (item.categoryName() != null && normalize(item.categoryName()).contains(needle)))
                .toList();

        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        for (ProductStockResponse item : matches.stream().limit(MAX_ITEMS).toList()) {
            String unit = item.baseUnitAbbreviation();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", item.productName());
            row.put("category", item.categoryName());
            row.put("stock", AssistantFormat.quantity(item.currentStock(), unit));
            row.put("minStock", AssistantFormat.quantity(item.minStock(), unit));
            row.put("salePrice", AssistantFormat.money(item.salePrice()) + " por " + unit);
            row.put("status", AssistantFormat.statusLabel(item.status()));
            row.put("nextExpiration", item.nextExpirationDate() == null ? null : item.nextExpirationDate().toString());
            row.put("lastSupplier", item.lastSupplierName());
            rows.add(row);
            items.add(new AssistantBlockItem(
                    item.productName(),
                    AssistantFormat.quantity(item.currentStock(), unit) + " · " + AssistantFormat.money(item.salePrice()),
                    AssistantFormat.statusLabel(item.status()),
                    AssistantFormat.statusTone(item.status()),
                    null));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("query", query);
        data.put("matches", matches.size());
        data.put("products", rows);
        if (matches.isEmpty()) {
            data.put("note", "No hay productos activos que coincidan.");
        }
        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST, "Productos encontrados", items));
        return new AssistantToolResult(data, blocks,
                List.of(AssistantAction.navigate("Ver inventario", AssistantActionTarget.INVENTORY)));
    }

    // Lower case without accents, so "azucar" finds "Azúcar".
    static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}

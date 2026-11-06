package pe.edu.utp.ebenezer.service.ai.tool;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.service.ai.tool.ToolArguments.DateRange;
import pe.edu.utp.ebenezer.service.purchase.PurchaseService;
import tools.jackson.databind.JsonNode;

/** Purchases grouped by supplier. Admin only, like GET /api/purchases. */
@Component
@RequiredArgsConstructor
public class PurchasesBySupplierTool implements AssistantTool {

    private final PurchaseService purchaseService;

    @Override
    public String name() {
        return "get_purchases_by_supplier";
    }

    @Override
    public String description() {
        return "Compras (ingresos de mercadería) agrupadas por proveedor: número de compras y monto total. "
                + "Úsala para: a qué proveedor le compro más, cuánto gasté en compras.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.object(
                ToolSchemas.dateRangeProperties("Por defecto hace 29 días (últimos 30 días)"), List.of());
    }

    @Override
    public boolean allowedFor(RoleName role) {
        return role == RoleName.ADMIN;
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        DateRange range = ToolArguments.dateRange(arguments, today.minusDays(29), today);

        Map<String, SupplierPurchases> bySupplier = new LinkedHashMap<>();
        for (PurchaseResponse purchase : purchaseService.findByPeriod(range.from(), range.to())) {
            if (purchase.status() == PurchaseStatus.CANCELLED) {
                continue;
            }
            String supplier = purchase.supplierName() == null ? "Sin proveedor" : purchase.supplierName();
            SupplierPurchases totals = bySupplier.computeIfAbsent(supplier, key -> new SupplierPurchases());
            totals.count++;
            totals.total = totals.total.add(purchase.total());
        }
        List<Map.Entry<String, SupplierPurchases>> ranking = bySupplier.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, SupplierPurchases> entry) -> entry.getValue().total).reversed())
                .toList();
        BigDecimal max = ranking.isEmpty() ? BigDecimal.ZERO : ranking.getFirst().getValue().total;
        BigDecimal grandTotal = ranking.stream().map(entry -> entry.getValue().total).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> bars = new ArrayList<>();
        for (Map.Entry<String, SupplierPurchases> entry : ranking) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("supplier", entry.getKey());
            row.put("purchases", entry.getValue().count);
            row.put("total", AssistantFormat.money(entry.getValue().total));
            rows.add(row);
            bars.add(new AssistantBlockItem(entry.getKey(),
                    AssistantFormat.money(entry.getValue().total) + " · " + entry.getValue().count
                            + (entry.getValue().count == 1 ? " compra" : " compras"),
                    null, AssistantTone.NEUTRAL, AssistantFormat.ratio(entry.getValue().total, max)));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", range.from().toString());
        data.put("to", range.to().toString());
        data.put("totalSpent", AssistantFormat.money(grandTotal));
        data.put("suppliers", rows);
        List<AssistantBlock> blocks = bars.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.BARS, "Compras por proveedor", bars));
        return new AssistantToolResult(data, blocks,
                List.of(AssistantAction.navigate("Ver proveedores", AssistantActionTarget.SUPPLIERS)));
    }

    private static final class SupplierPurchases {
        private int count;
        private BigDecimal total = BigDecimal.ZERO;
    }
}

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
import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.domain.enums.SaleStatus;
import pe.edu.utp.ebenezer.service.ai.tool.ToolArguments.DateRange;
import pe.edu.utp.ebenezer.service.sale.SaleService;
import tools.jackson.databind.JsonNode;

/**
 * Best-selling products of a period, ranked by revenue (products sold by weight and by unit cannot be
 * compared by quantity). Quantities are summed in the unit of each sale line.
 */
@Component
@RequiredArgsConstructor
public class TopSellingProductsTool implements AssistantTool {

    private static final int DEFAULT_LIMIT = 6;

    private final SaleService saleService;

    @Override
    public String name() {
        return "get_top_selling_products";
    }

    @Override
    public String description() {
        return "Productos más vendidos de un período, ordenados por monto vendido, con la cantidad vendida. "
                + "Úsala para: qué se vende más, productos estrella, qué no se vende.";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> properties = new LinkedHashMap<>(
                ToolSchemas.dateRangeProperties("Por defecto hace 6 días (últimos 7 días)"));
        properties.put("limit", ToolSchemas.integer("Cuántos productos devolver, por defecto 6", 1, 15));
        return ToolSchemas.object(properties, List.of());
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        DateRange range = ToolArguments.dateRange(arguments, today.minusDays(6), today);
        int limit = ToolArguments.integer(arguments, "limit", DEFAULT_LIMIT, 1, 15);

        Map<Long, ProductSales> byProduct = new LinkedHashMap<>();
        for (SaleResponse sale : saleService.findByPeriod(range.from(), range.to())) {
            if (sale.status() == SaleStatus.CANCELLED) {
                continue;
            }
            for (SaleDetailResponse detail : sale.details()) {
                byProduct.computeIfAbsent(detail.productId(), id -> new ProductSales(detail.productName()))
                        .add(detail);
            }
        }
        List<ProductSales> ranking = byProduct.values().stream()
                .sorted(Comparator.comparing((ProductSales sales) -> sales.revenue).reversed())
                .toList();
        List<ProductSales> top = ranking.stream().limit(limit).toList();
        BigDecimal max = top.isEmpty() ? BigDecimal.ZERO : top.getFirst().revenue;

        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> bars = new ArrayList<>();
        for (ProductSales sales : top) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", sales.name);
            row.put("revenue", AssistantFormat.money(sales.revenue));
            row.put("quantity", sales.quantityText());
            rows.add(row);
            bars.add(new AssistantBlockItem(sales.name, AssistantFormat.money(sales.revenue) + " · " + sales.quantityText(),
                    null, AssistantTone.NEUTRAL, AssistantFormat.ratio(sales.revenue, max)));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", range.from().toString());
        data.put("to", range.to().toString());
        data.put("productsSold", ranking.size());
        data.put("top", rows);
        List<AssistantBlock> blocks = bars.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.BARS, "Más vendidos", bars));
        return new AssistantToolResult(data, blocks,
                List.of(AssistantAction.navigate("Ver productos", AssistantActionTarget.PRODUCTS)));
    }

    private static final class ProductSales {

        private final String name;
        private BigDecimal revenue = BigDecimal.ZERO;
        private final Map<String, BigDecimal> quantityByUnit = new LinkedHashMap<>();

        private ProductSales(String name) {
            this.name = name;
        }

        private void add(SaleDetailResponse detail) {
            // The line subtotal already has the promotion discount applied.
            revenue = revenue.add(detail.subtotal());
            quantityByUnit.merge(detail.unitOfMeasureAbbreviation(), detail.quantity(), BigDecimal::add);
        }

        private String quantityText() {
            List<String> parts = new ArrayList<>();
            quantityByUnit.forEach((unit, quantity) -> parts.add(AssistantFormat.quantity(quantity, unit)));
            return String.join(" + ", parts);
        }
    }
}

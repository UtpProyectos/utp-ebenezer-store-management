package pe.edu.utp.ebenezer.service.ai.tool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantActionTarget;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockItem;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockType;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantTone;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionDetailResponse;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.service.ai.tool.ToolArguments.DateRange;
import pe.edu.utp.ebenezer.service.consumption.InternalConsumptionService;
import tools.jackson.databind.JsonNode;

/** Internal consumption and waste of a period. Admin only, like GET /api/internal-consumptions. */
@Component
@RequiredArgsConstructor
public class InternalConsumptionTool implements AssistantTool {

    private static final String NO_REASON = "Sin motivo";

    private final InternalConsumptionService internalConsumptionService;

    @Override
    public String name() {
        return "get_internal_consumption";
    }

    @Override
    public String description() {
        return "Consumo interno (productos usados en la tienda o dados de baja como merma, sin venta) de un "
                + "período: productos, cantidades, motivo y quién lo registró. Úsala para: mermas, "
                + "consumo del personal, qué se retiró.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.object(ToolSchemas.dateRangeProperties("Por defecto el día 1 del mes actual"), List.of());
    }

    @Override
    public boolean allowedFor(RoleName role) {
        return role == RoleName.ADMIN;
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        DateRange range = ToolArguments.dateRange(arguments, today.withDayOfMonth(1), today);
        List<InternalConsumptionResponse> consumptions = internalConsumptionService.findByPeriod(range.from(), range.to());

        Map<String, Integer> linesByReason = new TreeMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        int lines = 0;
        for (InternalConsumptionResponse consumption : consumptions) {
            String reason = consumption.reason() == null || consumption.reason().isBlank() ? NO_REASON : consumption.reason();
            for (InternalConsumptionDetailResponse detail : consumption.details()) {
                lines++;
                linesByReason.merge(reason, 1, Integer::sum);
                if (rows.size() >= StockAlertsTool.MAX_ITEMS) {
                    continue;
                }
                String quantity = AssistantFormat.quantity(detail.quantity(), detail.unitOfMeasureAbbreviation());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("date", consumption.consumptionDate().toLocalDate().toString());
                row.put("product", detail.productName());
                row.put("quantity", quantity);
                row.put("reason", reason);
                row.put("registeredBy", consumption.userName());
                rows.add(row);
                items.add(new AssistantBlockItem(detail.productName(),
                        quantity + " · " + AssistantFormat.day(consumption.consumptionDate().toLocalDate()),
                        reason, AssistantTone.NEUTRAL, null));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", range.from().toString());
        data.put("to", range.to().toString());
        data.put("records", consumptions.size());
        data.put("productLines", lines);
        data.put("linesByReason", linesByReason);
        data.put("latest", rows);
        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST, "Consumo interno", items));
        return new AssistantToolResult(data, blocks,
                List.of(AssistantAction.navigate("Ir a consumo interno", AssistantActionTarget.CONSUMPTION)));
    }
}

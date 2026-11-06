package pe.edu.utp.ebenezer.service.ai.tool;

import java.util.ArrayList;
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
import pe.edu.utp.ebenezer.api.dto.promotion.PromotionResponse;
import pe.edu.utp.ebenezer.service.promotion.PromotionService;
import tools.jackson.databind.JsonNode;

/** Active promotions valid now. */
@Component
@RequiredArgsConstructor
public class CurrentPromotionsTool implements AssistantTool {

    private final PromotionService promotionService;

    @Override
    public String name() {
        return "get_current_promotions";
    }

    @Override
    public String description() {
        return "Promociones activas y vigentes hoy: producto, cantidad, precio promocional y hasta cuándo. "
                + "Úsala para: qué promociones hay, ofertas.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.noArguments();
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        List<PromotionResponse> promotions = promotionService.findCurrent();
        List<Map<String, Object>> rows = new ArrayList<>();
        List<AssistantBlockItem> items = new ArrayList<>();
        for (PromotionResponse promotion : promotions) {
            String offer = AssistantFormat.quantity(promotion.promotionQuantity(), promotion.unitOfMeasureAbbreviation())
                    + " x " + AssistantFormat.money(promotion.promotionalPrice());
            String until = promotion.endDate() == null ? null : promotion.endDate().toLocalDate().toString();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("promotion", promotion.name());
            row.put("product", promotion.productName());
            row.put("offer", offer);
            row.put("repeatable", Boolean.TRUE.equals(promotion.repeatable()));
            row.put("until", until);
            rows.add(row);
            items.add(new AssistantBlockItem(promotion.productName(), offer,
                    until == null ? "Sin fecha fin" : "Hasta " + AssistantFormat.day(promotion.endDate().toLocalDate()),
                    AssistantTone.ACCENT, null));
        }
        Map<String, Object> data = Map.of("totalPromotions", promotions.size(), "promotions", rows);
        List<AssistantBlock> blocks = items.isEmpty()
                ? List.of()
                : List.of(new AssistantBlock(AssistantBlockType.LIST, "Promociones vigentes", items));
        return new AssistantToolResult(data, blocks,
                List.of(AssistantAction.navigate("Ir a ventas", AssistantActionTarget.SALES)));
    }
}

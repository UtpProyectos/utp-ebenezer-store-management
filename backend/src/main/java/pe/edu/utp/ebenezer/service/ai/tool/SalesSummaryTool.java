package pe.edu.utp.ebenezer.service.ai.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantAction;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantActionTarget;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlock;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockItem;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockType;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantTone;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.domain.enums.SaleStatus;
import pe.edu.utp.ebenezer.service.ai.tool.ToolArguments.DateRange;
import pe.edu.utp.ebenezer.service.sale.SaleService;
import tools.jackson.databind.JsonNode;

/** Sales of a period: total, tickets, average ticket, by day, by payment method and busiest hour. */
@Component
@RequiredArgsConstructor
public class SalesSummaryTool implements AssistantTool {

    private static final int MAX_DAY_BARS = 31;

    private final SaleService saleService;

    @Override
    public String name() {
        return "get_sales_summary";
    }

    @Override
    public String description() {
        return "Resumen de ventas de un día o período: total vendido, número de ventas (tickets), ticket "
                + "promedio, ventas por día, por método de pago y hora con más ventas. Las ventas anuladas no "
                + "cuentan. Úsala para: cuánto vendí hoy, ayer, esta semana o este mes.";
    }

    @Override
    public Map<String, Object> parameters() {
        return ToolSchemas.object(ToolSchemas.dateRangeProperties("Por defecto hoy"), List.of());
    }

    @Override
    public AssistantToolResult execute(JsonNode arguments) {
        LocalDate today = LocalDate.now();
        DateRange range = ToolArguments.dateRange(arguments, today, today);
        List<SaleResponse> sales = saleService.findByPeriod(range.from(), range.to()).stream()
                .filter(sale -> sale.status() != SaleStatus.CANCELLED)
                .toList();

        BigDecimal total = sales.stream().map(SaleResponse::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sales.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
        BigDecimal dailyAverage = total.divide(BigDecimal.valueOf(range.days()), 2, RoundingMode.HALF_UP);

        Map<LocalDate, BigDecimal> byDay = new TreeMap<>();
        for (LocalDate day = range.from(); !day.isAfter(range.to()); day = day.plusDays(1)) {
            byDay.put(day, BigDecimal.ZERO);
        }
        sales.forEach(sale -> byDay.merge(sale.saleDate().toLocalDate(), sale.total(), BigDecimal::add));

        Map<String, String> byPaymentMethod = sales.stream().collect(Collectors.groupingBy(
                sale -> sale.paymentMethod().name(), TreeMap::new,
                Collectors.collectingAndThen(
                        Collectors.reducing(BigDecimal.ZERO, SaleResponse::total, BigDecimal::add),
                        AssistantFormat::money)));
        Map<Integer, Long> byHour = sales.stream()
                .collect(Collectors.groupingBy(sale -> sale.saleDate().getHour(), Collectors.counting()));
        Integer busiestHour = byHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", range.from().toString());
        data.put("to", range.to().toString());
        data.put("days", range.days());
        data.put("total", AssistantFormat.money(total));
        data.put("tickets", sales.size());
        data.put("averageTicket", AssistantFormat.money(average));
        if (range.days() > 1) {
            data.put("dailyAverage", AssistantFormat.money(dailyAverage));
            data.put("byDay", byDay.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey().toString(), entry -> AssistantFormat.money(entry.getValue()),
                    (a, b) -> a, LinkedHashMap::new)));
        }
        data.put("byPaymentMethod", byPaymentMethod);
        data.put("busiestHour", busiestHour == null ? null : String.format("%02d:00 a %02d:00", busiestHour, busiestHour + 1));

        List<AssistantBlockItem> stats = new ArrayList<>();
        stats.add(new AssistantBlockItem("Ventas", AssistantFormat.money(total), null, AssistantTone.ACCENT, null));
        stats.add(new AssistantBlockItem("Tickets", String.valueOf(sales.size()), null, AssistantTone.NEUTRAL, null));
        stats.add(new AssistantBlockItem("Ticket promedio", AssistantFormat.money(average), null, AssistantTone.NEUTRAL, null));
        if (range.days() > 1) {
            stats.add(new AssistantBlockItem("Promedio diario", AssistantFormat.money(dailyAverage), null, AssistantTone.NEUTRAL, null));
        }
        List<AssistantBlock> blocks = new ArrayList<>();
        blocks.add(new AssistantBlock(AssistantBlockType.STATS, title(range), stats));
        if (range.days() > 1 && range.days() <= MAX_DAY_BARS) {
            BigDecimal max = byDay.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            List<AssistantBlockItem> bars = byDay.entrySet().stream()
                    .map(entry -> new AssistantBlockItem(
                            entry.getKey().equals(today) ? "Hoy" : AssistantFormat.day(entry.getKey()),
                            AssistantFormat.money(entry.getValue()),
                            null,
                            entry.getKey().equals(today) ? AssistantTone.ACCENT : AssistantTone.NEUTRAL,
                            AssistantFormat.ratio(entry.getValue(), max)))
                    .toList();
            blocks.add(new AssistantBlock(AssistantBlockType.BARS, "Ventas por día", bars));
        }
        return new AssistantToolResult(data, blocks, List.of(
                AssistantAction.navigate("Ver historial de ventas", AssistantActionTarget.SALES_HISTORY),
                AssistantAction.navigate("Ver inicio", AssistantActionTarget.DASHBOARD)));
    }

    private static String title(DateRange range) {
        if (range.from().equals(range.to())) {
            return range.to().equals(LocalDate.now()) ? "Ventas de hoy" : "Ventas del " + AssistantFormat.day(range.to());
        }
        return "Ventas del " + AssistantFormat.day(range.from()) + " al " + AssistantFormat.day(range.to());
    }
}

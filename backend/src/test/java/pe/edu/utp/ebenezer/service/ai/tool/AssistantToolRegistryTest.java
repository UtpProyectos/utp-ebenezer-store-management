package pe.edu.utp.ebenezer.service.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;

import pe.edu.utp.ebenezer.ai.model.AiToolDefinition;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.service.consumption.InternalConsumptionService;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;
import pe.edu.utp.ebenezer.service.purchase.PurchaseService;
import pe.edu.utp.ebenezer.service.sale.SaleService;

class AssistantToolRegistryTest {

    private final AssistantToolRegistry registry = new AssistantToolRegistry(List.of(
            new StockAlertsTool(mock(InventoryService.class)),
            new SalesSummaryTool(mock(SaleService.class)),
            new PurchasesBySupplierTool(mock(PurchaseService.class)),
            new InternalConsumptionTool(mock(InternalConsumptionService.class))));

    @Test
    void cashierDoesNotReceiveAdminOnlyTools() {
        List<String> names = registry.definitionsFor(RoleName.CASHIER).stream().map(AiToolDefinition::name).toList();

        assertThat(names).containsExactlyInAnyOrder("get_stock_alerts", "get_sales_summary");
        assertThat(registry.find("get_purchases_by_supplier", RoleName.CASHIER)).isEmpty();
    }

    @Test
    void adminReceivesEveryTool() {
        assertThat(registry.definitionsFor(RoleName.ADMIN)).hasSize(4);
        assertThat(registry.find("get_internal_consumption", RoleName.ADMIN)).isPresent();
    }

    @Test
    void unknownToolIsNotFound() {
        assertThat(registry.find("run_sql", RoleName.ADMIN)).isEmpty();
    }
}

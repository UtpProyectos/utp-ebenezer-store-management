package pe.edu.utp.ebenezer.service.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.ai.model.AiMessage;
import pe.edu.utp.ebenezer.ai.model.AiToolCall;
import pe.edu.utp.ebenezer.ai.prompt.AssistantPromptBuilder;
import pe.edu.utp.ebenezer.ai.provider.AiProvider;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantActionType;
import pe.edu.utp.ebenezer.api.dto.ai.AssistantBlockType;
import pe.edu.utp.ebenezer.api.dto.ai.ChatRequest;
import pe.edu.utp.ebenezer.api.dto.ai.ChatResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.StockStatus;
import pe.edu.utp.ebenezer.config.AiProperties;
import pe.edu.utp.ebenezer.domain.entity.Role;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.RoleName;
import pe.edu.utp.ebenezer.domain.enums.UnitType;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.ai.tool.AssistantToolRegistry;
import pe.edu.utp.ebenezer.service.ai.tool.PurchasesBySupplierTool;
import pe.edu.utp.ebenezer.service.ai.tool.StockAlertsTool;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;
import pe.edu.utp.ebenezer.service.purchase.PurchaseService;
import tools.jackson.databind.json.JsonMapper;

class AssistantServiceImplTest {

    private AiProvider aiProvider;
    private InventoryService inventoryService;
    private PurchaseService purchaseService;
    private AssistantServiceImpl assistantService;
    private final List<AiCompletionRequest> requests = new ArrayList<>();

    @BeforeEach
    void setUp() {
        aiProvider = mock(AiProvider.class);
        inventoryService = mock(InventoryService.class);
        purchaseService = mock(PurchaseService.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        when(currentUserProvider.getCurrentUser()).thenReturn(user(RoleName.CASHIER));

        AssistantToolRegistry registry = new AssistantToolRegistry(List.of(
                new StockAlertsTool(inventoryService), new PurchasesBySupplierTool(purchaseService)));
        AiProperties properties = new AiProperties(null, null, 25, 4, 700, 0.2);
        assistantService = new AssistantServiceImpl(aiProvider, registry, new AssistantPromptBuilder(),
                currentUserProvider, properties, JsonMapper.builder().build());
    }

    @Test
    void runsTheRequestedToolAndReturnsItsBlocks() {
        when(inventoryService.findStock()).thenReturn(List.of(stock(1L, "Leche Gloria", "2", "10", StockStatus.CRITICAL)));
        when(aiProvider.complete(any())).thenAnswer(invocation -> {
            requests.add(invocation.getArgument(0));
            return requests.size() == 1
                    ? new AiCompletion(null, List.of(new AiToolCall("call_1", "get_stock_alerts", "{}")), "openai")
                    : new AiCompletion("{\"headline\":\"Tienes 1 producto en stock crítico.\",\"note\":null,\"outOfScope\":false}",
                            List.of(), "openai");
        });

        ChatResponse response = assistantService.chat(new ChatRequest("¿Qué está en stock crítico?", List.of()));

        assertThat(response.headline()).isEqualTo("Tienes 1 producto en stock crítico.");
        assertThat(response.outOfScope()).isFalse();
        assertThat(response.blocks()).singleElement()
                .satisfies(block -> assertThat(block.type()).isEqualTo(AssistantBlockType.LIST));
        assertThat(response.actions()).first()
                .satisfies(action -> assertThat(action.type()).isEqualTo(AssistantActionType.GENERATE_SHOPPING_LIST));
        // The second call carries the tool result back to the model.
        AiMessage toolMessage = requests.get(1).messages().getLast();
        assertThat(toolMessage.role()).isEqualTo(AiMessage.TOOL);
        assertThat(toolMessage.content()).contains("Leche Gloria");
    }

    @Test
    void cashierCannotRunAdminToolEvenIfTheModelAsksForIt() {
        when(aiProvider.complete(any())).thenAnswer(invocation -> {
            requests.add(invocation.getArgument(0));
            return requests.size() == 1
                    ? new AiCompletion(null, List.of(new AiToolCall("call_1", "get_purchases_by_supplier", "{}")), "openai")
                    : new AiCompletion("{\"headline\":\"Esa información es del administrador.\"}", List.of(), "openai");
        });

        ChatResponse response = assistantService.chat(new ChatRequest("¿A qué proveedor le compro más?", null));

        verify(purchaseService, never()).findByPeriod(any(), any());
        assertThat(requests.getFirst().tools()).extracting("name").containsExactly("get_stock_alerts");
        assertThat(response.blocks()).isEmpty();
    }

    @Test
    void outOfScopeAnswerHasNoBlocksAndAddsExamples() {
        when(aiProvider.complete(any())).thenReturn(new AiCompletion(
                "{\"headline\":\"Solo puedo ayudarte con tu bodega.\",\"note\":null,\"outOfScope\":true}", List.of(), "openrouter"));

        ChatResponse response = assistantService.chat(new ChatRequest("Ayúdame con mi tarea de matemáticas", null));

        assertThat(response.outOfScope()).isTrue();
        assertThat(response.blocks()).isEmpty();
        assertThat(response.actions()).isEmpty();
        assertThat(response.note()).isNotBlank();
        assertThat(response.provider()).isEqualTo("openrouter");
    }

    @Test
    void plainTextAnswerIsUsedAsHeadline() {
        when(aiProvider.complete(any())).thenReturn(new AiCompletion("Hola, ¿en qué te ayudo con tu tienda?", List.of(), "openai"));

        ChatResponse response = assistantService.chat(new ChatRequest("Hola", null));

        assertThat(response.headline()).isEqualTo("Hola, ¿en qué te ayudo con tu tienda?");
    }

    @Test
    void stopsCallingToolsAfterTheMaximumRounds() {
        when(inventoryService.findStock()).thenReturn(List.of());
        when(aiProvider.complete(any())).thenAnswer(invocation -> {
            AiCompletionRequest request = invocation.getArgument(0);
            requests.add(request);
            return request.tools().isEmpty()
                    ? new AiCompletion("{\"headline\":\"No hay productos con stock bajo.\"}", List.of(), "openai")
                    : new AiCompletion(null, List.of(new AiToolCall("call_" + requests.size(), "get_stock_alerts", "{}")), "openai");
        });

        ChatResponse response = assistantService.chat(new ChatRequest("¿Qué se está acabando?", null));

        assertThat(requests).hasSize(5);
        assertThat(requests.getLast().tools()).isEmpty();
        assertThat(response.headline()).isEqualTo("No hay productos con stock bajo.");
    }

    private static User user(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setName("Rosa Quispe");
        user.setRole(role);
        return user;
    }

    private static ProductStockResponse stock(Long id, String name, String current, String min, StockStatus status) {
        return new ProductStockResponse(id, name, null, "Lácteos", 1L, "und", UnitType.UNIT,
                new BigDecimal(current), new BigDecimal(min), new BigDecimal("4.50"), new BigDecimal("3.80"),
                "Distribuidora Lima", null, BigDecimal.ZERO, status);
    }
}

package pe.edu.utp.ebenezer.service.shoppinglist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailResponse;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailUpdateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListResponse;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Purchase;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.entity.ShoppingList;
import pe.edu.utp.ebenezer.domain.entity.ShoppingListDetail;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.enums.ShoppingListSource;
import pe.edu.utp.ebenezer.domain.enums.ShoppingListStatus;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.ProductStockView;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.shoppinglist.ShoppingListDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.shoppinglist.ShoppingListRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceImplTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;
    @Mock
    private ShoppingListDetailRepository shoppingListDetailRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PurchaseDetailRepository purchaseDetailRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ShoppingListServiceImpl shoppingListService;

    @Test
    void generateCreatesSystemListWithProductsAtOrBelowMinimumStock() {
        Product low = product(1L, "Leche", "10");
        Product ok = product(2L, "Arroz", "5");
        when(inventoryMovementRepository.findStockByProduct())
                .thenReturn(List.of(stock(1L, "4"), stock(2L, "20")));
        when(shoppingListRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(productRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(low, ok));
        when(currentUserProvider.getCurrentUser()).thenReturn(user());
        when(purchaseDetailRepository.findLatestByProduct(PurchaseStatus.REGISTERED))
                .thenReturn(List.of(lastPurchase(low, "Distribuidora Lima", "30.00", "10")));

        ShoppingListResponse response = shoppingListService.generate(null);

        assertThat(response.source()).isEqualTo(ShoppingListSource.SYSTEM);
        assertThat(response.status()).isEqualTo(ShoppingListStatus.PENDING);
        assertThat(response.details()).hasSize(1);
        ShoppingListDetailResponse item = response.details().getFirst();
        // Twice the minimum (20) minus the stock (4).
        assertThat(item.suggestedQuantity()).isEqualByComparingTo("16");
        assertThat(item.estimatedCost()).isEqualByComparingTo("48.00");
        assertThat(item.supplierName()).isEqualTo("Distribuidora Lima");
        assertThat(item.currentStock()).isEqualByComparingTo("4");
        assertThat(response.estimatedCost()).isEqualByComparingTo("48.00");
        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    void generateFailsWhenThereIsNothingToRestockAndNoOpenList() {
        when(inventoryMovementRepository.findStockByProduct()).thenReturn(List.of(stock(2L, "20")));
        when(shoppingListRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(productRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(product(2L, "Arroz", "5")));

        assertThatThrownBy(() -> shoppingListService.generate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("There are no products to restock");
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    void markingEveryItemAsPurchasedCompletesTheList() {
        ShoppingList list = listWith(detail(10L, product(1L, "Leche", "10"), "5"));
        when(shoppingListRepository.findByDetailId(10L)).thenReturn(Optional.of(list));
        when(inventoryMovementRepository.findStockByProduct()).thenReturn(List.of());

        ShoppingListResponse response = shoppingListService.updateDetail(
                10L, new ShoppingListDetailUpdateRequest(null, true));

        assertThat(response.status()).isEqualTo(ShoppingListStatus.COMPLETED);
        assertThat(response.details().getFirst().purchasedQuantity()).isEqualByComparingTo("5");
    }

    @Test
    void removingTheLastItemCancelsTheList() {
        ShoppingListDetail detail = detail(10L, product(1L, "Leche", "10"), "5");
        ShoppingList list = listWith(detail);
        when(shoppingListRepository.findByDetailId(10L)).thenReturn(Optional.of(list));
        when(inventoryMovementRepository.findStockByProduct()).thenReturn(List.of());

        ShoppingListResponse response = shoppingListService.removeDetail(10L);

        assertThat(response.status()).isEqualTo(ShoppingListStatus.CANCELLED);
        assertThat(response.details()).isEmpty();
        verify(shoppingListDetailRepository).delete(detail);
    }

    private static Product product(Long id, String name, String minStock) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setId(1L);
        unit.setAbbreviation("UND");
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setMinStock(new BigDecimal(minStock));
        product.setBaseUnit(unit);
        return product;
    }

    private static ProductStockView stock(Long productId, String quantity) {
        return new ProductStockView() {
            @Override
            public Long getProductId() {
                return productId;
            }

            @Override
            public BigDecimal getStock() {
                return new BigDecimal(quantity);
            }
        };
    }

    private static PurchaseDetail lastPurchase(Product product, String supplierName, String subtotal, String quantity) {
        Supplier supplier = new Supplier();
        supplier.setId(7L);
        supplier.setName(supplierName);
        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        PurchaseDetail detail = new PurchaseDetail();
        detail.setPurchase(purchase);
        detail.setProduct(product);
        detail.setSubtotal(new BigDecimal(subtotal));
        detail.setBaseQuantity(new BigDecimal(quantity));
        return detail;
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Rosa Quispe");
        return user;
    }

    private static ShoppingListDetail detail(Long id, Product product, String quantity) {
        ShoppingListDetail detail = new ShoppingListDetail();
        detail.setId(id);
        detail.setProduct(product);
        detail.setUnitOfMeasure(product.getBaseUnit());
        detail.setSuggestedQuantity(new BigDecimal(quantity));
        return detail;
    }

    private static ShoppingList listWith(ShoppingListDetail detail) {
        ShoppingList list = new ShoppingList();
        list.setId(1L);
        list.setUser(user());
        list.setSource(ShoppingListSource.SYSTEM);
        list.setStatus(ShoppingListStatus.PENDING);
        detail.setShoppingList(list);
        list.getDetails().add(detail);
        return list;
    }
}

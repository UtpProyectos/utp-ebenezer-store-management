package pe.edu.utp.ebenezer.service.shoppinglist;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailUpdateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListGenerateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListResponse;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.entity.ShoppingList;
import pe.edu.utp.ebenezer.domain.entity.ShoppingListDetail;
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
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {

    private static final Set<ShoppingListStatus> OPEN_STATUSES =
            Set.of(ShoppingListStatus.PENDING, ShoppingListStatus.PARTIAL);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListDetailRepository shoppingListDetailRepository;
    private final ProductRepository productRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingListResponse> findCurrent() {
        return shoppingListRepository.findFirstByOrderByIdDesc()
                .filter(list -> list.getStatus() != ShoppingListStatus.CANCELLED)
                .map(list -> ShoppingListMapper.toResponse(list, stockByProduct()));
    }

    @Override
    @Transactional
    public ShoppingListResponse generate(ShoppingListGenerateRequest request) {
        List<Long> productIds = request == null || request.productIds() == null
                ? List.of()
                : request.productIds().stream().distinct().toList();
        boolean manual = !productIds.isEmpty();
        Map<Long, BigDecimal> stock = stockByProduct();

        ShoppingList list = shoppingListRepository.findFirstByOrderByIdDesc()
                .filter(current -> OPEN_STATUSES.contains(current.getStatus()))
                .orElse(null);
        Set<Long> alreadyListed = list == null
                ? Set.of()
                : list.getDetails().stream().map(detail -> detail.getProduct().getId()).collect(Collectors.toSet());

        List<Product> toAdd = (manual ? findActiveProducts(productIds) : findProductsToRestock(stock)).stream()
                .filter(product -> !alreadyListed.contains(product.getId()))
                .toList();
        if (toAdd.isEmpty() && list == null) {
            throw new BusinessException("There are no products to restock");
        }

        boolean newList = list == null;
        if (newList) {
            list = new ShoppingList();
            list.setUser(currentUserProvider.getCurrentUser());
            list.setSource(manual ? ShoppingListSource.MANUAL : ShoppingListSource.SYSTEM);
        }

        Map<Long, PurchaseDetail> lastDetailByProduct = purchaseDetailRepository
                .findLatestByProduct(PurchaseStatus.REGISTERED).stream()
                .collect(Collectors.toMap(detail -> detail.getProduct().getId(), Function.identity()));
        List<ShoppingListDetail> added = new ArrayList<>();
        for (Product product : toAdd) {
            PurchaseDetail lastDetail = lastDetailByProduct.get(product.getId());
            BigDecimal quantity = suggestedQuantity(product, stock.getOrDefault(product.getId(), BigDecimal.ZERO));

            ShoppingListDetail detail = new ShoppingListDetail();
            detail.setShoppingList(list);
            detail.setProduct(product);
            detail.setSupplier(lastDetail == null ? null : lastDetail.getPurchase().getSupplier());
            detail.setUnitOfMeasure(product.getBaseUnit());
            detail.setSuggestedQuantity(quantity);
            detail.setEstimatedCost(estimateCost(lastDetail, quantity));
            list.getDetails().add(detail);
            added.add(detail);
        }
        refreshTotals(list);

        if (newList) {
            shoppingListRepository.save(list);
        } else {
            shoppingListDetailRepository.saveAll(added);
        }
        return ShoppingListMapper.toResponse(list, stock);
    }

    @Override
    @Transactional
    public ShoppingListResponse updateDetail(Long detailId, ShoppingListDetailUpdateRequest request) {
        ShoppingList list = getEditableList(detailId);
        ShoppingListDetail detail = findDetail(list, detailId);

        if (request.suggestedQuantity() != null) {
            BigDecimal quantity = request.suggestedQuantity();
            PurchaseDetail lastDetail = purchaseDetailRepository
                    .findFirstByProduct_IdAndPurchase_StatusOrderByIdDesc(
                            detail.getProduct().getId(), PurchaseStatus.REGISTERED)
                    .orElse(null);
            detail.setSuggestedQuantity(quantity);
            detail.setEstimatedCost(estimateCost(lastDetail, quantity));
        }
        if (request.purchased() != null) {
            detail.setPurchased(request.purchased());
        }
        detail.setPurchasedQuantity(detail.getPurchased() ? detail.getSuggestedQuantity() : BigDecimal.ZERO);
        refreshTotals(list);
        return ShoppingListMapper.toResponse(list, stockByProduct());
    }

    @Override
    @Transactional
    public ShoppingListResponse removeDetail(Long detailId) {
        ShoppingList list = getEditableList(detailId);
        ShoppingListDetail detail = findDetail(list, detailId);

        list.getDetails().remove(detail);
        shoppingListDetailRepository.delete(detail);
        refreshTotals(list);
        return ShoppingListMapper.toResponse(list, stockByProduct());
    }

    private ShoppingList getEditableList(Long detailId) {
        ShoppingList list = shoppingListRepository.findByDetailId(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found"));
        if (list.getStatus() == ShoppingListStatus.CANCELLED) {
            throw new BusinessException("The shopping list is cancelled");
        }
        return list;
    }

    private static ShoppingListDetail findDetail(ShoppingList list, Long detailId) {
        return list.getDetails().stream()
                .filter(detail -> detail.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found"));
    }

    private List<Product> findActiveProducts(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds).stream()
                .filter(Product::getActive)
                .toList();
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return products;
    }

    // Products to restock: current stock at or below the minimum stock (AGENTS/database.md §18).
    private List<Product> findProductsToRestock(Map<Long, BigDecimal> stock) {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .filter(product -> stock.getOrDefault(product.getId(), BigDecimal.ZERO)
                        .compareTo(product.getMinStock()) <= 0)
                .toList();
    }

    // Same suggestion as the prototype: enough to reach twice the minimum stock, at least one.
    private static BigDecimal suggestedQuantity(Product product, BigDecimal stock) {
        BigDecimal missing = product.getMinStock().multiply(TWO).subtract(stock).setScale(0, RoundingMode.CEILING);
        return missing.max(BigDecimal.ONE);
    }

    // Quantity (base unit) times the cost per base unit of the last purchase; zero when never bought.
    private static BigDecimal estimateCost(PurchaseDetail lastDetail, BigDecimal quantity) {
        if (lastDetail == null || lastDetail.getBaseQuantity().signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal unitCost = lastDetail.getSubtotal().divide(lastDetail.getBaseQuantity(), 4, RoundingMode.HALF_UP);
        return unitCost.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    private static void refreshTotals(ShoppingList list) {
        List<ShoppingListDetail> details = list.getDetails();
        list.setEstimatedCost(details.stream()
                .map(ShoppingListDetail::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        long purchased = details.stream().filter(ShoppingListDetail::getPurchased).count();
        if (details.isEmpty()) {
            list.setStatus(ShoppingListStatus.CANCELLED);
        } else if (purchased == details.size()) {
            list.setStatus(ShoppingListStatus.COMPLETED);
        } else if (purchased > 0) {
            list.setStatus(ShoppingListStatus.PARTIAL);
        } else {
            list.setStatus(ShoppingListStatus.PENDING);
        }
    }

    private Map<Long, BigDecimal> stockByProduct() {
        return inventoryMovementRepository.findStockByProduct().stream()
                .collect(Collectors.toMap(ProductStockView::getProductId, ProductStockView::getStock));
    }
}

package pe.edu.utp.ebenezer.service.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementRequest;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.StockStatus;
import pe.edu.utp.ebenezer.domain.entity.BusinessSettings;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotStockView;
import pe.edu.utp.ebenezer.domain.repository.inventory.ProductStockView;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.settings.BusinessSettingsRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Set<InventoryMovementType> WITHDRAWAL_TYPES =
            Set.of(InventoryMovementType.WASTE, InventoryMovementType.RETURN);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private final InventoryMovementRepository inventoryMovementRepository;
    private final LotRepository lotRepository;
    private final ProductRepository productRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final BusinessSettingsRepository businessSettingsRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockResponse> findStock() {
        LocalDate today = LocalDate.now();
        LocalDate warningLimit = today.plusDays(expirationWarningDays());

        Map<Long, BigDecimal> stockByProduct = inventoryMovementRepository.findStockByProduct().stream()
                .collect(Collectors.toMap(ProductStockView::getProductId, ProductStockView::getStock));
        Map<Long, List<LotStockView>> lotsByProduct = inventoryMovementRepository.findLotsWithStock().stream()
                .collect(Collectors.groupingBy(LotStockView::getProductId));
        Map<Long, PurchaseDetail> lastDetailByProduct = purchaseDetailRepository
                .findLatestByProduct(PurchaseStatus.REGISTERED).stream()
                .collect(Collectors.toMap(detail -> detail.getProduct().getId(), Function.identity()));

        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(product -> toStockResponse(
                        product,
                        stockByProduct.getOrDefault(product.getId(), BigDecimal.ZERO),
                        lotsByProduct.getOrDefault(product.getId(), List.of()),
                        lastDetailByProduct.get(product.getId()),
                        today,
                        warningLimit))
                .toList();
    }

    @Override
    @Transactional
    public List<InventoryMovementResponse> registerWithdrawal(InventoryMovementRequest request) {
        if (!WITHDRAWAL_TYPES.contains(request.movementType())) {
            throw new BusinessException("Movement type not allowed: " + request.movementType());
        }
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<LotAllocation> allocations = request.lotId() != null
                ? allocateFromLot(product, request.lotId(), request.quantity())
                : allocateFefo(product, request.quantity());

        User user = currentUserProvider.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        List<InventoryMovement> movements = allocations.stream()
                .map(allocation -> {
                    InventoryMovement movement = new InventoryMovement();
                    movement.setProduct(product);
                    movement.setLot(allocation.lot());
                    movement.setUser(user);
                    movement.setMovementType(request.movementType());
                    movement.setBaseQuantity(allocation.quantity().negate());
                    movement.setMovementDate(now);
                    movement.setReason(trimToNull(request.reason()));
                    movement.setNotes(trimToNull(request.notes()));
                    return movement;
                })
                .toList();

        return inventoryMovementRepository.saveAll(movements).stream()
                .map(InventoryMapper::toResponse)
                .toList();
    }

    private ProductStockResponse toStockResponse(Product product, BigDecimal stock, List<LotStockView> lots,
            PurchaseDetail lastDetail, LocalDate today, LocalDate warningLimit) {
        LocalDate nextExpiration = lots.stream()
                .map(LotStockView::getExpirationDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
        BigDecimal expiredQuantity = lots.stream()
                .filter(lot -> lot.getExpirationDate() != null && lot.getExpirationDate().isBefore(today))
                .map(LotStockView::getStock)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastUnitCost = null;
        String lastSupplierName = null;
        if (lastDetail != null) {
            lastUnitCost = lastDetail.getSubtotal().divide(lastDetail.getBaseQuantity(), 4, RoundingMode.HALF_UP);
            if (lastDetail.getPurchase().getSupplier() != null) {
                lastSupplierName = lastDetail.getPurchase().getSupplier().getName();
            }
        }

        return new ProductStockResponse(
                product.getId(),
                product.getName(),
                product.getBarcode(),
                product.getCategory().getName(),
                product.getBaseUnit().getId(),
                product.getBaseUnit().getAbbreviation(),
                product.getBaseUnit().getType(),
                stock,
                product.getMinStock(),
                product.getSalePrice(),
                lastUnitCost,
                lastSupplierName,
                nextExpiration,
                expiredQuantity,
                resolveStatus(stock, product.getMinStock(), nextExpiration, expiredQuantity, warningLimit));
    }

    // Same priority as the prototype; thresholds from AGENTS/database.md §18.
    private static StockStatus resolveStatus(BigDecimal stock, BigDecimal minStock, LocalDate nextExpiration,
            BigDecimal expiredQuantity, LocalDate warningLimit) {
        if (expiredQuantity.signum() > 0) {
            return StockStatus.EXPIRED;
        }
        if (stock.compareTo(minStock.divide(TWO)) <= 0) {
            return StockStatus.CRITICAL;
        }
        if (nextExpiration != null && !nextExpiration.isAfter(warningLimit)) {
            return StockStatus.EXPIRING_SOON;
        }
        if (stock.compareTo(minStock) <= 0) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }

    private List<LotAllocation> allocateFromLot(Product product, Long lotId, BigDecimal quantity) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
        if (!Objects.equals(lot.getProduct().getId(), product.getId())) {
            throw new BusinessException("Lot does not belong to the product");
        }
        if (quantity.compareTo(inventoryMovementRepository.sumBaseQuantityByLotId(lotId)) > 0) {
            throw new BusinessException("Not enough stock");
        }
        return List.of(new LotAllocation(lot, quantity));
    }

    // FEFO: lots come ordered by expiration date, so expired lots are withdrawn first.
    private List<LotAllocation> allocateFefo(Product product, BigDecimal quantity) {
        List<LotStockView> lots = inventoryMovementRepository.findLotsWithStockByProductId(product.getId());
        BigDecimal available = lots.stream().map(LotStockView::getStock).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (quantity.compareTo(available) > 0) {
            throw new BusinessException("Not enough stock");
        }

        List<LotAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = quantity;
        for (LotStockView lot : lots) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal taken = remaining.min(lot.getStock());
            allocations.add(new LotAllocation(lotRepository.getReferenceById(lot.getLotId()), taken));
            remaining = remaining.subtract(taken);
        }
        return allocations;
    }

    private int expirationWarningDays() {
        return businessSettingsRepository.findAll().stream()
                .findFirst()
                .map(BusinessSettings::getExpirationWarningDays)
                .orElseGet(() -> new BusinessSettings().getExpirationWarningDays());
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record LotAllocation(Lot lot, BigDecimal quantity) {
    }
}

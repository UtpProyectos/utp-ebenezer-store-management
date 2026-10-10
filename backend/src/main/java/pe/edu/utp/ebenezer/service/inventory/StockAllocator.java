package pe.edu.utp.ebenezer.service.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotStockView;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

/**
 * Decides which lots an outgoing quantity is taken from (AGENTS/database.md §8). Shared by withdrawals,
 * sales and internal consumption. Call it inside the caller's transaction.
 */
@Component
@RequiredArgsConstructor
public class StockAllocator {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final LotRepository lotRepository;

    /**
     * FEFO: lots with the nearest expiration first, then lots without expiration, then stock that entered
     * without a lot (e.g. initial adjustments). Expired lots are skipped unless {@code includeExpired}.
     */
    public List<StockAllocation> allocate(Product product, BigDecimal quantity, boolean includeExpired) {
        LocalDate today = LocalDate.now();
        List<LotStockView> lots = inventoryMovementRepository.findLotsWithStockByProductId(product.getId()).stream()
                .filter(lot -> includeExpired || !isExpired(lot, today))
                .toList();
        BigDecimal unlotted = Objects.requireNonNullElse(
                inventoryMovementRepository.sumUnlottedBaseQuantityByProductId(product.getId()), BigDecimal.ZERO)
                .max(BigDecimal.ZERO);
        BigDecimal available = lots.stream().map(LotStockView::getStock).reduce(unlotted, BigDecimal::add);
        if (quantity.compareTo(available) > 0) {
            throw notEnoughStock(product);
        }

        List<StockAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = quantity;
        for (LotStockView lot : lots) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal taken = remaining.min(lot.getStock());
            allocations.add(new StockAllocation(lotRepository.getReferenceById(lot.getLotId()), taken));
            remaining = remaining.subtract(taken);
        }
        if (remaining.signum() > 0) {
            allocations.add(new StockAllocation(null, remaining));
        }
        return allocations;
    }

    /** Takes the whole quantity from one lot of the product. */
    public List<StockAllocation> allocateFromLot(Product product, Long lotId, BigDecimal quantity) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
        if (!Objects.equals(lot.getProduct().getId(), product.getId())) {
            throw new BusinessException("Lot does not belong to the product");
        }
        if (quantity.compareTo(inventoryMovementRepository.sumBaseQuantityByLotId(lotId)) > 0) {
            throw notEnoughStock(product);
        }
        return List.of(new StockAllocation(lot, quantity));
    }

    private static boolean isExpired(LotStockView lot, LocalDate today) {
        return lot.getExpirationDate() != null && lot.getExpirationDate().isBefore(today);
    }

    private static BusinessException notEnoughStock(Product product) {
        return new BusinessException("Not enough stock for " + product.getName());
    }
}

package pe.edu.utp.ebenezer.service.consumption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionDetailRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.domain.entity.InternalConsumption;
import pe.edu.utp.ebenezer.domain.entity.InternalConsumptionDetail;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.repository.consumption.InternalConsumptionRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.inventory.StockAllocation;
import pe.edu.utp.ebenezer.service.inventory.StockAllocator;
import pe.edu.utp.ebenezer.service.unit.UnitConverter;

@Service
@RequiredArgsConstructor
public class InternalConsumptionServiceImpl implements InternalConsumptionService {

    private final InternalConsumptionRepository internalConsumptionRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final StockAllocator stockAllocator;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public InternalConsumptionResponse create(InternalConsumptionRequest request) {
        rejectRepeatedProducts(request.details());
        User user = currentUserProvider.getCurrentUser();
        LocalDateTime consumptionDate = request.consumptionDate() != null
                ? request.consumptionDate()
                : LocalDateTime.now();
        prefetchProductsAndUnits(request.details());

        InternalConsumption consumption = new InternalConsumption();
        consumption.setUser(user);
        consumption.setConsumptionDate(consumptionDate);
        consumption.setReason(trimToNull(request.reason()));
        consumption.setNotes(trimToNull(request.notes()));

        List<DetailStock> detailStock = new ArrayList<>();
        for (InternalConsumptionDetailRequest detailRequest : request.details()) {
            InternalConsumptionDetail detail = buildDetail(detailRequest);
            // Same rule as sales: expired lots are not consumed, they are withdrawn as waste.
            List<StockAllocation> allocations =
                    stockAllocator.allocate(detail.getProduct(), detail.getBaseQuantity(), false);
            detail.setInternalConsumption(consumption);
            consumption.getDetails().add(detail);
            detailStock.add(new DetailStock(detail, allocations));
        }

        InternalConsumption saved = internalConsumptionRepository.save(consumption);
        registerMovements(detailStock, user, consumptionDate);
        return InternalConsumptionMapper.toResponse(saved);
    }

    // Each product once per consumption, so the same stock is never allocated twice.
    private static void rejectRepeatedProducts(List<InternalConsumptionDetailRequest> details) {
        Set<Long> seen = new HashSet<>();
        for (InternalConsumptionDetailRequest detail : details) {
            if (!seen.add(detail.productId())) {
                throw new BusinessException("Product is repeated in the consumption");
            }
        }
    }

    private void prefetchProductsAndUnits(List<InternalConsumptionDetailRequest> details) {
        productRepository.findAllById(details.stream().map(InternalConsumptionDetailRequest::productId).toList());
        unitOfMeasureRepository.findAllById(
                details.stream().map(InternalConsumptionDetailRequest::unitOfMeasureId).distinct().toList());
    }

    private InternalConsumptionDetail buildDetail(InternalConsumptionDetailRequest request) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        UnitOfMeasure unit = unitOfMeasureRepository.findById(request.unitOfMeasureId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found"));
        BigDecimal baseQuantity = UnitConverter.toBaseQuantity(request.quantity(), unit, product.getBaseUnit());

        InternalConsumptionDetail detail = new InternalConsumptionDetail();
        detail.setProduct(product);
        detail.setUnitOfMeasure(unit);
        detail.setQuantity(request.quantity());
        detail.setBaseQuantity(baseQuantity);
        return detail;
    }

    private void registerMovements(List<DetailStock> detailStock, User user, LocalDateTime consumptionDate) {
        List<InventoryMovement> movements = new ArrayList<>();
        for (DetailStock entry : detailStock) {
            for (StockAllocation allocation : entry.allocations()) {
                InventoryMovement movement = new InventoryMovement();
                movement.setProduct(entry.detail().getProduct());
                movement.setLot(allocation.lot());
                movement.setUser(user);
                movement.setMovementType(InventoryMovementType.INTERNAL_CONSUMPTION);
                movement.setBaseQuantity(allocation.quantity().negate());
                movement.setInternalConsumptionDetail(entry.detail());
                movement.setMovementDate(consumptionDate);
                movements.add(movement);
            }
        }
        inventoryMovementRepository.saveAll(movements);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record DetailStock(InternalConsumptionDetail detail, List<StockAllocation> allocations) {
    }
}

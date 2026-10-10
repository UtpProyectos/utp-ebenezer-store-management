package pe.edu.utp.ebenezer.service.purchase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseDetailRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseRequest;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Purchase;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.PurchaseStatus;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.LotRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.purchase.PurchaseRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.unit.UnitConverter;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final LotRepository lotRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final SupplierRepository supplierRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        User user = currentUserProvider.getCurrentUser();
        LocalDateTime purchaseDate = request.purchaseDate() != null ? request.purchaseDate() : LocalDateTime.now();

        Purchase purchase = new Purchase();
        purchase.setSupplier(request.supplierId() == null ? null : getSupplier(request.supplierId()));
        purchase.setUser(user);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setStatus(PurchaseStatus.REGISTERED);
        purchase.setNotes(trimToNull(request.notes()));

        prefetchProductsAndUnits(request.details());

        List<LotData> lotData = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseDetailRequest detailRequest : request.details()) {
            PurchaseDetail detail = buildDetail(detailRequest);
            detail.setPurchase(purchase);
            purchase.getDetails().add(detail);
            lotData.add(new LotData(detail, trimToNull(detailRequest.lotCode()), detailRequest.expirationDate()));
            total = total.add(detail.getSubtotal());
        }
        purchase.setSubtotal(total);
        purchase.setTotal(total);

        Purchase saved = purchaseRepository.save(purchase);
        registerLotsAndMovements(lotData, user, purchaseDate);
        return PurchaseMapper.toResponse(saved);
    }

    // Loads every product and unit of the purchase in two queries; the per-line findById calls in
    // buildDetail are then served from the persistence context, keeping the same validation order.
    private void prefetchProductsAndUnits(List<PurchaseDetailRequest> details) {
        productRepository.findAllById(details.stream().map(PurchaseDetailRequest::productId).distinct().toList());
        unitOfMeasureRepository.findAllById(
                details.stream().map(PurchaseDetailRequest::unitOfMeasureId).distinct().toList());
    }

    private PurchaseDetail buildDetail(PurchaseDetailRequest request) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        UnitOfMeasure unit = unitOfMeasureRepository.findById(request.unitOfMeasureId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found"));

        BigDecimal baseQuantity = UnitConverter.toBaseQuantity(request.quantity(), unit, product.getBaseUnit());
        if (request.salePrice() != null) {
            product.setSalePrice(request.salePrice());
        }

        PurchaseDetail detail = new PurchaseDetail();
        detail.setProduct(product);
        detail.setUnitOfMeasure(unit);
        detail.setQuantity(request.quantity());
        detail.setBaseQuantity(baseQuantity);
        detail.setUnitCost(request.subtotal().divide(request.quantity(), 4, RoundingMode.HALF_UP));
        detail.setSubtotal(request.subtotal().setScale(2, RoundingMode.HALF_UP));
        return detail;
    }

    private void registerLotsAndMovements(List<LotData> lotData, User user, LocalDateTime purchaseDate) {
        List<Lot> lots = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();
        for (LotData data : lotData) {
            PurchaseDetail detail = data.detail();

            Lot lot = new Lot();
            lot.setProduct(detail.getProduct());
            lot.setPurchaseDetail(detail);
            lot.setLotCode(data.lotCode());
            lot.setEntryDate(purchaseDate);
            lot.setExpirationDate(data.expirationDate());
            lots.add(lot);

            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(detail.getProduct());
            movement.setLot(lot);
            movement.setUser(user);
            movement.setMovementType(InventoryMovementType.PURCHASE);
            movement.setBaseQuantity(detail.getBaseQuantity());
            movement.setPurchaseDetail(detail);
            movement.setMovementDate(purchaseDate);
            movements.add(movement);
        }
        lotRepository.saveAll(lots);
        inventoryMovementRepository.saveAll(movements);
    }

    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .filter(Supplier::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record LotData(PurchaseDetail detail, String lotCode, LocalDate expirationDate) {
    }
}

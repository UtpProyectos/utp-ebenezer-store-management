package pe.edu.utp.ebenezer.service.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Promotion;
import pe.edu.utp.ebenezer.domain.entity.Sale;
import pe.edu.utp.ebenezer.domain.entity.SaleDetail;
import pe.edu.utp.ebenezer.domain.entity.SaleHistory;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.entity.User;
import pe.edu.utp.ebenezer.domain.enums.InventoryMovementType;
import pe.edu.utp.ebenezer.domain.enums.SaleHistoryAction;
import pe.edu.utp.ebenezer.domain.enums.SaleStatus;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleHistoryRepository;
import pe.edu.utp.ebenezer.domain.repository.sale.SaleRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;
import pe.edu.utp.ebenezer.security.CurrentUserProvider;
import pe.edu.utp.ebenezer.service.inventory.StockAllocation;
import pe.edu.utp.ebenezer.service.inventory.StockAllocator;
import pe.edu.utp.ebenezer.service.promotion.PromotionService;
import pe.edu.utp.ebenezer.service.unit.UnitConverter;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleHistoryRepository saleHistoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final PromotionService promotionService;
    private final StockAllocator stockAllocator;
    private final CurrentUserProvider currentUserProvider;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public SaleResponse create(SaleRequest request) {
        rejectRepeatedProducts(request.details());
        User user = currentUserProvider.getCurrentUser();
        LocalDateTime saleDate = LocalDateTime.now();
        prefetchProductsAndUnits(request.details());
        Map<Long, Promotion> promotions = promotionService.findCurrentByProduct(saleDate);

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setSaleDate(saleDate);
        sale.setPaymentMethod(request.paymentMethod());
        sale.setStatus(SaleStatus.CONFIRMED);

        List<DetailStock> detailStock = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleDetailRequest detailRequest : request.details()) {
            SaleDetail detail = buildDetail(detailRequest, promotions);
            // Expired lots are never sold.
            List<StockAllocation> allocations =
                    stockAllocator.allocate(detail.getProduct(), detail.getBaseQuantity(), false);
            detail.setSale(sale);
            sale.getDetails().add(detail);
            detailStock.add(new DetailStock(detail, allocations));
            subtotal = subtotal.add(detail.getSubtotal());
        }

        BigDecimal discount = SalePricing.money(nonNull(request.discount()));
        if (discount.compareTo(subtotal) > 0) {
            throw new BusinessException("Discount cannot be greater than the subtotal");
        }
        sale.setSubtotal(subtotal);
        sale.setDiscount(discount);
        sale.setTotal(subtotal.subtract(discount));

        Sale saved = saleRepository.save(sale);
        registerMovements(detailStock, user, saleDate);

        SaleResponse response = SaleMapper.toResponse(saved);
        registerHistory(saved, user, response);
        return response;
    }

    // Each product once per sale, so the same stock is never allocated twice.
    private static void rejectRepeatedProducts(List<SaleDetailRequest> details) {
        Set<Long> seen = new HashSet<>();
        for (SaleDetailRequest detail : details) {
            if (!seen.add(detail.productId())) {
                throw new BusinessException("Product is repeated in the sale");
            }
        }
    }

    // Loads every product and unit in two queries; the per-line findById calls are then served from
    // the persistence context.
    private void prefetchProductsAndUnits(List<SaleDetailRequest> details) {
        productRepository.findAllById(details.stream().map(SaleDetailRequest::productId).toList());
        unitOfMeasureRepository.findAllById(
                details.stream().map(SaleDetailRequest::unitOfMeasureId).distinct().toList());
    }

    private SaleDetail buildDetail(SaleDetailRequest request, Map<Long, Promotion> promotions) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        UnitOfMeasure unit = unitOfMeasureRepository.findById(request.unitOfMeasureId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found"));
        BigDecimal baseQuantity = UnitConverter.toBaseQuantity(request.quantity(), unit, product.getBaseUnit());

        SalePricing.LinePrice price = SalePricing.price(
                baseQuantity, product.getSalePrice(), promotions.get(product.getId()), product.getBaseUnit());
        BigDecimal discount = price.promotionDiscount().add(SalePricing.money(nonNull(request.discount())));
        if (discount.compareTo(price.gross()) > 0) {
            throw new BusinessException("Discount cannot be greater than the line amount");
        }

        SaleDetail detail = new SaleDetail();
        detail.setProduct(product);
        detail.setUnitOfMeasure(unit);
        detail.setQuantity(request.quantity());
        detail.setBaseQuantity(baseQuantity);
        detail.setUnitPrice(UnitConverter.convertPrice(product.getSalePrice(), unit, product.getBaseUnit()));
        detail.setDiscount(discount);
        detail.setSubtotal(price.gross().subtract(discount));
        return detail;
    }

    private void registerMovements(List<DetailStock> detailStock, User user, LocalDateTime saleDate) {
        List<InventoryMovement> movements = new ArrayList<>();
        for (DetailStock entry : detailStock) {
            for (StockAllocation allocation : entry.allocations()) {
                InventoryMovement movement = new InventoryMovement();
                movement.setProduct(entry.detail().getProduct());
                movement.setLot(allocation.lot());
                movement.setUser(user);
                movement.setMovementType(InventoryMovementType.SALE);
                movement.setBaseQuantity(allocation.quantity().negate());
                movement.setSaleDetail(entry.detail());
                movement.setMovementDate(saleDate);
                movements.add(movement);
            }
        }
        inventoryMovementRepository.saveAll(movements);
    }

    private void registerHistory(Sale sale, User user, SaleResponse response) {
        SaleHistory history = new SaleHistory();
        history.setSale(sale);
        history.setUser(user);
        history.setAction(SaleHistoryAction.CREATED);
        history.setNewData(jsonMapper.writeValueAsString(response));
        saleHistoryRepository.save(history);
    }

    private static BigDecimal nonNull(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO);
    }

    private record DetailStock(SaleDetail detail, List<StockAllocation> allocations) {
    }
}

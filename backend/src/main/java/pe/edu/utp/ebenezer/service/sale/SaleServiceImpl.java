package pe.edu.utp.ebenezer.service.sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.sale.SaleCancelRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailUpdateRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleHistoryResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleRequest;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleUpdateRequest;
import pe.edu.utp.ebenezer.domain.entity.InventoryMovement;
import pe.edu.utp.ebenezer.domain.entity.Lot;
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
        registerSaleMovements(detailStock, user, saleDate);

        SaleResponse response = SaleMapper.toResponse(saved);
        registerHistory(saved, user, SaleHistoryAction.CREATED, null, response, null);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findByDate(LocalDate date) {
        return saleRepository.findBySaleDateGreaterThanEqualAndSaleDateLessThanOrderBySaleDateDesc(
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .map(SaleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleHistoryResponse> findChanges(LocalDate date) {
        return saleHistoryRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndActionNotOrderByCreatedAtDesc(
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay(), SaleHistoryAction.CREATED).stream()
                .map(SaleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SaleResponse update(Long id, SaleUpdateRequest request) {
        Sale sale = getActiveSale(id);
        String previousData = toJson(SaleMapper.toResponse(sale));
        Map<Long, BigDecimal> newQuantities = newQuantitiesByDetail(sale, request.details());

        List<SaleDetail> changed = sale.getDetails().stream()
                .filter(detail -> detail.getQuantity().compareTo(newQuantities.get(detail.getId())) != 0)
                .toList();
        if (changed.isEmpty() && sale.getPaymentMethod() == request.paymentMethod()) {
            throw new BusinessException("The sale has no changes");
        }
        if (newQuantities.values().stream().allMatch(quantity -> quantity.signum() == 0)) {
            throw new BusinessException("A sale needs at least one product; cancel it instead");
        }

        User user = currentUserProvider.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        String movementReason = "Sale #" + sale.getId() + " edited";
        // Undo the stock of the changed lines first, so their own units are available again.
        reverseMovements(sale, changed, user, now, movementReason);

        Map<Long, Promotion> promotions = promotionService.findCurrentByProduct(now);
        List<DetailStock> detailStock = new ArrayList<>();
        for (SaleDetail detail : changed) {
            List<StockAllocation> allocations = repriceDetail(detail, newQuantities.get(detail.getId()), promotions);
            detailStock.add(new DetailStock(detail, allocations));
        }
        registerSaleMovements(detailStock, user, now);

        BigDecimal subtotal = sale.getDetails().stream()
                .map(SaleDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // The header discount is kept while it still fits in the new subtotal.
        sale.setDiscount(sale.getDiscount().min(subtotal));
        sale.setSubtotal(subtotal);
        sale.setTotal(subtotal.subtract(sale.getDiscount()));
        sale.setPaymentMethod(request.paymentMethod());
        sale.setStatus(SaleStatus.EDITED);

        Sale saved = saleRepository.save(sale);
        SaleResponse response = SaleMapper.toResponse(saved);
        registerHistory(saved, user, SaleHistoryAction.EDITED, previousData, response, request.reason().trim());
        return response;
    }

    @Override
    @Transactional
    public SaleResponse cancel(Long id, SaleCancelRequest request) {
        Sale sale = getActiveSale(id);
        String previousData = toJson(SaleMapper.toResponse(sale));
        User user = currentUserProvider.getCurrentUser();
        String reason = request.reason().trim();

        reverseMovements(sale, sale.getDetails(), user, LocalDateTime.now(), "Sale #" + sale.getId() + " cancelled");
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancellationReason(reason);

        Sale saved = saleRepository.save(sale);
        SaleResponse response = SaleMapper.toResponse(saved);
        registerHistory(saved, user, SaleHistoryAction.CANCELLED, previousData, response, reason);
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

    private Sale getActiveSale(Long id) {
        Sale sale = saleRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessException("The sale is cancelled");
        }
        return sale;
    }

    // New quantity of every line: the requested one, or the current one when the line is not listed.
    private static Map<Long, BigDecimal> newQuantitiesByDetail(Sale sale, List<SaleDetailUpdateRequest> requests) {
        Map<Long, BigDecimal> quantities = new LinkedHashMap<>();
        sale.getDetails().forEach(detail -> quantities.put(detail.getId(), detail.getQuantity()));
        Set<Long> seen = new HashSet<>();
        for (SaleDetailUpdateRequest request : requests) {
            if (!quantities.containsKey(request.saleDetailId())) {
                throw new BusinessException("Line " + request.saleDetailId() + " does not belong to the sale");
            }
            if (!seen.add(request.saleDetailId())) {
                throw new BusinessException("Line is repeated in the request");
            }
            quantities.put(request.saleDetailId(), request.quantity());
        }
        return quantities;
    }

    // Recalculates a line with the price it was sold at (unit_price) and the promotion valid now.
    private List<StockAllocation> repriceDetail(SaleDetail detail, BigDecimal quantity,
            Map<Long, Promotion> promotions) {
        detail.setQuantity(quantity);
        if (quantity.signum() == 0) {
            detail.setBaseQuantity(BigDecimal.ZERO);
            detail.setDiscount(BigDecimal.ZERO);
            detail.setSubtotal(BigDecimal.ZERO);
            return List.of();
        }
        Product product = detail.getProduct();
        UnitOfMeasure baseUnit = product.getBaseUnit();
        BigDecimal baseQuantity = UnitConverter.toBaseQuantity(quantity, detail.getUnitOfMeasure(), baseUnit);
        BigDecimal basePrice = UnitConverter.convertPrice(detail.getUnitPrice(), baseUnit, detail.getUnitOfMeasure());
        SalePricing.LinePrice price = SalePricing.price(baseQuantity, basePrice, promotions.get(product.getId()), baseUnit);

        detail.setBaseQuantity(baseQuantity);
        detail.setDiscount(price.promotionDiscount());
        detail.setSubtotal(price.gross().subtract(price.promotionDiscount()));
        return stockAllocator.allocate(product, baseQuantity, false);
    }

    /**
     * Gives back the stock of the given lines: one positive REVERSAL per lot with the net quantity still
     * taken by the line (SALE minus earlier reversals), so the Kardex stays complete.
     */
    private void reverseMovements(Sale sale, List<SaleDetail> details, User user, LocalDateTime date, String reason) {
        if (details.isEmpty()) {
            return;
        }
        Map<Long, SaleDetail> detailsById = new HashMap<>();
        details.forEach(detail -> detailsById.put(detail.getId(), detail));

        Map<ReversalKey, BigDecimal> netByLot = new LinkedHashMap<>();
        Map<ReversalKey, Lot> lots = new HashMap<>();
        for (InventoryMovement movement : inventoryMovementRepository.findBySaleId(sale.getId())) {
            Long detailId = movement.getSaleDetail().getId();
            if (!detailsById.containsKey(detailId)) {
                continue;
            }
            ReversalKey key = new ReversalKey(detailId, movement.getLot() == null ? null : movement.getLot().getId());
            netByLot.merge(key, movement.getBaseQuantity(), BigDecimal::add);
            lots.putIfAbsent(key, movement.getLot());
        }

        List<InventoryMovement> reversals = new ArrayList<>();
        netByLot.forEach((key, net) -> {
            if (net.signum() >= 0) {
                return;
            }
            SaleDetail detail = detailsById.get(key.saleDetailId());
            InventoryMovement reversal = new InventoryMovement();
            reversal.setProduct(detail.getProduct());
            reversal.setLot(lots.get(key));
            reversal.setUser(user);
            reversal.setMovementType(InventoryMovementType.REVERSAL);
            reversal.setBaseQuantity(net.negate());
            reversal.setSaleDetail(detail);
            reversal.setMovementDate(date);
            reversal.setReason(reason);
            reversals.add(reversal);
        });
        inventoryMovementRepository.saveAll(reversals);
    }

    private void registerSaleMovements(List<DetailStock> detailStock, User user, LocalDateTime date) {
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
                movement.setMovementDate(date);
                movements.add(movement);
            }
        }
        inventoryMovementRepository.saveAll(movements);
    }

    private void registerHistory(Sale sale, User user, SaleHistoryAction action, String previousData,
            SaleResponse response, String reason) {
        SaleHistory history = new SaleHistory();
        history.setSale(sale);
        history.setUser(user);
        history.setAction(action);
        history.setPreviousData(previousData);
        history.setNewData(toJson(response));
        history.setReason(reason);
        saleHistoryRepository.save(history);
    }

    private String toJson(SaleResponse response) {
        return jsonMapper.writeValueAsString(response);
    }

    private static BigDecimal nonNull(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO);
    }

    private record DetailStock(SaleDetail detail, List<StockAllocation> allocations) {
    }

    private record ReversalKey(Long saleDetailId, Long lotId) {
    }
}

package pe.edu.utp.ebenezer.service.shoppinglist;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailResponse;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListResponse;
import pe.edu.utp.ebenezer.domain.entity.ShoppingList;
import pe.edu.utp.ebenezer.domain.entity.ShoppingListDetail;

public final class ShoppingListMapper {

    private ShoppingListMapper() {
    }

    /** stockByProduct: current stock (base unit) of each product; missing products have no stock. */
    public static ShoppingListResponse toResponse(ShoppingList list, Map<Long, BigDecimal> stockByProduct) {
        return new ShoppingListResponse(
                list.getId(),
                list.getUser().getId(),
                list.getUser().getName(),
                list.getCreatedAt(),
                list.getSource(),
                list.getStatus(),
                list.getEstimatedCost(),
                // Insertion order: the details collection itself has no defined order.
                list.getDetails().stream()
                        .sorted(Comparator.comparing(ShoppingListDetail::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(detail -> toResponse(detail,
                                stockByProduct.getOrDefault(detail.getProduct().getId(), BigDecimal.ZERO)))
                        .toList());
    }

    public static ShoppingListDetailResponse toResponse(ShoppingListDetail detail, BigDecimal currentStock) {
        return new ShoppingListDetailResponse(
                detail.getId(),
                detail.getProduct().getId(),
                detail.getProduct().getName(),
                detail.getSupplier() == null ? null : detail.getSupplier().getId(),
                detail.getSupplier() == null ? null : detail.getSupplier().getName(),
                detail.getUnitOfMeasure().getId(),
                detail.getUnitOfMeasure().getAbbreviation(),
                currentStock,
                detail.getSuggestedQuantity(),
                detail.getPurchasedQuantity(),
                detail.getEstimatedCost(),
                detail.getPurchased());
    }
}

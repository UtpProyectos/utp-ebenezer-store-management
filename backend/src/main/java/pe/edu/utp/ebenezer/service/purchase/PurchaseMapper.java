package pe.edu.utp.ebenezer.service.purchase;

import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseDetailResponse;
import pe.edu.utp.ebenezer.api.dto.purchase.PurchaseResponse;
import pe.edu.utp.ebenezer.domain.entity.Purchase;
import pe.edu.utp.ebenezer.domain.entity.PurchaseDetail;

public final class PurchaseMapper {

    private PurchaseMapper() {
    }

    public static PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getSupplier() == null ? null : purchase.getSupplier().getId(),
                purchase.getSupplier() == null ? null : purchase.getSupplier().getName(),
                purchase.getUser().getId(),
                purchase.getUser().getName(),
                purchase.getPurchaseDate(),
                purchase.getSubtotal(),
                purchase.getTotal(),
                purchase.getStatus(),
                purchase.getNotes(),
                purchase.getDetails().stream().map(PurchaseMapper::toResponse).toList());
    }

    public static PurchaseDetailResponse toResponse(PurchaseDetail detail) {
        return new PurchaseDetailResponse(
                detail.getId(),
                detail.getProduct().getId(),
                detail.getProduct().getName(),
                detail.getUnitOfMeasure().getId(),
                detail.getUnitOfMeasure().getAbbreviation(),
                detail.getQuantity(),
                detail.getBaseQuantity(),
                detail.getUnitCost(),
                detail.getSubtotal());
    }
}

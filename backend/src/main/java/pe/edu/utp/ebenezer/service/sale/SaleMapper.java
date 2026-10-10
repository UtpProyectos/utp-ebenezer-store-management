package pe.edu.utp.ebenezer.service.sale;

import pe.edu.utp.ebenezer.api.dto.sale.SaleDetailResponse;
import pe.edu.utp.ebenezer.api.dto.sale.SaleResponse;
import pe.edu.utp.ebenezer.domain.entity.Sale;
import pe.edu.utp.ebenezer.domain.entity.SaleDetail;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getUser().getId(),
                sale.getUser().getName(),
                sale.getSaleDate(),
                sale.getSubtotal(),
                sale.getDiscount(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getCancellationReason(),
                sale.getCreatedAt(),
                sale.getUpdatedAt(),
                sale.getDetails().stream().map(SaleMapper::toResponse).toList());
    }

    public static SaleDetailResponse toResponse(SaleDetail detail) {
        return new SaleDetailResponse(
                detail.getId(),
                detail.getProduct().getId(),
                detail.getProduct().getName(),
                detail.getUnitOfMeasure().getId(),
                detail.getUnitOfMeasure().getAbbreviation(),
                detail.getQuantity(),
                detail.getBaseQuantity(),
                detail.getUnitPrice(),
                detail.getDiscount(),
                detail.getSubtotal());
    }
}

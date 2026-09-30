package pe.edu.utp.ebenezer.api.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import pe.edu.utp.ebenezer.domain.enums.PaymentMethod;
import pe.edu.utp.ebenezer.domain.enums.SaleStatus;

public record SaleResponse(
        Long id,
        Long userId,
        String userName,
        LocalDateTime saleDate,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        PaymentMethod paymentMethod,
        SaleStatus status,
        String cancellationReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SaleDetailResponse> details
) {
}

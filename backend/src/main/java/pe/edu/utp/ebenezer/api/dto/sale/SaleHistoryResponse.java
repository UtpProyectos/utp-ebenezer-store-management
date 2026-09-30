package pe.edu.utp.ebenezer.api.dto.sale;

import java.time.LocalDateTime;

import pe.edu.utp.ebenezer.domain.enums.SaleHistoryAction;

public record SaleHistoryResponse(
        Long id,
        Long saleId,
        Long userId,
        String userName,
        SaleHistoryAction action,
        String previousData,
        String newData,
        String reason,
        LocalDateTime createdAt
) {
}

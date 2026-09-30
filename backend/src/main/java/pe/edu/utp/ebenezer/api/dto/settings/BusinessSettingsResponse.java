package pe.edu.utp.ebenezer.api.dto.settings;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BusinessSettingsResponse(
        Long id,
        String businessName,
        String ruc,
        String address,
        String phone,
        String currency,
        String logoUrl,
        BigDecimal defaultMinStock,
        Integer expirationWarningDays,
        Boolean criticalStockAlert,
        Boolean expirationAlert,
        Boolean autoPrintTicket,
        LocalDateTime updatedAt
) {
}

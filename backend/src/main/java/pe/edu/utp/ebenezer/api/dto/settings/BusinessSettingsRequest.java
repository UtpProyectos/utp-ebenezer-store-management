package pe.edu.utp.ebenezer.api.dto.settings;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BusinessSettingsRequest(
        @NotBlank @Size(max = 150) String businessName,
        @Size(max = 20) String ruc,
        @Size(max = 250) String address,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 10) String currency,
        @Size(max = 500) String logoUrl,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 3) BigDecimal defaultMinStock,
        @NotNull @Min(0) Integer expirationWarningDays,
        @NotNull Boolean criticalStockAlert,
        @NotNull Boolean expirationAlert,
        @NotNull Boolean autoPrintTicket
) {
}

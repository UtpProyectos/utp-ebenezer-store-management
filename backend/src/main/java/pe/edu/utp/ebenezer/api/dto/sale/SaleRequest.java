package pe.edu.utp.ebenezer.api.dto.sale;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import pe.edu.utp.ebenezer.domain.enums.PaymentMethod;

// Unit prices and totals are calculated by the service; the client never sends them.
public record SaleRequest(
        @NotNull PaymentMethod paymentMethod,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal discount,
        @NotEmpty List<@Valid SaleDetailRequest> details
) {
}

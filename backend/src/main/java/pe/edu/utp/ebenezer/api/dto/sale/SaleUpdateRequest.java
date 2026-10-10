package pe.edu.utp.ebenezer.api.dto.sale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.edu.utp.ebenezer.domain.enums.PaymentMethod;

// Correction of a registered sale. Lines not listed keep their quantity (an empty list only changes the payment
// method); prices are recalculated by the service.
public record SaleUpdateRequest(
        @NotNull PaymentMethod paymentMethod,
        @NotBlank @Size(max = 500) String reason,
        @NotNull List<@Valid SaleDetailUpdateRequest> details
) {
}

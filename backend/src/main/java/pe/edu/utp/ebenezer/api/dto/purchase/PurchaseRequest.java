package pe.edu.utp.ebenezer.api.dto.purchase;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// supplierId is optional (occasional purchases). purchaseDate defaults to now when null.
public record PurchaseRequest(
        Long supplierId,
        LocalDateTime purchaseDate,
        @Size(max = 500) String notes,
        @NotEmpty List<@Valid PurchaseDetailRequest> details
) {
}

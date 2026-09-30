package pe.edu.utp.ebenezer.api.dto.consumption;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// consumptionDate defaults to now when null.
public record InternalConsumptionRequest(
        LocalDateTime consumptionDate,
        @Size(max = 300) String reason,
        @Size(max = 500) String notes,
        @NotEmpty List<@Valid InternalConsumptionDetailRequest> details
) {
}

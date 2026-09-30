package pe.edu.utp.ebenezer.api.dto.consumption;

import java.time.LocalDateTime;
import java.util.List;

public record InternalConsumptionResponse(
        Long id,
        Long userId,
        String userName,
        LocalDateTime consumptionDate,
        String reason,
        String notes,
        List<InternalConsumptionDetailResponse> details
) {
}

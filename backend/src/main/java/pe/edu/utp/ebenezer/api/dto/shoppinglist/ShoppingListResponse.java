package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import pe.edu.utp.ebenezer.domain.enums.ShoppingListSource;
import pe.edu.utp.ebenezer.domain.enums.ShoppingListStatus;

public record ShoppingListResponse(
        Long id,
        Long userId,
        String userName,
        LocalDateTime createdAt,
        ShoppingListSource source,
        ShoppingListStatus status,
        BigDecimal estimatedCost,
        List<ShoppingListDetailResponse> details
) {
}

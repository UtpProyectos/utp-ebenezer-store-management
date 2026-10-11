package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.util.List;

import jakarta.validation.constraints.NotNull;

// productIds is optional: when empty, every active product at or below its minimum stock is added.
public record ShoppingListGenerateRequest(
        List<@NotNull Long> productIds
) {
}

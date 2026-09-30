package pe.edu.utp.ebenezer.api.dto.shoppinglist;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import pe.edu.utp.ebenezer.domain.enums.ShoppingListSource;

public record ShoppingListRequest(
        @NotNull ShoppingListSource source,
        @NotEmpty List<@Valid ShoppingListDetailRequest> details
) {
}

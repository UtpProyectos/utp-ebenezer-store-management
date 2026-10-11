package pe.edu.utp.ebenezer.service.shoppinglist;

import java.util.Optional;

import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailUpdateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListGenerateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListResponse;

public interface ShoppingListService {

    /** Latest shopping list, unless it was cancelled. */
    Optional<ShoppingListResponse> findCurrent();

    /**
     * Adds products to the open list (PENDING or PARTIAL), creating one when there is none: the given
     * products (MANUAL) or every active product at or below its minimum stock (SYSTEM). Products already
     * in the list are skipped.
     */
    ShoppingListResponse generate(ShoppingListGenerateRequest request);

    /** Changes the quantity to buy and/or marks the item as bought; recalculates totals and status. */
    ShoppingListResponse updateDetail(Long detailId, ShoppingListDetailUpdateRequest request);

    /** Removes an item from its list. A list left empty becomes CANCELLED. */
    ShoppingListResponse removeDetail(Long detailId);
}

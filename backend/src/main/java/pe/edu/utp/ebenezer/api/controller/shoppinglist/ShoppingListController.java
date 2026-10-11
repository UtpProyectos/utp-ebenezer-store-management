package pe.edu.utp.ebenezer.api.controller.shoppinglist;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListDetailUpdateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListGenerateRequest;
import pe.edu.utp.ebenezer.api.dto.shoppinglist.ShoppingListResponse;
import pe.edu.utp.ebenezer.service.shoppinglist.ShoppingListService;

// Any authenticated user (ADMIN or CASHIER).
@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    /** Latest list; 204 when there is none (or it was cancelled). */
    @GetMapping("/current")
    public ResponseEntity<ShoppingListResponse> findCurrent() {
        return shoppingListService.findCurrent()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingListResponse generate(@Valid @RequestBody(required = false) ShoppingListGenerateRequest request) {
        return shoppingListService.generate(request);
    }

    @PatchMapping("/details/{detailId}")
    public ShoppingListResponse updateDetail(
            @PathVariable Long detailId,
            @Valid @RequestBody ShoppingListDetailUpdateRequest request
    ) {
        return shoppingListService.updateDetail(detailId, request);
    }

    @DeleteMapping("/details/{detailId}")
    public ShoppingListResponse removeDetail(@PathVariable Long detailId) {
        return shoppingListService.removeDetail(detailId);
    }
}

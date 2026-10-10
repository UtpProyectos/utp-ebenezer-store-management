package pe.edu.utp.ebenezer.api.controller.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementRequest;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;

// Any authenticated user (ADMIN or CASHIER).
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<ProductStockResponse> findStock() {
        return inventoryService.findStock();
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InventoryMovementResponse> registerWithdrawal(@Valid @RequestBody InventoryMovementRequest request) {
        return inventoryService.registerWithdrawal(request);
    }
}

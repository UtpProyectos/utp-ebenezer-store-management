package pe.edu.utp.ebenezer.api.controller.inventory;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementRequest;
import pe.edu.utp.ebenezer.api.dto.inventory.InventoryMovementResponse;
import pe.edu.utp.ebenezer.api.dto.inventory.ProductStockResponse;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;

// Any authenticated user (ADMIN or CASHIER), except the movement history (admin only).
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<ProductStockResponse> findStock() {
        return inventoryService.findStock();
    }

    /** Movements of a period (from / to, both included; today by default). */
    @GetMapping("/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryMovementResponse> findMovements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate start = from != null ? from : LocalDate.now();
        return inventoryService.findMovements(start, to != null ? to : start);
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InventoryMovementResponse> registerWithdrawal(@Valid @RequestBody InventoryMovementRequest request) {
        return inventoryService.registerWithdrawal(request);
    }
}

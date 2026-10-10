package pe.edu.utp.ebenezer.api.controller.supplier;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierResponse;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierStatusRequest;
import pe.edu.utp.ebenezer.service.supplier.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public List<SupplierResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active
    ) {
        return supplierService.findAll(search, active);
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable Long id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SupplierStatusRequest request
    ) {
        return supplierService.updateStatus(id, request.active());
    }
}

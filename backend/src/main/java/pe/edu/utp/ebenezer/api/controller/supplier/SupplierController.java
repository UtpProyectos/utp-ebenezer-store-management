package pe.edu.utp.ebenezer.api.controller.supplier;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.supplier.SupplierService;

// TODO(team): expose the endpoints. Only delegate to SupplierService; never call repositories here.
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
}

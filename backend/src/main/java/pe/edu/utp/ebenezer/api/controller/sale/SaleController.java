package pe.edu.utp.ebenezer.api.controller.sale;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.sale.SaleService;

// TODO(team): expose the endpoints. Only delegate to SaleService; never call repositories here.
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
}

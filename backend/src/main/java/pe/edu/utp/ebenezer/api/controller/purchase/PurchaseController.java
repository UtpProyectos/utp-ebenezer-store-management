package pe.edu.utp.ebenezer.api.controller.purchase;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.purchase.PurchaseService;

// TODO(team): expose the endpoints. Only delegate to PurchaseService; never call repositories here.
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
}

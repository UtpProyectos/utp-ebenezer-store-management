package pe.edu.utp.ebenezer.api.controller.inventory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.inventory.InventoryService;

// TODO(team): expose the endpoints. Only delegate to InventoryService; never call repositories here.
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
}

package pe.edu.utp.ebenezer.api.controller.shoppinglist;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.service.shoppinglist.ShoppingListService;

// TODO(team): expose the endpoints. Only delegate to ShoppingListService; never call repositories here.
@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
}

package pe.edu.utp.ebenezer.service.shoppinglist;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.domain.repository.shoppinglist.ShoppingListDetailRepository;
import pe.edu.utp.ebenezer.domain.repository.shoppinglist.ShoppingListRepository;

@Service
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListDetailRepository shoppingListDetailRepository;
}

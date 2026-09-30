package pe.edu.utp.ebenezer.domain.repository.shoppinglist;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.ShoppingList;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
}

package pe.edu.utp.ebenezer.domain.repository.shoppinglist;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.ShoppingList;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    // Latest list with its details, products, units and suggested suppliers in one query.
    @EntityGraph(attributePaths = {
            "user", "details", "details.product", "details.product.baseUnit", "details.supplier",
            "details.unitOfMeasure"})
    Optional<ShoppingList> findFirstByOrderByIdDesc();

    // The list that contains a detail, loaded the same way, ready to update its totals and status.
    @EntityGraph(attributePaths = {
            "user", "details", "details.product", "details.product.baseUnit", "details.supplier",
            "details.unitOfMeasure"})
    @Query("""
            select l from ShoppingList l
            where exists (select d.id from ShoppingListDetail d where d.shoppingList = l and d.id = :detailId)""")
    Optional<ShoppingList> findByDetailId(@Param("detailId") Long detailId);
}

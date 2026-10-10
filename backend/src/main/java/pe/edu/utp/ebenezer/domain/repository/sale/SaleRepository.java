package pe.edu.utp.ebenezer.domain.repository.sale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Sales of a period with everything the list shows, in one query.
    @EntityGraph(attributePaths = {"user", "details", "details.product", "details.unitOfMeasure"})
    List<Sale> findBySaleDateGreaterThanEqualAndSaleDateLessThanOrderBySaleDateDesc(
            LocalDateTime from, LocalDateTime to);

    // A sale ready to be edited or cancelled: details with product, base unit and line unit.
    @EntityGraph(attributePaths = {
            "user", "details", "details.product", "details.product.baseUnit", "details.unitOfMeasure"})
    Optional<Sale> findWithDetailsById(Long id);
}

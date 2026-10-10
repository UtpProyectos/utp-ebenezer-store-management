package pe.edu.utp.ebenezer.domain.repository.supplier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import pe.edu.utp.ebenezer.domain.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @EntityGraph(attributePaths = "products")
    List<Supplier> findAllByOrderByNameAsc();

    Optional<Supplier> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

package pe.edu.utp.ebenezer.domain.repository.supplier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @EntityGraph(attributePaths = "products")
    @Query("""
            select s from Supplier s
            where (:search = ''
                or lower(s.name) like :search
                or lower(coalesce(s.phone, '')) like :search
                or lower(coalesce(s.contactName, '')) like :search)
              and (:active is null or s.active = :active)
            order by s.name asc""")
    List<Supplier> search(@Param("search") String search, @Param("active") Boolean active);

    @EntityGraph(attributePaths = "products")
    Optional<Supplier> findWithProductsById(Long id);

    Optional<Supplier> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

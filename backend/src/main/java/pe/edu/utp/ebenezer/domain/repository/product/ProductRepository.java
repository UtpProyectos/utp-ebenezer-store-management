package pe.edu.utp.ebenezer.domain.repository.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.ebenezer.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findByBarcodeAndIdNot(String barcode, Long id);

    boolean existsByBarcode(String barcode);

    @Query("""
            select p.id as id,
                   c.id as categoryId,
                   c.name as categoryName,
                   u.id as baseUnitId,
                   u.abbreviation as baseUnitAbbreviation,
                   p.name as name,
                   p.description as description,
                   p.barcode as barcode,
                   p.salePrice as salePrice,
                   p.minStock as minStock,
                   p.active as active,
                   p.createdAt as createdAt,
                   coalesce(
                       (select sum(m.baseQuantity)
                        from InventoryMovement m
                        where m.product.id = p.id),
                       :zero
                   ) as currentStock
            from Product p
            join p.category c
            join p.baseUnit u
            where (:search = ''
                or lower(p.name) like :search
                or lower(coalesce(p.barcode, '')) like :search)
              and (:categoryId is null or c.id = :categoryId)
              and (:active is null or p.active = :active)
            order by lower(p.name)
            """)
    List<ProductSummary> searchSummaries(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("active") Boolean active,
            @Param("zero") BigDecimal zero
    );

    interface ProductSummary {
        Long getId();

        Long getCategoryId();

        String getCategoryName();

        Long getBaseUnitId();

        String getBaseUnitAbbreviation();

        String getName();

        String getDescription();

        String getBarcode();

        BigDecimal getSalePrice();

        BigDecimal getMinStock();

        Boolean getActive();

        LocalDateTime getCreatedAt();

        BigDecimal getCurrentStock();
    }
}

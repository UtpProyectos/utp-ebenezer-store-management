package pe.edu.utp.ebenezer.domain.repository.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);
}

package pe.edu.utp.ebenezer.domain.repository.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByActive(Boolean active, Sort sort);
}

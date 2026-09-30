package pe.edu.utp.ebenezer.domain.repository.unit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation);
}

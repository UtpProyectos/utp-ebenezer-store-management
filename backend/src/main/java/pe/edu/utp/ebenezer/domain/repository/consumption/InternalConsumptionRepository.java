package pe.edu.utp.ebenezer.domain.repository.consumption;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.ebenezer.domain.entity.InternalConsumption;

public interface InternalConsumptionRepository extends JpaRepository<InternalConsumption, Long> {

    // Consumptions of a period with everything the history shows, in one query.
    @EntityGraph(attributePaths = {"user", "details", "details.product", "details.unitOfMeasure"})
    List<InternalConsumption> findByConsumptionDateGreaterThanEqualAndConsumptionDateLessThanOrderByConsumptionDateDesc(
            LocalDateTime from, LocalDateTime to);
}

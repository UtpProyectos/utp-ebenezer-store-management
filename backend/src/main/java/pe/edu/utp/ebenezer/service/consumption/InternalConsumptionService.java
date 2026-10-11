package pe.edu.utp.ebenezer.service.consumption;

import java.time.LocalDate;
import java.util.List;

import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;

public interface InternalConsumptionService {

    /** Consumptions registered between two days (both included), newest first. */
    List<InternalConsumptionResponse> findByPeriod(LocalDate from, LocalDate to);

    /**
     * Registers products taken for internal use: no income, FEFO over non-expired stock and one negative
     * INTERNAL_CONSUMPTION movement per lot.
     */
    InternalConsumptionResponse create(InternalConsumptionRequest request);
}

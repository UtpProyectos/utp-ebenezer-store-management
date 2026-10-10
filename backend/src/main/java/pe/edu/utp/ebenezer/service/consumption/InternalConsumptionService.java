package pe.edu.utp.ebenezer.service.consumption;

import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionRequest;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;

public interface InternalConsumptionService {

    /**
     * Registers products taken for internal use: no income, FEFO over non-expired stock and one negative
     * INTERNAL_CONSUMPTION movement per lot.
     */
    InternalConsumptionResponse create(InternalConsumptionRequest request);
}

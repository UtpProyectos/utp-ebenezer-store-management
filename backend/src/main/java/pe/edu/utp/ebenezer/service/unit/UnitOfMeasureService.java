package pe.edu.utp.ebenezer.service.unit;

import java.util.List;

import pe.edu.utp.ebenezer.api.dto.unit.UnitOfMeasureResponse;

public interface UnitOfMeasureService {

    List<UnitOfMeasureResponse> findAll();

    /**
     * Creates the base units of measure that are missing. Existing units are left untouched.
     */
    void ensureDefaultUnits();
}

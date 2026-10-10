package pe.edu.utp.ebenezer.service.consumption;

import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionDetailResponse;
import pe.edu.utp.ebenezer.api.dto.consumption.InternalConsumptionResponse;
import pe.edu.utp.ebenezer.domain.entity.InternalConsumption;
import pe.edu.utp.ebenezer.domain.entity.InternalConsumptionDetail;

public final class InternalConsumptionMapper {

    private InternalConsumptionMapper() {
    }

    public static InternalConsumptionResponse toResponse(InternalConsumption consumption) {
        return new InternalConsumptionResponse(
                consumption.getId(),
                consumption.getUser().getId(),
                consumption.getUser().getName(),
                consumption.getConsumptionDate(),
                consumption.getReason(),
                consumption.getNotes(),
                consumption.getDetails().stream().map(InternalConsumptionMapper::toResponse).toList());
    }

    public static InternalConsumptionDetailResponse toResponse(InternalConsumptionDetail detail) {
        return new InternalConsumptionDetailResponse(
                detail.getId(),
                detail.getProduct().getId(),
                detail.getProduct().getName(),
                detail.getUnitOfMeasure().getId(),
                detail.getUnitOfMeasure().getAbbreviation(),
                detail.getQuantity(),
                detail.getBaseQuantity());
    }
}

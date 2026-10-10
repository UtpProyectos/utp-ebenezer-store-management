package pe.edu.utp.ebenezer.api.dto.supplier;

import java.util.List;

import pe.edu.utp.ebenezer.domain.enums.SupplierType;

public record SupplierResponse(
        Long id,
        String name,
        String documentNumber,
        String phone,
        SupplierType type,
        String contactName,
        String address,
        String notes,
        Boolean active,
        List<SupplierProductResponse> products
) {
}

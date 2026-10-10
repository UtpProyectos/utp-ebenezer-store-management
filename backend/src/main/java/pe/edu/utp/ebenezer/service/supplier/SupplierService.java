package pe.edu.utp.ebenezer.service.supplier;

// TODO(team): declare the Supplier use cases here (request/response DTOs from api.dto.supplier).
import java.util.List;

import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierResponse;

public interface SupplierService {

    List<SupplierResponse> findAll(String search, Boolean active);

    SupplierResponse findById(Long id);

    SupplierResponse create(SupplierRequest request);

    SupplierResponse update(Long id, SupplierRequest request);

    SupplierResponse updateStatus(Long id, Boolean active);
}

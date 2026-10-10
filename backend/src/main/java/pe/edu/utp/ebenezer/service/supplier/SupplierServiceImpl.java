package pe.edu.utp.ebenezer.service.supplier;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierResponse;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll(String search, Boolean active) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return supplierRepository.findAll(Sort.by("name")).stream()
                .filter(supplier -> active == null || supplier.getActive().equals(active))
                .filter(supplier -> normalizedSearch.isEmpty()
                        || contains(supplier.getName(), normalizedSearch)
                        || contains(supplier.getPhone(), normalizedSearch)
                        || contains(supplier.getContactName(), normalizedSearch))
                .map(SupplierServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return toResponse(getSupplier(id));
    }

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        String name = request.name().trim();
        ensureNameAvailable(name);
        Supplier supplier = new Supplier();
        applyRequest(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = getSupplier(id);
        String name = request.name().trim();
        if (!supplier.getName().equalsIgnoreCase(name)) {
            ensureNameAvailable(name);
        }
        applyRequest(supplier, request);
        return toResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse updateStatus(Long id, Boolean active) {
        Supplier supplier = getSupplier(id);
        supplier.setActive(active);
        return toResponse(supplier);
    }

    private void ensureNameAvailable(String name) {
        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Supplier name is already in use");
        }
    }

    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private static void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name().trim());
        supplier.setDocumentNumber(normalize(request.documentNumber()));
        supplier.setPhone(normalize(request.phone()));
        supplier.setType(request.type());
        supplier.setContactName(normalize(request.contactName()));
        supplier.setAddress(normalize(request.address()));
        supplier.setNotes(normalize(request.notes()));
        if (supplier.getActive() == null) {
            supplier.setActive(true);
        }
    }

    private static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getDocumentNumber(),
                supplier.getPhone(),
                supplier.getType(),
                supplier.getContactName(),
                supplier.getAddress(),
                supplier.getNotes(),
                supplier.getActive()
        );
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }
}

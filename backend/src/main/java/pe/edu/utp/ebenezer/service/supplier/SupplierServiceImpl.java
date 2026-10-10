package pe.edu.utp.ebenezer.service.supplier;

import java.util.List;
import java.util.Locale;
import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierRequest;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierResponse;
import pe.edu.utp.ebenezer.api.dto.supplier.SupplierProductResponse;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.Supplier;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.supplier.SupplierRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll(String search, Boolean active) {
        String normalizedSearch = search == null || search.isBlank()
                ? ""
                : "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return supplierRepository.search(normalizedSearch, active).stream()
                .map(this::toResponse)
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
        return supplierRepository.findWithProductsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name().trim());
        supplier.setDocumentNumber(normalize(request.documentNumber()));
        supplier.setPhone(normalize(request.phone()));
        supplier.setType(request.type());
        supplier.setContactName(normalize(request.contactName()));
        supplier.setAddress(normalize(request.address()));
        supplier.setNotes(normalize(request.notes()));
        List<Long> productIds = request.productIds() == null ? List.of() : request.productIds();
        if (productIds.size() > 8) {
            throw new BusinessException("A supplier can have at most 8 products");
        }
        if (new HashSet<>(productIds).size() != productIds.size()) {
            throw new BusinessException("Supplier product selection contains duplicates");
        }
        if (!productIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(productIds);
            if (products.size() != productIds.size()) {
                throw new ResourceNotFoundException("One or more products were not found");
            }
            supplier.getProducts().clear();
            supplier.getProducts().addAll(products);
        } else {
            supplier.getProducts().clear();
        }
        if (supplier.getActive() == null) {
            supplier.setActive(true);
        }
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getDocumentNumber(),
                supplier.getPhone(),
                supplier.getType(),
                supplier.getContactName(),
                supplier.getAddress(),
                supplier.getNotes(),
                supplier.getActive(),
                supplier.getProducts().stream()
                        .map(product -> new SupplierProductResponse(product.getId(), product.getName()))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList()
        );
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

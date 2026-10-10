package pe.edu.utp.ebenezer.service.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.product.ProductRequest;
import pe.edu.utp.ebenezer.api.dto.product.ProductResponse;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.entity.Product;
import pe.edu.utp.ebenezer.domain.entity.UnitOfMeasure;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.inventory.InventoryMovementRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.domain.repository.unit.UnitOfMeasureRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(String search, Long categoryId, Boolean active) {
        String normalizedSearch = search == null || search.isBlank()
                ? ""
                : "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
        return productRepository.searchSummaries(normalizedSearch, categoryId, active, BigDecimal.ZERO).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request, null);
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        applyRequest(product, request, id);
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long id, Boolean active) {
        Product product = getProduct(id);
        product.setActive(active);
        return toResponse(product);
    }

    private void applyRequest(Product product, ProductRequest request, Long currentId) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getActive()) {
            throw new BusinessException("Cannot assign an inactive category to a product");
        }

        UnitOfMeasure baseUnit = unitOfMeasureRepository.findById(request.baseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found"));

        String barcode = normalize(request.barcode());
        if (barcode != null) {
            boolean barcodeInUse = currentId == null
                    ? productRepository.existsByBarcode(barcode)
                    : productRepository.findByBarcodeAndIdNot(barcode, currentId).isPresent();
            if (barcodeInUse) {
                throw new BusinessException("Barcode is already in use");
            }
        }

        product.setCategory(category);
        product.setBaseUnit(baseUnit);
        product.setName(request.name().trim());
        product.setDescription(normalize(request.description()));
        product.setBarcode(barcode);
        product.setSalePrice(request.salePrice());
        product.setMinStock(request.minStock() == null ? BigDecimal.ZERO : request.minStock());
        if (product.getActive() == null) {
            product.setActive(true);
        }
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getBaseUnit().getId(),
                product.getBaseUnit().getAbbreviation(),
                product.getName(),
                product.getDescription(),
                product.getBarcode(),
                product.getSalePrice(),
                product.getMinStock(),
                product.getActive(),
                product.getCreatedAt(),
                inventoryMovementRepository.sumBaseQuantityByProductId(product.getId())
        );
    }

    private ProductResponse toResponse(ProductRepository.ProductSummary summary) {
        return new ProductResponse(
                summary.getId(),
                summary.getCategoryId(),
                summary.getCategoryName(),
                summary.getBaseUnitId(),
                summary.getBaseUnitAbbreviation(),
                summary.getName(),
                summary.getDescription(),
                summary.getBarcode(),
                summary.getSalePrice(),
                summary.getMinStock(),
                summary.getActive(),
                summary.getCreatedAt(),
                summary.getCurrentStock()
        );
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

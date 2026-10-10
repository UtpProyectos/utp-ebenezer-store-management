package pe.edu.utp.ebenezer.service.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.utp.ebenezer.api.dto.category.CategoryRequest;
import pe.edu.utp.ebenezer.api.dto.category.CategoryResponse;
import pe.edu.utp.ebenezer.domain.entity.Category;
import pe.edu.utp.ebenezer.domain.repository.category.CategoryRepository;
import pe.edu.utp.ebenezer.domain.repository.product.ProductRepository;
import pe.edu.utp.ebenezer.exception.BusinessException;
import pe.edu.utp.ebenezer.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(Boolean active) {
        Sort sort = Sort.by("name");
        List<Category> categories = active == null
                ? categoryRepository.findAll(sort)
                : categoryRepository.findByActive(active, sort);
        // One grouped count instead of one count query per category.
        Map<Long, Long> productCountByCategory = productRepository.countByCategory().stream()
                .collect(Collectors.toMap(
                        ProductRepository.CategoryProductCount::getCategoryId,
                        ProductRepository.CategoryProductCount::getProductCount));
        return categories.stream()
                .map(category -> toResponse(category, productCountByCategory.getOrDefault(category.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        ensureNameAvailable(name);

        Category category = new Category();
        applyRequest(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        String name = request.name().trim();
        if (!category.getName().equalsIgnoreCase(name)) {
            ensureNameAvailable(name);
        }
        applyRequest(category, request);
        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateStatus(Long id, Boolean active) {
        Category category = getCategory(id);
        category.setActive(active);
        return toResponse(category);
    }

    private void ensureNameAvailable(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Category name is already in use");
        }
    }

    private static void applyRequest(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setDescription(normalize(request.description()));
        if (category.getActive() == null) {
            category.setActive(true);
        }
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private CategoryResponse toResponse(Category category) {
        return toResponse(category, productRepository.countByCategory_Id(category.getId()));
    }

    private static CategoryResponse toResponse(Category category, long productCount) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                productCount
        );
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

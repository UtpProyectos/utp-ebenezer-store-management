package pe.edu.utp.ebenezer.service.category;

import java.util.List;

import pe.edu.utp.ebenezer.api.dto.category.CategoryResponse;

public interface CategoryService {

    List<CategoryResponse> findAll(Boolean active);
}

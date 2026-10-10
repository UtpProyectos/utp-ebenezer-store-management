package pe.edu.utp.ebenezer.service.product;

import java.util.List;

import pe.edu.utp.ebenezer.api.dto.product.ProductRequest;
import pe.edu.utp.ebenezer.api.dto.product.ProductResponse;

public interface ProductService {

    List<ProductResponse> findAll(String search, Long categoryId, Boolean active);

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse updateStatus(Long id, Boolean active);
}

package com.jm.order_managment_system.service;

import com.jm.order_managment_system.dto.request.ProductRequest;
import com.jm.order_managment_system.dto.response.ProductResponse;
import com.jm.order_managment_system.entity.Product;
import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    Product findProductEntity(Long id);
}

package com.jm.order_managment_system.service.impl;

import com.jm.order_managment_system.dto.request.ProductRequest;
import com.jm.order_managment_system.dto.response.ProductResponse;
import com.jm.order_managment_system.entity.Product;
import com.jm.order_managment_system.exception.BusinessException;
import com.jm.order_managment_system.exception.NotFoundException;
import com.jm.order_managment_system.mapper.ProductMapper;
import com.jm.order_managment_system.repository.ProductRepository;
import com.jm.order_managment_system.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProductEntity(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        productRepository.findByName(request.name())
                .ifPresent(product -> {
                    throw new BusinessException("Product name already exists");
                });

        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductEntity(id);

        productRepository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Product name already exists");
                });

        productMapper.updateProductFromRequest(request, product);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductEntity(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }
}

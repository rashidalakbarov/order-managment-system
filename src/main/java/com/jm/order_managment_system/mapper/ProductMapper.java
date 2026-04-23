package com.jm.order_managment_system.mapper;

import com.jm.order_managment_system.dto.request.ProductRequest;
import com.jm.order_managment_system.dto.response.ProductResponse;
import com.jm.order_managment_system.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);
}

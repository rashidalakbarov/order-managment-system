package com.jm.order_managment_system.mapper;

import com.jm.order_managment_system.dto.response.OrderItemResponse;
import com.jm.order_managment_system.dto.response.OrderResponse;
import com.jm.order_managment_system.entity.Order;
import com.jm.order_managment_system.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toItemResponse(OrderItem orderItem);
}

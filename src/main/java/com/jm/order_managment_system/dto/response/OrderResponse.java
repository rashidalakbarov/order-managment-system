package com.jm.order_managment_system.dto.response;

import com.jm.order_managment_system.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
}

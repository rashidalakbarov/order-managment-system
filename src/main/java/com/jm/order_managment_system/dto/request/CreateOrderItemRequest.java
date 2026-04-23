package com.jm.order_managment_system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}

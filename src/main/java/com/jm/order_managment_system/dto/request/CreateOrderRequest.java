package com.jm.order_managment_system.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
}

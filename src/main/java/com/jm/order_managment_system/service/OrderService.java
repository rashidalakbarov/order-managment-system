package com.jm.order_managment_system.service;

import com.jm.order_managment_system.dto.request.CreateOrderRequest;
import com.jm.order_managment_system.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    List<OrderResponse> getMyOrders();

    List<OrderResponse> getOrders(Long userId);

    OrderResponse cancelOrder(Long orderId);
}

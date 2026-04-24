package com.jm.order_managment_system.service.impl;

import com.jm.order_managment_system.dto.request.CreateOrderItemRequest;
import com.jm.order_managment_system.dto.request.CreateOrderRequest;
import com.jm.order_managment_system.dto.response.OrderResponse;
import com.jm.order_managment_system.entity.Order;
import com.jm.order_managment_system.entity.OrderItem;
import com.jm.order_managment_system.entity.OrderStatus;
import com.jm.order_managment_system.entity.Product;
import com.jm.order_managment_system.entity.User;
import com.jm.order_managment_system.exception.BusinessException;
import com.jm.order_managment_system.exception.NotFoundException;
import com.jm.order_managment_system.mapper.OrderMapper;
import com.jm.order_managment_system.repository.OrderRepository;
import com.jm.order_managment_system.repository.ProductRepository;
import com.jm.order_managment_system.repository.UserRepository;
import com.jm.order_managment_system.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = resolveCurrentUser();
        Map<Long, Integer> requestedQuantities = normalizeRequestedQuantities(request);
        Map<Long, Product> productMap = getProductMap(requestedQuantities.keySet());
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productMap.get(entry.getKey());
            Integer quantity = entry.getValue();

            if (product.getStock() < quantity) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - quantity);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);

        persistProducts(productMap.values(), "Stock changed while creating the order. Please retry.");
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        return mapToResponses(getOrdersForUser(resolveCurrentUser().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long userId) {
        User currentUser = resolveCurrentUser();
        boolean admin = isAdmin();

        if (!admin && userId == null) {
            throw new AccessDeniedException("Only admins can view all orders");
        }

        if (!admin && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to view these orders");
        }

        List<Order> orders = userId == null ? orderRepository.findAll() : getOrdersForUser(userId);
        return mapToResponses(orders);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        User currentUser = resolveCurrentUser();

        validateCancelPermission(order, currentUser);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only pending orders can be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(currentUser.getEmail());

        persistProducts(order.getItems().stream()
                .map(OrderItem::getProduct)
                .toList(), "Stock changed while cancelling the order. Please retry.");
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    private List<OrderResponse> mapToResponses(List<Order> orders) {
        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    private Map<Long, Integer> normalizeRequestedQuantities(CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }

        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : request.items()) {
            if (item.productId() == null) {
                throw new BusinessException("Product id is required");
            }

            if (item.quantity() == null || item.quantity() < 1) {
                throw new BusinessException("Quantity must be greater than zero");
            }

            requestedQuantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        return requestedQuantities;
    }

    private Map<Long, Product> getProductMap(java.util.Set<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = new LinkedHashMap<>();
        products.forEach(product -> productMap.put(product.getId(), product));

        if (productMap.size() != productIds.size()) {
            throw new NotFoundException("One or more products were not found");
        }

        return productMap;
    }

    private User resolveCurrentUser() {
        Authentication authentication = getRequiredAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }

    private void validateCancelPermission(Order order, User currentUser) {
        if (isAdmin()) {
            return;
        }

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to cancel this order");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = getAuthenticationOrNull();
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private Authentication getRequiredAuthentication() {
        Authentication authentication = getAuthenticationOrNull();
        if (authentication == null) {
            throw new AccessDeniedException("Authentication is required for this operation");
        }
        return authentication;
    }

    private Authentication getAuthenticationOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication;
    }

    private void persistProducts(Collection<Product> products, String concurrencyMessage) {
        try {
            productRepository.saveAllAndFlush(products);
        } catch (OptimisticLockingFailureException exception) {
            throw new OptimisticLockingFailureException(concurrencyMessage, exception);
        }
    }
}

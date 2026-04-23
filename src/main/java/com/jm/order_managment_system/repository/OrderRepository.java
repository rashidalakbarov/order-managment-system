package com.jm.order_managment_system.repository;

import com.jm.order_managment_system.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findById(Long id);
}

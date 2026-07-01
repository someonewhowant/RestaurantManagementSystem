package com.vanilla.crm.orders;

import com.vanilla.crm.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByTableIdAndStatus(UUID tableId, Order.OrderStatus status);

    Optional<Order> findFirstByTableIdAndStatus(UUID tableId, Order.OrderStatus status);
}

package com.vanilla.crm.repository;

import com.vanilla.crm.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // Get all items currently being cooked (for kitchen display)
    @Query("SELECT oi FROM OrderItem oi WHERE oi.status = 'COOKING'")
    List<OrderItem> findAllCookingItems();
}

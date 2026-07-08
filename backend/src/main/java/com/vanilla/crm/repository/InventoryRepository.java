package com.vanilla.crm.repository;

import com.vanilla.crm.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {
    
    // Finds items where currentStock <= minStock
    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock <= i.minStock")
    List<InventoryItem> findLowStockItems();

    // Finds items that are expiring soon (e.g. <= 3 days) and sorts them
    @Query("SELECT i FROM InventoryItem i WHERE i.expiresInDays IS NOT NULL AND i.expiresInDays <= 3 ORDER BY i.expiresInDays ASC")
    List<InventoryItem> findExpiringItems();
}

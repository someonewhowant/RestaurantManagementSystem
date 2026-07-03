package com.vanilla.crm.inventory;

import com.vanilla.crm.inventory.entity.InventoryItem;
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

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i SET i.currentStock = i.currentStock + :amount WHERE i.id = :id")
    void restockItem(@org.springframework.data.repository.query.Param("id") UUID id, @org.springframework.data.repository.query.Param("amount") Double amount);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i SET i.currentStock = CASE WHEN i.currentStock - :amount < 0 THEN 0.0 ELSE i.currentStock - :amount END WHERE i.id = :id")
    void consumeItem(@org.springframework.data.repository.query.Param("id") UUID id, @org.springframework.data.repository.query.Param("amount") Double amount);
}

package com.vanilla.crm.service;

import com.vanilla.crm.dto.inventory.ConsumeItemDto;
import com.vanilla.crm.dto.inventory.InventoryItemDto;
import com.vanilla.crm.entity.Order;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<InventoryItemDto> getAllItems();
    List<InventoryItemDto> getLowStockItems();
    List<InventoryItemDto> getExpiringItems();
    InventoryItemDto createItem(InventoryItemDto dto);
    InventoryItemDto updateItem(UUID id, InventoryItemDto dto);
    InventoryItemDto restock(UUID id, Double amount);
    InventoryItemDto consume(UUID id, Double amount);
    void consumeBatch(List<ConsumeItemDto> items);
    void deleteItem(UUID id);
    void consumeByOrder(Order order);
    void restockByOrder(Order order);
    byte[] exportCsv();
}

package com.vanilla.crm.service;

import com.vanilla.crm.dto.inventory.ConsumeItemDto;
import com.vanilla.crm.dto.inventory.InventoryItemDto;
import com.vanilla.crm.entity.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
}

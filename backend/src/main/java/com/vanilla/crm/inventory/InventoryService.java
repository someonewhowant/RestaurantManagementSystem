package com.vanilla.crm.inventory;

import com.vanilla.crm.inventory.dto.ConsumeItemDto;
import com.vanilla.crm.inventory.dto.InventoryItemDto;
import com.vanilla.crm.inventory.entity.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(InventoryItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(InventoryItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getExpiringItems() {
        return inventoryRepository.findExpiringItems().stream()
                .map(InventoryItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemDto createItem(InventoryItemDto dto) {
        InventoryItem item = InventoryItem.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .currentStock(dto.getCurrentStock())
                .minStock(dto.getMinStock())
                .unit(dto.getUnit())
                .pricePerUnit(dto.getPricePerUnit())
                .expiresInDays(dto.getExpiresInDays())
                .build();
        return InventoryItemDto.fromEntity(inventoryRepository.save(item));
    }

    @Transactional
    public InventoryItemDto updateItem(UUID id, InventoryItemDto dto) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getCurrentStock() != null) item.setCurrentStock(dto.getCurrentStock());
        if (dto.getMinStock() != null) item.setMinStock(dto.getMinStock());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getPricePerUnit() != null) item.setPricePerUnit(dto.getPricePerUnit());
        if (dto.getExpiresInDays() != null) item.setExpiresInDays(dto.getExpiresInDays());

        return InventoryItemDto.fromEntity(inventoryRepository.save(item));
    }

    @Transactional
    public InventoryItemDto restock(UUID id, Double amount) {
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setCurrentStock(item.getCurrentStock() + amount);
        return InventoryItemDto.fromEntity(inventoryRepository.save(item));
    }

    @Transactional
    public InventoryItemDto consume(UUID id, Double amount) {
        if (amount == null || amount <= 0) return inventoryRepository.findById(id).map(InventoryItemDto::fromEntity).orElseThrow();
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        double newStock = item.getCurrentStock() - amount;
        item.setCurrentStock(newStock < 0 ? 0.0 : newStock);
        return InventoryItemDto.fromEntity(inventoryRepository.save(item));
    }

    @Transactional
    public void consumeBatch(List<ConsumeItemDto> items) {
        if (items == null || items.isEmpty()) return;
        List<UUID> itemIds = items.stream().map(ConsumeItemDto::getIngredientId).collect(Collectors.toList());
        List<InventoryItem> inventoryItems = inventoryRepository.findAllById(itemIds);
        
        java.util.Map<UUID, InventoryItem> itemMap = inventoryItems.stream()
                .collect(Collectors.toMap(InventoryItem::getId, i -> i));

        for (ConsumeItemDto itemDto : items) {
            if (itemDto.getAmount() != null && itemDto.getAmount() > 0) {
                InventoryItem item = itemMap.get(itemDto.getIngredientId());
                if (item != null) {
                    double newStock = item.getCurrentStock() - itemDto.getAmount();
                    item.setCurrentStock(newStock < 0 ? 0.0 : newStock);
                }
            }
        }
        inventoryRepository.saveAll(inventoryItems);
    }

    @Transactional
    public void deleteItem(UUID id) {
        inventoryRepository.deleteById(id);
    }
}

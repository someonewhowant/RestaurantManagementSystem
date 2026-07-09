package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;
import com.vanilla.crm.exception.BusinessRuleException;
import com.vanilla.crm.entity.Order;
import com.vanilla.crm.entity.OrderItem;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.util.CsvExportUtil;

import com.vanilla.crm.repository.InventoryRepository;

import com.vanilla.crm.dto.inventory.ConsumeItemDto;
import com.vanilla.crm.dto.inventory.InventoryItemDto;
import com.vanilla.crm.entity.InventoryItem;
import com.vanilla.crm.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vanilla.crm.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    @Override
    public List<InventoryItemDto> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryItemDto> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventoryItemDto> getExpiringItems() {
        return inventoryRepository.findExpiringItems().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public InventoryItemDto createItem(InventoryItemDto dto) {
        log.info("Creating new inventory item: {}", dto.getName());
        InventoryItem item = InventoryItem.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .currentStock(dto.getCurrentStock())
                .minStock(dto.getMinStock())
                .unit(dto.getUnit())
                .pricePerUnit(dto.getPricePerUnit())
                .expiresInDays(dto.getExpiresInDays())
                .build();
        return inventoryMapper.toDto(inventoryRepository.save(item));
    }

    @Transactional
    @Override
    public InventoryItemDto updateItem(UUID id, InventoryItemDto dto) {
        log.info("Updating inventory item {}", id);
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getCategory() != null) item.setCategory(dto.getCategory());
        if (dto.getCurrentStock() != null) item.setCurrentStock(dto.getCurrentStock());
        if (dto.getMinStock() != null) item.setMinStock(dto.getMinStock());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getPricePerUnit() != null) item.setPricePerUnit(dto.getPricePerUnit());
        if (dto.getExpiresInDays() != null) item.setExpiresInDays(dto.getExpiresInDays());

        return inventoryMapper.toDto(inventoryRepository.save(item));
    }

    @Transactional
    @Override
    public InventoryItemDto restock(UUID id, Double amount) {
        log.info("Restocking item {} by {}", id, amount);
        if (amount == null || amount <= 0) throw new BusinessRuleException("Amount must be positive");
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        item.setCurrentStock(item.getCurrentStock() + amount);
        return inventoryMapper.toDto(inventoryRepository.save(item));
    }

    @Transactional
    @Override
    public InventoryItemDto consume(UUID id, Double amount) {
        log.info("Consuming item {} by {}", id, amount);
        if (amount == null || amount <= 0) return inventoryRepository.findById(id).map(inventoryMapper::toDto).orElseThrow();
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        double newStock = item.getCurrentStock() - amount;
        item.setCurrentStock(newStock < 0 ? 0.0 : newStock);
        return inventoryMapper.toDto(inventoryRepository.save(item));
    }

    @Transactional
    @Override
    public void consumeBatch(List<ConsumeItemDto> items) {
        log.info("Consuming batch of {} items", items != null ? items.size() : 0);
        if (items == null || items.isEmpty()) return;
        List<UUID> itemIds = items.stream().map(ConsumeItemDto::getIngredientId).collect(Collectors.toList());
        List<InventoryItem> inventoryItems = inventoryRepository.findAllById(itemIds);
        
        Map<UUID, InventoryItem> itemMap = inventoryItems.stream()
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
    @Override
    public void deleteItem(UUID id) {
        log.info("Deleting item {}", id);
        inventoryRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void consumeByOrder(Order order) {
        log.info("Consuming ingredients for order {}", order.getId());
        Map<UUID, Double> ingredientsToConsume = new HashMap<>();
        order.getItems().stream()
                .filter(orderItem -> orderItem.getStatus() != OrderItem.ItemStatus.CANCELLED)
                .forEach(orderItem -> {
                    Dish dish = orderItem.getDish();
                    if (dish.getRecipe() != null) {
                        dish.getRecipe().forEach(recipeIngredient -> {
                            double totalAmount = recipeIngredient.getAmount() * orderItem.getQuantity();
                            UUID invId = recipeIngredient.getInventoryItem().getId();
                            ingredientsToConsume.put(invId, ingredientsToConsume.getOrDefault(invId, 0.0) + totalAmount);
                        });
                    }
                });

        if (ingredientsToConsume.isEmpty()) return;

        List<InventoryItem> inventoryItems = inventoryRepository.findAllById(ingredientsToConsume.keySet());
        Map<UUID, InventoryItem> itemMap = inventoryItems.stream()
                .collect(Collectors.toMap(InventoryItem::getId, i -> i));

        for (Map.Entry<UUID, Double> entry : ingredientsToConsume.entrySet()) {
            InventoryItem item = itemMap.get(entry.getKey());
            if (item != null) {
                double newStock = item.getCurrentStock() - entry.getValue();
                item.setCurrentStock(newStock < 0 ? 0.0 : newStock);
            }
        }
        inventoryRepository.saveAll(inventoryItems);
    }

    @Transactional
    @Override
    public void restockByOrder(Order order) {
        log.info("Restocking ingredients from cancelled order {}", order.getId());
        Map<UUID, Double> ingredientsToRestock = new HashMap<>();
        order.getItems().stream()
                .filter(item -> item.getStatus() != OrderItem.ItemStatus.CANCELLED)
                .forEach(orderItem -> {
                    Dish dish = orderItem.getDish();
                    if (dish.getRecipe() != null) {
                        dish.getRecipe().forEach(recipeIngredient -> {
                            double totalAmount = recipeIngredient.getAmount() * orderItem.getQuantity();
                            UUID invId = recipeIngredient.getInventoryItem().getId();
                            ingredientsToRestock.put(invId, ingredientsToRestock.getOrDefault(invId, 0.0) + totalAmount);
                        });
                    }
                });

        if (ingredientsToRestock.isEmpty()) return;

        List<InventoryItem> inventoryItems = inventoryRepository.findAllById(ingredientsToRestock.keySet());
        Map<UUID, InventoryItem> itemMap = inventoryItems.stream()
                .collect(Collectors.toMap(InventoryItem::getId, i -> i));

        for (Map.Entry<UUID, Double> entry : ingredientsToRestock.entrySet()) {
            InventoryItem item = itemMap.get(entry.getKey());
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock() + entry.getValue());
            }
        }
        inventoryRepository.saveAll(inventoryItems);
    }

    @Override
    public byte[] exportCsv() {
        log.info("Exporting inventory to CSV");
        List<InventoryItemDto> items = getAllItems();
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Название;Категория;Текущий остаток;Мин. остаток;Единица;Цена за ед.\n");
        for (InventoryItemDto item : items) {
            sb.append(String.format("%s;%s;%s;%s;%s;%s;%s\n",
                    item.getId(),
                    CsvExportUtil.escapeField(item.getName()),
                    CsvExportUtil.escapeField(item.getCategory()),
                    item.getCurrentStock(),
                    item.getMinStock(),
                    CsvExportUtil.escapeField(item.getUnit()),
                    item.getPricePerUnit()));
        }
        return CsvExportUtil.wrapWithBom(sb.toString());
    }
}

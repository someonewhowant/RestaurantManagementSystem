package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.inventory.InventoryItemDto;
import com.vanilla.crm.entity.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public InventoryItemDto toDto(InventoryItem item) {
        if (item == null) return null;
        return InventoryItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .currentStock(item.getCurrentStock())
                .minStock(item.getMinStock())
                .unit(item.getUnit())
                .pricePerUnit(item.getPricePerUnit())
                .expiresInDays(item.getExpiresInDays())
                .build();
    }
}

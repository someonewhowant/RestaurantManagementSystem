package com.vanilla.crm.inventory.dto;

import com.vanilla.crm.inventory.entity.InventoryItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class InventoryItemDto {
    private UUID id;
    private String name;
    private String category;
    private Double currentStock;
    private Double minStock;
    private String unit;
    private BigDecimal pricePerUnit;
    private Integer expiresInDays;

    public static InventoryItemDto fromEntity(InventoryItem item) {
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

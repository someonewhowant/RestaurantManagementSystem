package com.vanilla.crm.dto.inventory;

import com.vanilla.crm.entity.InventoryItem;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class InventoryItemDto {
    private UUID id;
    
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    
    @NotBlank(message = "Категория обязательна")
    private String category;
    
    @NotNull(message = "Текущий остаток обязателен")
    @PositiveOrZero(message = "Остаток не может быть отрицательным")
    private Double currentStock;
    
    @NotNull(message = "Минимальный остаток обязателен")
    @PositiveOrZero(message = "Минимальный остаток не может быть отрицательным")
    private Double minStock;
    
    @NotBlank(message = "Единица измерения обязательна")
    private String unit;
    
    @NotNull(message = "Цена обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
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

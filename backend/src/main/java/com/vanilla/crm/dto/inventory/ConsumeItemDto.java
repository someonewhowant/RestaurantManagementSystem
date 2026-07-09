package com.vanilla.crm.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeItemDto {
    @NotNull(message = "ID ингредиента обязателен")
    private UUID ingredientId;
    
    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть положительным")
    private Double amount;
}

package com.vanilla.crm.dto.menu;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Data
public class RecipeRequest {
    @NotNull(message = "ID ингредиента обязателен")
    private UUID ingredientId;
    
    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть положительным")
    private Double amount;
}

package com.vanilla.crm.dto.orders;

import lombok.Data;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class AddItemRequest {
    @NotNull(message = "ID блюда обязателен")
    private UUID dishId;
    
    @Positive(message = "Количество должно быть положительным")
    private Integer quantity;
}

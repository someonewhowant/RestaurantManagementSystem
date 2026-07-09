package com.vanilla.crm.dto.inventory;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class AmountRequest {
    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть положительным")
    private Double amount;
}

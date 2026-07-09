package com.vanilla.crm.dto.orders;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ItemStatusRequest {
    @NotBlank(message = "Статус обязателен")
    private String status; // "new", "cooking", "ready", "served"
}

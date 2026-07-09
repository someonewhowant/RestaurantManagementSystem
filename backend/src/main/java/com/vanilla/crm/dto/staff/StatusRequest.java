package com.vanilla.crm.dto.staff;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class StatusRequest {
    @NotBlank(message = "Статус обязателен")
    private String status; // "Активен", "В отпуске", "Уволен"
}

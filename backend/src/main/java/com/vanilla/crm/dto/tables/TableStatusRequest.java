package com.vanilla.crm.dto.tables;

import lombok.Data;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

@Data
public class TableStatusRequest {
    @NotBlank(message = "Статус обязателен")
    private String status;    // "Свободен", "Занят", "Ожидает блюда", "Оплата"
    private UUID waiterId;
}

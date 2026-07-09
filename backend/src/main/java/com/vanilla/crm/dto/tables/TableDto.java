package com.vanilla.crm.dto.tables;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
public class TableDto {
    private UUID id;
    
    @NotNull(message = "Номер столика обязателен")
    @Positive(message = "Номер столика должен быть положительным")
    private Integer number;
    
    @NotNull(message = "Вместимость обязательна")
    @Positive(message = "Вместимость должна быть положительной")
    private Integer capacity;
    
    @NotBlank(message = "Статус обязателен")
    private String status;   // "Свободен", "Занят", "Ожидает блюда", "Оплата"
    
    private UUID waiterId;
    private String statusUpdatedAt;

}

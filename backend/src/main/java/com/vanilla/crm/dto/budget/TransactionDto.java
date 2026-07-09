package com.vanilla.crm.dto.budget;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

@Data
@Builder
public class TransactionDto {
    private UUID id;
    private String date;

    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма транзакции должна быть больше нуля")
    private BigDecimal amount;

    @NotBlank(message = "Тип транзакции обязателен")
    private String type;       // "Доход" or "Расход"

    @NotBlank(message = "Категория обязательна")
    private String category;

    private String description;

    private UUID orderId;

}

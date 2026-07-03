package com.vanilla.crm.budget.dto;

import com.vanilla.crm.budget.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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

    private static final java.util.Map<String, Transaction.TransactionType> TYPE_MAP = java.util.Map.of(
            "Доход", Transaction.TransactionType.INCOME,
            "Расход", Transaction.TransactionType.EXPENSE
    );

    private static final java.util.Map<Transaction.TransactionType, String> TYPE_REVERSE = java.util.Map.of(
            Transaction.TransactionType.INCOME, "Доход",
            Transaction.TransactionType.EXPENSE, "Расход"
    );

    public static TransactionDto fromEntity(Transaction tx) {
        return TransactionDto.builder()
                .id(tx.getId())
                .date(tx.getDate() != null ? tx.getDate().toString() : null)
                .amount(tx.getAmount())
                .type(TYPE_REVERSE.getOrDefault(tx.getType(), tx.getType().name()))
                .category(tx.getCategory())
                .description(tx.getDescription())
                .build();
    }

    public static Transaction.TransactionType toTypeEnum(String type) {
        return TYPE_MAP.getOrDefault(type, Transaction.TransactionType.EXPENSE);
    }
}

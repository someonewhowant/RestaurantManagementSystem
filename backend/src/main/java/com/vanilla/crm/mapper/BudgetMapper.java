package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.budget.TransactionDto;
import com.vanilla.crm.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BudgetMapper {
    private static final Map<String, Transaction.TransactionType> TYPE_MAP = Map.of(
            "Доход", Transaction.TransactionType.INCOME,
            "Расход", Transaction.TransactionType.EXPENSE
    );

    private static final Map<Transaction.TransactionType, String> TYPE_REVERSE = Map.of(
            Transaction.TransactionType.INCOME, "Доход",
            Transaction.TransactionType.EXPENSE, "Расход"
    );

    public TransactionDto toDto(Transaction tx) {
        if (tx == null) return null;
        return TransactionDto.builder()
                .id(tx.getId())
                .date(tx.getDate() != null ? tx.getDate().toString() : null)
                .amount(tx.getAmount())
                .type(TYPE_REVERSE.getOrDefault(tx.getType(), tx.getType().name()))
                .category(tx.getCategory())
                .description(tx.getDescription())
                .orderId(tx.getOrderId())
                .build();
    }

    public Transaction.TransactionType toTypeEnum(String type) {
        return TYPE_MAP.getOrDefault(type, Transaction.TransactionType.EXPENSE);
    }
}

package com.vanilla.crm.budget;

import com.vanilla.crm.budget.dto.BudgetSummaryDto;
import com.vanilla.crm.budget.dto.TransactionDto;
import com.vanilla.crm.budget.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByDateDesc(pageable)
                .map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public BudgetSummaryDto getSummary() {
        BigDecimal income = transactionRepository.sumIncome();
        BigDecimal expense = transactionRepository.sumExpense();
        return BudgetSummaryDto.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .balance(income.subtract(expense))
                .build();
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto dto) {
        Transaction tx = Transaction.builder()
                .date(dto.getDate() != null ? Instant.parse(dto.getDate()) : Instant.now())
                .amount(dto.getAmount())
                .type(TransactionDto.toTypeEnum(dto.getType()))
                .category(dto.getCategory())
                .description(dto.getDescription())
                .build();

        return TransactionDto.fromEntity(transactionRepository.save(tx));
    }
}

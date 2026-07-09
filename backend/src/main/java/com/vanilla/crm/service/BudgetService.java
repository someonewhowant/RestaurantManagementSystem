package com.vanilla.crm.service;

import com.vanilla.crm.dto.budget.BudgetSummaryDto;
import com.vanilla.crm.dto.budget.TransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface BudgetService {
    Page<TransactionDto> getTransactions(Instant start, Instant end, Pageable pageable);
    BudgetSummaryDto getSummary(Instant start, Instant end);
    TransactionDto createTransaction(TransactionDto dto);
    TransactionDto refundTransaction(UUID transactionId);
    byte[] exportCsv(Instant start, Instant end);
}

package com.vanilla.crm.service;

import com.vanilla.crm.repository.OrderRepository;
import com.vanilla.crm.entity.Order;
import com.vanilla.crm.entity.OrderItem;
import com.vanilla.crm.service.InventoryService;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.dto.budget.BudgetSummaryDto;
import com.vanilla.crm.dto.budget.TransactionDto;
import com.vanilla.crm.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BudgetService {
    Page<TransactionDto> getTransactions(Instant start, Instant end, Pageable pageable);
    BudgetSummaryDto getSummary(Instant start, Instant end);
    TransactionDto createTransaction(TransactionDto dto);
    TransactionDto refundTransaction(UUID transactionId);
}

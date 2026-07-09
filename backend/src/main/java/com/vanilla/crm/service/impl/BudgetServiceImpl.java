package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;
import com.vanilla.crm.exception.BusinessRuleException;
import com.vanilla.crm.exception.DuplicateResourceException;
import com.vanilla.crm.util.CsvExportUtil;

import com.vanilla.crm.repository.TransactionRepository;

import com.vanilla.crm.repository.OrderRepository;
import com.vanilla.crm.entity.Order;
import com.vanilla.crm.entity.OrderItem;
import com.vanilla.crm.service.InventoryService;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.dto.budget.BudgetSummaryDto;
import com.vanilla.crm.dto.budget.TransactionDto;
import com.vanilla.crm.entity.Transaction;
import com.vanilla.crm.mapper.BudgetMapper;
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
import lombok.extern.slf4j.Slf4j;
import com.vanilla.crm.service.BudgetService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final BudgetMapper budgetMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionDto> getTransactions(Instant start, Instant end, Pageable pageable) {
        if (start != null && end != null) {
            return transactionRepository.findAllByDateBetweenOrderByDateDesc(start, end, pageable)
                    .map(budgetMapper::toDto);
        }
        return transactionRepository.findAllByOrderByDateDesc(pageable)
                .map(budgetMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public BudgetSummaryDto getSummary(Instant start, Instant end) {
        BigDecimal income;
        BigDecimal expense;
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        if (start != null && end != null) {
            income = transactionRepository.sumIncomeBetween(start, end);
            expense = transactionRepository.sumExpenseBetween(start, end);
            List<Object[]> rawCategories = transactionRepository.sumExpenseByCategoryBetween(start, end);
            for (Object[] row : rawCategories) {
                expenseByCategory.put((String) row[0], (BigDecimal) row[1]);
            }
        } else {
            income = transactionRepository.sumIncome();
            expense = transactionRepository.sumExpense();
            // We can leave expenseByCategory empty for all time if not requested, or compute it. 
            // For now, let's only compute it if a range is provided, or compute it for all time.
        }

        return BudgetSummaryDto.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .balance(income.subtract(expense))
                .expenseByCategory(expenseByCategory)
                .build();
    }

    @Transactional
    @Override
    public TransactionDto createTransaction(TransactionDto dto) {
        log.info("Creating transaction of type {} for amount {}", dto.getType(), dto.getAmount());
        Transaction tx = Transaction.builder()
                .date(dto.getDate() != null ? Instant.parse(dto.getDate()) : Instant.now())
                .amount(dto.getAmount())
                .type(budgetMapper.toTypeEnum(dto.getType()))
                .category(dto.getCategory())
                .description(dto.getDescription())
                .orderId(dto.getOrderId())
                .build();

        return budgetMapper.toDto(transactionRepository.saveAndFlush(tx));
    }

    @Transactional
    @Override
    public TransactionDto refundTransaction(UUID transactionId) {
        log.info("Refunding transaction {}", transactionId);
        Transaction originalTx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Only allow refunding INCOME transactions (usually orders)
        if (originalTx.getType() != Transaction.TransactionType.INCOME) {
            throw new BusinessRuleException("Only income transactions can be refunded");
        }

        String refundDescription = "Возврат по транзакции #" + originalTx.getId();
        if (transactionRepository.existsByDescription(refundDescription)) {
            throw new DuplicateResourceException("This transaction has already been refunded");
        }

        // Create the Storno (Refund) transaction
        Transaction refundTx = Transaction.builder()
                .date(Instant.now())
                .amount(originalTx.getAmount())
                .type(Transaction.TransactionType.EXPENSE)
                .category("Возврат (Сторно)")
                .description(refundDescription)
                .orderId(originalTx.getOrderId())
                .build();

        Transaction savedRefund = transactionRepository.save(refundTx);

        // If the original transaction was linked to an order, return ingredients to inventory
        if (originalTx.getOrderId() != null) {
            orderRepository.findById(originalTx.getOrderId()).ifPresent(order -> {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                
                inventoryService.restockByOrder(order);
            });
        }

        return budgetMapper.toDto(savedRefund);
    }

    @Override
    public byte[] exportCsv(Instant start, Instant end) {
        log.info("Exporting budget to CSV");
        Page<TransactionDto> transactions = getTransactions(start, end, org.springframework.data.domain.Pageable.unpaged());
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Дата;Сумма;Тип;Категория;Описание\n");
        for (TransactionDto tx : transactions.getContent()) {
            sb.append(String.format("%s;%s;%s;%s;%s;%s\n",
                    tx.getId(),
                    tx.getDate() != null ? tx.getDate() : "",
                    tx.getAmount(),
                    CsvExportUtil.escapeField(tx.getType()),
                    CsvExportUtil.escapeField(tx.getCategory()),
                    CsvExportUtil.escapeField(tx.getDescription())));
        }
        return CsvExportUtil.wrapWithBom(sb.toString());
    }
}

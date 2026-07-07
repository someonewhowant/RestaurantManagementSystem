package com.vanilla.crm.budget;

import com.vanilla.crm.orders.OrderRepository;
import com.vanilla.crm.orders.entity.Order;
import com.vanilla.crm.orders.entity.OrderItem;
import com.vanilla.crm.inventory.InventoryService;
import com.vanilla.crm.menu.entity.Dish;
import com.vanilla.crm.budget.dto.BudgetSummaryDto;
import com.vanilla.crm.budget.dto.TransactionDto;
import com.vanilla.crm.budget.entity.Transaction;
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

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(Instant start, Instant end, Pageable pageable) {
        if (start != null && end != null) {
            return transactionRepository.findAllByDateBetweenOrderByDateDesc(start, end, pageable)
                    .map(TransactionDto::fromEntity);
        }
        return transactionRepository.findAllByOrderByDateDesc(pageable)
                .map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
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
    public TransactionDto createTransaction(TransactionDto dto) {
        Transaction tx = Transaction.builder()
                .date(dto.getDate() != null ? Instant.parse(dto.getDate()) : Instant.now())
                .amount(dto.getAmount())
                .type(TransactionDto.toTypeEnum(dto.getType()))
                .category(dto.getCategory())
                .description(dto.getDescription())
                .orderId(dto.getOrderId())
                .build();

        return TransactionDto.fromEntity(transactionRepository.saveAndFlush(tx));
    }

    @Transactional
    public TransactionDto refundTransaction(UUID transactionId) {
        Transaction originalTx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Only allow refunding INCOME transactions (usually orders)
        if (originalTx.getType() != Transaction.TransactionType.INCOME) {
            throw new RuntimeException("Only income transactions can be refunded");
        }

        String refundDescription = "Возврат по транзакции #" + originalTx.getId();
        if (transactionRepository.existsByDescription(refundDescription)) {
            throw new RuntimeException("This transaction has already been refunded");
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
                
                order.getItems().stream()
                        .filter(item -> item.getStatus() != OrderItem.ItemStatus.CANCELLED)
                        .forEach(orderItem -> {
                            Dish dish = orderItem.getDish();
                            if (dish.getRecipe() != null) {
                                dish.getRecipe().forEach(recipeIngredient -> {
                                    double totalAmountToReturn = recipeIngredient.getAmount() * orderItem.getQuantity();
                                    // Restock inventory
                                    inventoryService.restock(recipeIngredient.getInventoryItem().getId(), totalAmountToReturn);
                                });
                            }
                        });
            });
        }

        return TransactionDto.fromEntity(savedRefund);
    }
}

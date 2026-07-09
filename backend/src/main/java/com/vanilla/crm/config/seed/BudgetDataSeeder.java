package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.Transaction;
import com.vanilla.crm.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@Order(40)
@RequiredArgsConstructor
@Slf4j
public class BudgetDataSeeder implements DataSeeder {

    private final TransactionRepository transactionRepository;

    @Override
    public void seed() {
        if (transactionRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding budget data...");
        List<Transaction> txs = List.of(
            Transaction.builder().date(Instant.now().minusSeconds(86400 * 2)).amount(new BigDecimal("1500")).type(Transaction.TransactionType.INCOME).category("Оплата заказа").description("Выручка за смену").build(),
            Transaction.builder().date(Instant.now().minusSeconds(86400)).amount(new BigDecimal("300")).type(Transaction.TransactionType.EXPENSE).category("Закупки").description("Закупка овощей").build(),
            Transaction.builder().date(Instant.now()).amount(new BigDecimal("2100")).type(Transaction.TransactionType.INCOME).category("Оплата заказа").description("Выручка за смену").build(),
            Transaction.builder().date(Instant.now()).amount(new BigDecimal("500")).type(Transaction.TransactionType.EXPENSE).category("Коммуналка").description("Оплата электричества").build()
        );
        transactionRepository.saveAll(txs);
        log.info("Seeded {} transactions.", txs.size());
    }
}

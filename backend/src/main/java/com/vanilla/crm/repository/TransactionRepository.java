package com.vanilla.crm.repository;

import com.vanilla.crm.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.query.Param;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByType(Transaction.TransactionType type);

    boolean existsByDescription(String description);

    Page<Transaction> findAllByOrderByDateDesc(Pageable pageable);
    
    Page<Transaction> findAllByDateBetweenOrderByDateDesc(Instant start, Instant end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") Transaction.TransactionType type);

    default BigDecimal sumIncome() {
        return sumAmountByType(Transaction.TransactionType.INCOME);
    }

    default BigDecimal sumExpense() {
        return sumAmountByType(Transaction.TransactionType.EXPENSE);
    }

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal sumAmountByTypeBetween(@Param("type") Transaction.TransactionType type, @Param("start") Instant start, @Param("end") Instant end);

    default BigDecimal sumIncomeBetween(Instant start, Instant end) {
        return sumAmountByTypeBetween(Transaction.TransactionType.INCOME, start, end);
    }

    default BigDecimal sumExpenseBetween(Instant start, Instant end) {
        return sumAmountByTypeBetween(Transaction.TransactionType.EXPENSE, start, end);
    }

    @Query("SELECT t.category as category, COALESCE(SUM(t.amount), 0) as total FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :start AND :end GROUP BY t.category")
    List<Object[]> sumAmountByCategoryBetween(@Param("type") Transaction.TransactionType type, @Param("start") Instant start, @Param("end") Instant end);

    default List<Object[]> sumExpenseByCategoryBetween(Instant start, Instant end) {
        return sumAmountByCategoryBetween(Transaction.TransactionType.EXPENSE, start, end);
    }
}

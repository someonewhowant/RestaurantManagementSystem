package com.vanilla.crm.budget.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant date;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String category;

    private String description;

    @Column(name = "order_id")
    private UUID orderId;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = Instant.now();
        }
    }

    public enum TransactionType {
        INCOME,   // Доход
        EXPENSE   // Расход
    }
}

package com.vanilla.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status;

    private UUID waiterId;

    private Instant statusUpdatedAt;

    public enum TableStatus {
        FREE,            // Свободен
        OCCUPIED,        // Занят
        AWAITING_FOOD,   // Ожидает блюда
        PAYMENT          // Оплата
    }
}

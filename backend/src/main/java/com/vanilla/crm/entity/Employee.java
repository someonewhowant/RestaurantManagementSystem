package com.vanilla.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    private String salaryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private LocalDate hireDate;

    private LocalDate fireDate;

    private LocalDate vacationStart;

    private LocalDate vacationEnd;

    @Builder.Default
    private Boolean onShift = false;

    private Instant shiftStartTime;

    public enum EmployeeRole {
        MANAGER,    // Менеджер
        WAITER,     // Официант
        COOK,       // Повар
        CASHIER     // Кассир
    }

    public enum EmployeeStatus {
        ACTIVE,       // Активен
        ON_VACATION,  // В отпуске
        FIRED         // Уволен
    }
}

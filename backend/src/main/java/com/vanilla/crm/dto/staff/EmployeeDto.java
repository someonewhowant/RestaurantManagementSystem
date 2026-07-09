package com.vanilla.crm.dto.staff;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Map;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@Builder
public class EmployeeDto {
    private UUID id;
    
    @NotBlank(message = "Имя сотрудника не может быть пустым")
    private String name;
    
    @NotBlank(message = "Телефон обязателен")
    private String phone;
    
    private String email;
    
    @PositiveOrZero(message = "Зарплата не может быть отрицательной")
    private BigDecimal salary;
    
    private String salaryDate;
    
    @NotBlank(message = "Роль обязательна")
    private String role;      // "Менеджер", "Официант", "Повар", "Кассир"
    
    private String status;    // "Активен", "В отпуске", "Уволен"
    private String hireDate;
    private String fireDate;
    private String vacationStart;
    private String vacationEnd;
    private Boolean onShift;
    private String shiftStartTime;

}

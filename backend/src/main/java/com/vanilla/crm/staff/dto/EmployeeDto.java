package com.vanilla.crm.staff.dto;

import com.vanilla.crm.staff.entity.Employee;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class EmployeeDto {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String role;      // "Менеджер", "Официант", "Повар", "Кассир"
    private String status;    // "Активен", "В отпуске", "Уволен"
    private String hireDate;
    private String fireDate;
    private String vacationStart;
    private String vacationEnd;
    private Boolean onShift;
    private String shiftStartTime;

    // Mapping from Russian labels used in the frontend to Java enums
    private static final java.util.Map<String, Employee.EmployeeRole> ROLE_MAP = java.util.Map.of(
            "Менеджер", Employee.EmployeeRole.MANAGER,
            "Официант", Employee.EmployeeRole.WAITER,
            "Повар", Employee.EmployeeRole.COOK,
            "Кассир", Employee.EmployeeRole.CASHIER
    );

    private static final java.util.Map<Employee.EmployeeRole, String> ROLE_REVERSE = java.util.Map.of(
            Employee.EmployeeRole.MANAGER, "Менеджер",
            Employee.EmployeeRole.WAITER, "Официант",
            Employee.EmployeeRole.COOK, "Повар",
            Employee.EmployeeRole.CASHIER, "Кассир"
    );

    private static final java.util.Map<String, Employee.EmployeeStatus> STATUS_MAP = java.util.Map.of(
            "Активен", Employee.EmployeeStatus.ACTIVE,
            "В отпуске", Employee.EmployeeStatus.ON_VACATION,
            "Уволен", Employee.EmployeeStatus.FIRED
    );

    private static final java.util.Map<Employee.EmployeeStatus, String> STATUS_REVERSE = java.util.Map.of(
            Employee.EmployeeStatus.ACTIVE, "Активен",
            Employee.EmployeeStatus.ON_VACATION, "В отпуске",
            Employee.EmployeeStatus.FIRED, "Уволен"
    );

    public static EmployeeDto fromEntity(Employee emp) {
        return EmployeeDto.builder()
                .id(emp.getId())
                .name(emp.getName())
                .phone(emp.getPhone())
                .email(emp.getEmail())
                .role(ROLE_REVERSE.getOrDefault(emp.getRole(), emp.getRole().name()))
                .status(STATUS_REVERSE.getOrDefault(emp.getStatus(), emp.getStatus().name()))
                .hireDate(emp.getHireDate() != null ? emp.getHireDate().toString() : null)
                .fireDate(emp.getFireDate() != null ? emp.getFireDate().toString() : null)
                .vacationStart(emp.getVacationStart() != null ? emp.getVacationStart().toString() : null)
                .vacationEnd(emp.getVacationEnd() != null ? emp.getVacationEnd().toString() : null)
                .onShift(emp.getOnShift())
                .shiftStartTime(emp.getShiftStartTime() != null ? emp.getShiftStartTime().toString() : null)
                .build();
    }

    public static Employee.EmployeeRole toRoleEnum(String role) {
        if (role == null) return Employee.EmployeeRole.WAITER;
        return ROLE_MAP.getOrDefault(role, Employee.EmployeeRole.WAITER);
    }

    public static Employee.EmployeeStatus toStatusEnum(String status) {
        if (status == null) return Employee.EmployeeStatus.ACTIVE;
        return STATUS_MAP.getOrDefault(status, Employee.EmployeeStatus.ACTIVE);
    }
}

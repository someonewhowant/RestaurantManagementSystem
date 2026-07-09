package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.staff.EmployeeDto;
import com.vanilla.crm.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StaffMapper {

    private static final Map<String, Employee.EmployeeRole> ROLE_MAP = Map.of(
            "Менеджер", Employee.EmployeeRole.MANAGER,
            "Официант", Employee.EmployeeRole.WAITER,
            "Повар", Employee.EmployeeRole.COOK,
            "Кассир", Employee.EmployeeRole.CASHIER
    );

    private static final Map<Employee.EmployeeRole, String> ROLE_REVERSE = Map.of(
            Employee.EmployeeRole.MANAGER, "Менеджер",
            Employee.EmployeeRole.WAITER, "Официант",
            Employee.EmployeeRole.COOK, "Повар",
            Employee.EmployeeRole.CASHIER, "Кассир"
    );

    private static final Map<String, Employee.EmployeeStatus> STATUS_MAP = Map.of(
            "Активен", Employee.EmployeeStatus.ACTIVE,
            "В отпуске", Employee.EmployeeStatus.ON_VACATION,
            "Уволен", Employee.EmployeeStatus.FIRED
    );

    private static final Map<Employee.EmployeeStatus, String> STATUS_REVERSE = Map.of(
            Employee.EmployeeStatus.ACTIVE, "Активен",
            Employee.EmployeeStatus.ON_VACATION, "В отпуске",
            Employee.EmployeeStatus.FIRED, "Уволен"
    );

    public EmployeeDto toDto(Employee emp) {
        if (emp == null) return null;
        return EmployeeDto.builder()
                .id(emp.getId())
                .name(emp.getName())
                .phone(emp.getPhone())
                .email(emp.getEmail())
                .salary(emp.getSalary())
                .salaryDate(emp.getSalaryDate())
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

    public Employee.EmployeeRole toRoleEnum(String role) {
        if (role == null) return Employee.EmployeeRole.WAITER;
        return ROLE_MAP.getOrDefault(role, Employee.EmployeeRole.WAITER);
    }

    public Employee.EmployeeStatus toStatusEnum(String status) {
        if (status == null) return Employee.EmployeeStatus.ACTIVE;
        return STATUS_MAP.getOrDefault(status, Employee.EmployeeStatus.ACTIVE);
    }
}

package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.Employee;
import com.vanilla.crm.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@Order(30)
@RequiredArgsConstructor
@Slf4j
public class StaffDataSeeder implements DataSeeder {

    private final StaffRepository staffRepository;

    @Override
    public void seed() {
        if (staffRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding staff data...");
        List<Employee> employees = List.of(
            Employee.builder().name("Александр Иванов").role(Employee.EmployeeRole.MANAGER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2025, 1, 15)).onShift(true).shiftStartTime(Instant.now()).build(),
            Employee.builder().name("Мария Смирнова").role(Employee.EmployeeRole.WAITER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2025, 3, 22)).onShift(false).build(),
            Employee.builder().name("Дмитрий Кузнецов").role(Employee.EmployeeRole.COOK).status(Employee.EmployeeStatus.ON_VACATION).hireDate(LocalDate.of(2024, 11, 5)).vacationStart(LocalDate.of(2026, 6, 1)).vacationEnd(LocalDate.of(2026, 6, 30)).onShift(false).build(),
            Employee.builder().name("Анна Попова").role(Employee.EmployeeRole.CASHIER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2026, 2, 10)).onShift(true).shiftStartTime(Instant.now().minusSeconds(3600)).build()
        );
        staffRepository.saveAll(employees);
        log.info("Seeded {} employees.", employees.size());
    }
}

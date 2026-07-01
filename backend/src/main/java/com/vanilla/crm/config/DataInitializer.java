package com.vanilla.crm.config;

import com.vanilla.crm.budget.TransactionRepository;
import com.vanilla.crm.budget.entity.Transaction;
import com.vanilla.crm.inventory.InventoryRepository;
import com.vanilla.crm.inventory.entity.InventoryItem;
import com.vanilla.crm.menu.MenuRepository;
import com.vanilla.crm.menu.entity.Dish;
import com.vanilla.crm.staff.StaffRepository;
import com.vanilla.crm.staff.entity.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final InventoryRepository inventoryRepository;
    private final StaffRepository staffRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (menuRepository.count() == 0) {
            log.info("Database is empty. Seeding menu data...");
            seedMenu();
        }
        
        if (inventoryRepository.count() == 0) {
            log.info("Database is empty. Seeding inventory data...");
            seedInventory();
        }

        if (staffRepository.count() == 0) {
            log.info("Database is empty. Seeding staff data...");
            seedStaff();
        }

        if (transactionRepository.count() == 0) {
            log.info("Database is empty. Seeding budget data...");
            seedBudget();
        }
    }

    private void seedInventory() {
        List<InventoryItem> items = List.of(
            InventoryItem.builder().name("Лосось").category("Морепродукты").currentStock(2.0).minStock(5.0).unit("кг").pricePerUnit(new BigDecimal("1200")).expiresInDays(2).build(),
            InventoryItem.builder().name("Говядина").category("Мясо").currentStock(15.0).minStock(10.0).unit("кг").pricePerUnit(new BigDecimal("850")).build(),
            InventoryItem.builder().name("Картофель").category("Овощи").currentStock(40.0).minStock(20.0).unit("кг").pricePerUnit(new BigDecimal("40")).build(),
            InventoryItem.builder().name("Помидоры").category("Овощи").currentStock(18.0).minStock(15.0).unit("кг").pricePerUnit(new BigDecimal("150")).expiresInDays(1).build(),
            InventoryItem.builder().name("Оливковое масло").category("Бакалея").currentStock(12.0).minStock(5.0).unit("л").pricePerUnit(new BigDecimal("800")).build(),
            InventoryItem.builder().name("Соль").category("Бакалея").currentStock(1.0).minStock(3.0).unit("кг").pricePerUnit(new BigDecimal("30")).build(),
            InventoryItem.builder().name("Сливки 33%").category("Молочка").currentStock(5.0).minStock(3.0).unit("л").pricePerUnit(new BigDecimal("300")).expiresInDays(0).build()
        );
        inventoryRepository.saveAll(items);
        log.info("Seeded {} inventory items.", items.size());
    }

    private void seedMenu() {
        List<Dish> initialDishes = List.of(
            Dish.builder().name("Стейк Рибай").category("Горячее").price(new BigDecimal("2500")).weight("350г").imageIcon("🥩").build(),
            Dish.builder().name("Паста Карбонара").category("Горячее").price(new BigDecimal("650")).weight("300г").imageIcon("🍝").build(),
            Dish.builder().name("Бургер классический").category("Горячее").price(new BigDecimal("550")).weight("400г").imageIcon("🍔").build(),
            Dish.builder().name("Цезарь с курицей").category("Закуски").price(new BigDecimal("480")).weight("250г").imageIcon("🥗").build(),
            Dish.builder().name("Сырная тарелка").category("Закуски").price(new BigDecimal("850")).weight("200г").imageIcon("🧀").build(),
            Dish.builder().name("Лимонад").category("Напитки").price(new BigDecimal("250")).weight("400мл").imageIcon("🍹").build(),
            Dish.builder().name("Капучино").category("Напитки").price(new BigDecimal("220")).weight("250мл").imageIcon("☕").build(),
            Dish.builder().name("Чизкейк").category("Десерты").price(new BigDecimal("380")).weight("150г").imageIcon("🍰").build(),
            Dish.builder().name("Тирамису").category("Десерты").price(new BigDecimal("420")).weight("180г").imageIcon("🍮").build()
        );

        menuRepository.saveAll(initialDishes);
        log.info("Seeded {} dishes.", initialDishes.size());
    }

    private void seedStaff() {
        List<Employee> employees = List.of(
            Employee.builder().name("Александр Иванов").role(Employee.EmployeeRole.MANAGER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2025, 1, 15)).onShift(true).shiftStartTime(Instant.now()).build(),
            Employee.builder().name("Мария Смирнова").role(Employee.EmployeeRole.WAITER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2025, 3, 22)).onShift(false).build(),
            Employee.builder().name("Дмитрий Кузнецов").role(Employee.EmployeeRole.COOK).status(Employee.EmployeeStatus.ON_VACATION).hireDate(LocalDate.of(2024, 11, 5)).vacationStart(LocalDate.of(2026, 6, 1)).vacationEnd(LocalDate.of(2026, 6, 30)).onShift(false).build(),
            Employee.builder().name("Анна Попова").role(Employee.EmployeeRole.CASHIER).status(Employee.EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2026, 2, 10)).onShift(true).shiftStartTime(Instant.now().minusSeconds(3600)).build()
        );
        staffRepository.saveAll(employees);
        log.info("Seeded {} employees.", employees.size());
    }

    private void seedBudget() {
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

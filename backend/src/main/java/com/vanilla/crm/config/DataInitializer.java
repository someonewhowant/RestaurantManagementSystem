package com.vanilla.crm.config;

import com.vanilla.crm.auth.UserRepository;
import com.vanilla.crm.auth.entity.User;
import com.vanilla.crm.budget.TransactionRepository;
import com.vanilla.crm.budget.entity.Transaction;
import com.vanilla.crm.inventory.InventoryRepository;
import com.vanilla.crm.inventory.entity.InventoryItem;
import com.vanilla.crm.menu.MenuRepository;
import com.vanilla.crm.menu.entity.Dish;
import com.vanilla.crm.menu.entity.RecipeIngredient;
import com.vanilla.crm.staff.StaffRepository;
import com.vanilla.crm.staff.entity.Employee;
import com.vanilla.crm.tables.TableRepository;
import com.vanilla.crm.tables.entity.RestaurantTable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (menuRepository.count() == 0 && inventoryRepository.count() == 0) {
            log.info("Database is empty. Seeding inventory and menu data...");
            seedInventory();
            seedMenuAndRecipes();
        } else {
            if (inventoryRepository.count() == 0) {
                log.info("Database is empty. Seeding inventory data...");
                seedInventory();
            }
            if (menuRepository.count() == 0) {
                log.info("Database is empty. Seeding menu data...");
                seedMenuAndRecipes();
            }
        }

        if (staffRepository.count() == 0) {
            log.info("Database is empty. Seeding staff data...");
            seedStaff();
        }

        if (transactionRepository.count() == 0) {
            log.info("Database is empty. Seeding budget data...");
            seedBudget();
        }

        if (userRepository.count() == 0) {
            log.info("Database is empty. Seeding admin user...");
            seedUsers();
        }

        if (tableRepository.count() == 0) {
            log.info("Database is empty. Seeding tables data...");
            seedTables();
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

    private void seedMenuAndRecipes() {
        Dish dish1 = Dish.builder()
                .name("Стейк Рибай")
                .category("Горячее")
                .price(new BigDecimal("2500"))
                .status(Dish.DishStatus.AVAILABLE)
                .instructions("Прожарка medium rare, подавать с чесночным маслом")
                .allergens(List.of("Мясо"))
                .macros(com.vanilla.crm.menu.entity.Macros.builder().calories(650.0).protein(45.0).carbs(0.0).fats(50.0).build())
                .build();

        Dish dish2 = Dish.builder()
                .name("Салат Цезарь")
                .category("Салаты")
                .price(new BigDecimal("450"))
                .status(Dish.DishStatus.AVAILABLE)
                .instructions("Соус отдельно")
                .allergens(List.of("Яйцо", "Глютен"))
                .macros(com.vanilla.crm.menu.entity.Macros.builder().calories(320.0).protein(12.0).carbs(15.0).fats(25.0).build())
                .build();

        List<Dish> dishes = menuRepository.saveAll(List.of(dish1, dish2));

        List<InventoryItem> items = inventoryRepository.findAll();
        if (!items.isEmpty()) {
            InventoryItem meat = items.stream().filter(i -> i.getName().equals("Говядина")).findFirst().orElse(items.get(0));
            InventoryItem lettuce = items.stream().filter(i -> i.getName().equals("Помидоры")).findFirst().orElse(items.get(0));
            InventoryItem tomatoes = items.stream().filter(i -> i.getName().equals("Помидоры")).findFirst().orElse(items.get(0));

            Dish savedDish1 = dishes.get(0);
            savedDish1.getRecipe().add(RecipeIngredient.builder().dish(savedDish1).inventoryItem(meat).amount(0.3).build());
            
            Dish savedDish2 = dishes.get(1);
            savedDish2.getRecipe().add(RecipeIngredient.builder().dish(savedDish2).inventoryItem(lettuce).amount(0.1).build());
            savedDish2.getRecipe().add(RecipeIngredient.builder().dish(savedDish2).inventoryItem(tomatoes).amount(0.05).build());

            menuRepository.saveAll(dishes);
        }

        log.info("Seeded {} dishes with recipes.", dishes.size());
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

    private void seedTables() {
        List<RestaurantTable> tables = List.of(
            RestaurantTable.builder().number(1).capacity(2).status(RestaurantTable.TableStatus.FREE).build(),
            RestaurantTable.builder().number(2).capacity(2).status(RestaurantTable.TableStatus.OCCUPIED).statusUpdatedAt(Instant.now().minusSeconds(15 * 60)).build(),
            RestaurantTable.builder().number(3).capacity(4).status(RestaurantTable.TableStatus.AWAITING_FOOD).statusUpdatedAt(Instant.now().minusSeconds(25 * 60)).build(),
            RestaurantTable.builder().number(4).capacity(4).status(RestaurantTable.TableStatus.FREE).build(),
            RestaurantTable.builder().number(5).capacity(6).status(RestaurantTable.TableStatus.PAYMENT).statusUpdatedAt(Instant.now().minusSeconds(5 * 60)).build(),
            RestaurantTable.builder().number(6).capacity(8).status(RestaurantTable.TableStatus.FREE).build(),
            RestaurantTable.builder().number(7).capacity(2).status(RestaurantTable.TableStatus.OCCUPIED).statusUpdatedAt(Instant.now().minusSeconds(40 * 60)).build(),
            RestaurantTable.builder().number(8).capacity(4).status(RestaurantTable.TableStatus.FREE).build()
        );
        tableRepository.saveAll(tables);
        log.info("Seeded {} tables.", tables.size());
    }

    private void seedUsers() {
        User admin = User.builder()
                .email("admin@vanilla.crm")
                .passwordHash(passwordEncoder.encode("admin"))
                .firstName("Иван")
                .lastName("Иванов")
                .restaurantName("Ресторан Vanilla")
                .role(User.Role.OWNER)
                .build();
        userRepository.save(admin);
        log.info("Seeded default admin user (admin@vanilla.crm / admin).");
    }
}

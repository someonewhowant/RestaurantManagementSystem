package com.vanilla.crm.config;

import com.vanilla.crm.menu.MenuRepository;
import com.vanilla.crm.menu.entity.Dish;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;

    @Override
    public void run(String... args) throws Exception {
        if (menuRepository.count() == 0) {
            log.info("Database is empty. Seeding initial data...");
            seedMenu();
        } else {
            log.info("Database already contains data. Skipping seed.");
        }
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
}

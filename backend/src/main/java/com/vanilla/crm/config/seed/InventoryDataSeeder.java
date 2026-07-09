package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.InventoryItem;
import com.vanilla.crm.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class InventoryDataSeeder implements DataSeeder {

    private final InventoryRepository inventoryRepository;

    @Override
    public void seed() {
        if (inventoryRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding inventory data...");
        List<InventoryItem> items = List.of(
            InventoryItem.builder().name("Лосось").category("Морепродукты").currentStock(2.0).minStock(5.0).unit("кг").pricePerUnit(new BigDecimal("1200")).expiresInDays(2).build(),
            InventoryItem.builder().name("Говядина").category("Мясо").currentStock(15.0).minStock(10.0).unit("кг").pricePerUnit(new BigDecimal("850")).build(),
            InventoryItem.builder().name("Картофель").category("Овощи").currentStock(40.0).minStock(20.0).unit("кг").pricePerUnit(new BigDecimal("40")).build(),
            InventoryItem.builder().name("Помидоры").category("Овощи").currentStock(18.0).minStock(15.0).unit("кг").pricePerUnit(new BigDecimal("150")).expiresInDays(1).build(),
            InventoryItem.builder().name("Оливковое масло").category("Бакалея").currentStock(12.0).minStock(5.0).unit("л").pricePerUnit(new BigDecimal("800")).build(),
            InventoryItem.builder().name("Соль").category("Бакалея").currentStock(1.0).minStock(3.0).unit("кг").pricePerUnit(new BigDecimal("30")).build(),
            InventoryItem.builder().name("Сливки 33%").category("Молочка").currentStock(5.0).minStock(3.0).unit("л").pricePerUnit(new BigDecimal("300")).expiresInDays(0).build(),
            InventoryItem.builder().name("Кофе в зернах").category("Напитки").currentStock(3.0).minStock(2.0).unit("кг").pricePerUnit(new BigDecimal("1500")).build()
        );
        inventoryRepository.saveAll(items);
        log.info("Seeded {} inventory items.", items.size());
    }
}

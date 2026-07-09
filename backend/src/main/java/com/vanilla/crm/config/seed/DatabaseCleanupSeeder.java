package com.vanilla.crm.config.seed;

import com.vanilla.crm.repository.InventoryRepository;
import com.vanilla.crm.repository.MenuRepository;
import com.vanilla.crm.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupSeeder implements DataSeeder {

    private final MenuRepository menuRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Override
    public void seed() {
        if (menuRepository.count() > 0 && menuRepository.count() < 5) {
            log.info("Detected old test data. Clearing database to seed new full data...");
            orderRepository.deleteAll(); // Delete orders first due to foreign key constraints on dishes
            menuRepository.deleteAll();
            inventoryRepository.deleteAll();
        }
    }
}

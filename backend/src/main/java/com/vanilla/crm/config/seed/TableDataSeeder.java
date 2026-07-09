package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.RestaurantTable;
import com.vanilla.crm.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Order(60)
@RequiredArgsConstructor
@Slf4j
public class TableDataSeeder implements DataSeeder {

    private final TableRepository tableRepository;

    @Override
    public void seed() {
        if (tableRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding tables data...");
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
}

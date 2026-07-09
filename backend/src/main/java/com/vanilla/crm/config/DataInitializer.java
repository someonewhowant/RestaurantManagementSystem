package com.vanilla.crm.config;

import com.vanilla.crm.config.seed.DataSeeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final List<DataSeeder> seeders;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization with {} seeders...", seeders.size());
        for (DataSeeder seeder : seeders) {
            seeder.seed();
        }
        log.info("Data initialization completed.");
    }
}

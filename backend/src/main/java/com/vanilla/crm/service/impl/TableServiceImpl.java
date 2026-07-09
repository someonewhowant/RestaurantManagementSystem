package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;

import com.vanilla.crm.repository.TableRepository;

import com.vanilla.crm.dto.tables.TableDto;
import com.vanilla.crm.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.vanilla.crm.service.TableService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    @Override
    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(TableDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TableDto changeStatus(UUID id, String newStatus, UUID waiterId) {
        log.info("Changing status of table {} to {}", id, newStatus);
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        RestaurantTable.TableStatus status = TableDto.toStatusEnum(newStatus);
        table.setStatus(status);
        table.setStatusUpdatedAt(Instant.now());

        // If table is freed, remove waiter assignment
        if (status == RestaurantTable.TableStatus.FREE) {
            table.setWaiterId(null);
        } else if (waiterId != null) {
            table.setWaiterId(waiterId);
        }

        return TableDto.fromEntity(tableRepository.save(table));
    }

    @Transactional
    @Override
    public TableDto assignWaiter(UUID id, UUID waiterId) {
        log.info("Assigning waiter {} to table {}", waiterId, id);
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        table.setWaiterId(waiterId);
        return TableDto.fromEntity(tableRepository.save(table));
    }

    @Transactional
    @Override
    public TableDto createTable(TableDto dto) {
        log.info("Creating new table with number {}", dto.getNumber());
        RestaurantTable table = RestaurantTable.builder()
                .number(dto.getNumber())
                .capacity(dto.getCapacity())
                .status(RestaurantTable.TableStatus.FREE)
                .statusUpdatedAt(Instant.now())
                .build();
        return TableDto.fromEntity(tableRepository.save(table));
    }

    @Transactional
    @Override
    public TableDto updateTable(UUID id, TableDto dto) {
        log.info("Updating table {}", id);
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        if (dto.getNumber() != null) table.setNumber(dto.getNumber());
        if (dto.getCapacity() != null) table.setCapacity(dto.getCapacity());
        return TableDto.fromEntity(tableRepository.save(table));
    }

    @Transactional
    @Override
    public void deleteTable(UUID id) {
        log.info("Deleting table {}", id);
        tableRepository.deleteById(id);
    }
}

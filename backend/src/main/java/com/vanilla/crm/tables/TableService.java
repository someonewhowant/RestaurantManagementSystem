package com.vanilla.crm.tables;

import com.vanilla.crm.tables.dto.TableDto;
import com.vanilla.crm.tables.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<TableDto> getAllTables() {
        return tableRepository.findAll().stream()
                .map(TableDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TableDto changeStatus(UUID id, String newStatus, UUID waiterId) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));

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
    public TableDto assignWaiter(UUID id, UUID waiterId) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        table.setWaiterId(waiterId);
        return TableDto.fromEntity(tableRepository.save(table));
    }
}

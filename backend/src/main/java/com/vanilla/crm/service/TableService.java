package com.vanilla.crm.service;

import com.vanilla.crm.dto.tables.TableDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface TableService {
    List<TableDto> getAllTables();
    TableDto changeStatus(UUID id, String newStatus, UUID waiterId);
    TableDto assignWaiter(UUID id, UUID waiterId);
    TableDto createTable(TableDto dto);
    TableDto updateTable(UUID id, TableDto dto);
    void deleteTable(UUID id);
}

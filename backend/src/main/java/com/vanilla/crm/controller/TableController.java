package com.vanilla.crm.controller;

import com.vanilla.crm.service.TableService;

import com.vanilla.crm.dto.tables.TableDto;
import com.vanilla.crm.dto.tables.TableStatusRequest;
import com.vanilla.crm.dto.tables.WaiterAssignRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
@Tag(name = "Столики", description = "Управление столиками зала: статусы, назначение официантов")
public class TableController {

    private final TableService tableService;

    @Operation(summary = "Все столики", description = "Возвращает список всех столиков с их текущим статусом и назначенным официантом.")
    @ApiResponse(responseCode = "200", description = "Список столиков")
    @GetMapping
    public ResponseEntity<List<TableDto>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @Operation(summary = "Изменить статус столика", description = "Обновляет статус столика (free, occupied, awaiting_food, payment).")
    @ApiResponse(responseCode = "200", description = "Обновлённый столик")
    @ApiResponse(responseCode = "404", description = "Столик не найден")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TableDto> changeStatus(@PathVariable UUID id, @RequestBody TableStatusRequest request) {
        return ResponseEntity.ok(tableService.changeStatus(id, request.getStatus(), request.getWaiterId()));
    }

    @Operation(summary = "Назначить официанта", description = "Привязывает официанта к столику.")
    @ApiResponse(responseCode = "200", description = "Обновлённый столик")
    @ApiResponse(responseCode = "404", description = "Столик не найден")
    @PatchMapping("/{id}/waiter")
    public ResponseEntity<TableDto> assignWaiter(@PathVariable UUID id, @RequestBody WaiterAssignRequest request) {
        return ResponseEntity.ok(tableService.assignWaiter(id, request.getWaiterId()));
    }

    @Operation(summary = "Создать столик", description = "Добавляет новый столик в зал.")
    @ApiResponse(responseCode = "200", description = "Созданный столик")
    @PostMapping
    public ResponseEntity<TableDto> createTable(@RequestBody TableDto dto) {
        return ResponseEntity.ok(tableService.createTable(dto));
    }

    @Operation(summary = "Обновить столик", description = "Обновляет номер или вместимость столика.")
    @ApiResponse(responseCode = "200", description = "Обновлённый столик")
    @PutMapping("/{id}")
    public ResponseEntity<TableDto> updateTable(@PathVariable UUID id, @RequestBody TableDto dto) {
        return ResponseEntity.ok(tableService.updateTable(id, dto));
    }

    @Operation(summary = "Удалить столик", description = "Удаляет столик из системы.")
    @ApiResponse(responseCode = "204", description = "Столик удалён")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable UUID id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}

package com.vanilla.crm.controller;

import com.vanilla.crm.service.InventoryService;

import com.vanilla.crm.dto.inventory.AmountRequest;
import com.vanilla.crm.dto.inventory.BatchConsumeRequest;
import com.vanilla.crm.dto.inventory.InventoryItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vanilla.crm.util.CsvExportUtil;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Склад", description = "Управление запасами ингредиентов и расходных материалов")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Все позиции склада", description = "Возвращает полный список позиций на складе.")
    @ApiResponse(responseCode = "200", description = "Список позиций")
    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @Operation(summary = "Позиции с низким остатком", description = "Возвращает позиции, у которых текущее количество ниже минимального порога.")
    @ApiResponse(responseCode = "200", description = "Список позиций с низким остатком")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemDto>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @Operation(summary = "Истекающие позиции", description = "Возвращает позиции, срок годности которых скоро истечёт.")
    @ApiResponse(responseCode = "200", description = "Список истекающих позиций")
    @GetMapping("/expiring")
    public ResponseEntity<List<InventoryItemDto>> getExpiringItems() {
        return ResponseEntity.ok(inventoryService.getExpiringItems());
    }

    @Operation(summary = "Добавить позицию", description = "Создаёт новую позицию на складе.")
    @ApiResponse(responseCode = "200", description = "Созданная позиция")
    @PostMapping
    public ResponseEntity<InventoryItemDto> createItem(@Valid @RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.createItem(dto));
    }

    @Operation(summary = "Обновить позицию", description = "Полное обновление данных позиции по ID.")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "404", description = "Позиция не найдена")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(@PathVariable UUID id, @Valid @RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.updateItem(id, dto));
    }

    @Operation(summary = "Пополнить запас", description = "Увеличивает количество позиции на складе (приход товара).")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "404", description = "Позиция не найдена")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<InventoryItemDto> restock(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.restock(id, request.getAmount()));
    }

    @Operation(summary = "Списать со склада", description = "Уменьшает количество позиции на складе (расход/списание).")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "400", description = "Недостаточно запасов")
    @PatchMapping("/{id}/consume")
    public ResponseEntity<InventoryItemDto> consume(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.consume(id, request.getAmount()));
    }

    @Operation(summary = "Пакетное списание", description = "Списывает несколько позиций одновременно.")
    @ApiResponse(responseCode = "200", description = "Списание успешно")
    @PostMapping("/consume-batch")
    public ResponseEntity<Void> consumeBatch(@Valid @RequestBody BatchConsumeRequest request) {
        inventoryService.consumeBatch(request.getItems());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить позицию", description = "Полностью удаляет позицию со склада.")
    @ApiResponse(responseCode = "204", description = "Позиция удалена")
    @ApiResponse(responseCode = "404", description = "Позиция не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Экспорт в CSV", description = "Скачать остатки на складе в формате CSV.")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return CsvExportUtil.buildCsvResponse(inventoryService.exportCsv(), "inventory_report.csv");
    }
}

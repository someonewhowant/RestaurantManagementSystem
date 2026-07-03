package com.vanilla.crm.inventory;

import com.vanilla.crm.inventory.dto.AmountRequest;
import com.vanilla.crm.inventory.dto.InventoryItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.io.StringWriter;

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
    public ResponseEntity<InventoryItemDto> createItem(@RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.createItem(dto));
    }

    @Operation(summary = "Обновить позицию", description = "Полное обновление данных позиции по ID.")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "404", description = "Позиция не найдена")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(@PathVariable UUID id, @RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.updateItem(id, dto));
    }

    @Operation(summary = "Пополнить запас", description = "Увеличивает количество позиции на складе (приход товара).")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "404", description = "Позиция не найдена")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<InventoryItemDto> restock(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.restock(id, request.getAmount()));
    }

    @Operation(summary = "Списать со склада", description = "Уменьшает количество позиции на складе (расход/списание).")
    @ApiResponse(responseCode = "200", description = "Обновлённая позиция")
    @ApiResponse(responseCode = "400", description = "Недостаточно запасов")
    @PatchMapping("/{id}/consume")
    public ResponseEntity<InventoryItemDto> consume(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.consume(id, request.getAmount()));
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
        List<InventoryItemDto> items = inventoryService.getAllItems();
        StringWriter writer = new StringWriter();
        writer.append("ID;Название;Категория;Текущий остаток;Мин. остаток;Единица;Цена за ед.\n");
        for (InventoryItemDto item : items) {
            writer.append(String.format("%s;%s;%s;%s;%s;%s;%s\n",
                    item.getId(),
                    item.getName() != null ? item.getName().replace(";", " ") : "",
                    item.getCategory() != null ? item.getCategory().replace(";", " ") : "",
                    item.getCurrentStock(),
                    item.getMinStock(),
                    item.getUnit() != null ? item.getUnit().replace(";", " ") : "",
                    item.getPricePerUnit()));
        }

        byte[] textBytes = writer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] csvBytes = new byte[textBytes.length + 3];
        csvBytes[0] = (byte) 0xEF;
        csvBytes[1] = (byte) 0xBB;
        csvBytes[2] = (byte) 0xBF;
        System.arraycopy(textBytes, 0, csvBytes, 3, textBytes.length);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "inventory_report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}

package com.vanilla.crm.inventory;

import com.vanilla.crm.inventory.dto.AmountRequest;
import com.vanilla.crm.inventory.dto.InventoryItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemDto>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<InventoryItemDto>> getExpiringItems() {
        return ResponseEntity.ok(inventoryService.getExpiringItems());
    }

    @PostMapping
    public ResponseEntity<InventoryItemDto> createItem(@RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.createItem(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(@PathVariable UUID id, @RequestBody InventoryItemDto dto) {
        return ResponseEntity.ok(inventoryService.updateItem(id, dto));
    }

    @PatchMapping("/{id}/restock")
    public ResponseEntity<InventoryItemDto> restock(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.restock(id, request.getAmount()));
    }

    @PatchMapping("/{id}/consume")
    public ResponseEntity<InventoryItemDto> consume(@PathVariable UUID id, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(inventoryService.consume(id, request.getAmount()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}

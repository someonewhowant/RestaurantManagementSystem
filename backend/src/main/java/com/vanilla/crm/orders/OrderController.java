package com.vanilla.crm.orders;

import com.vanilla.crm.orders.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** Get active order for a table */
    @GetMapping
    public ResponseEntity<OrderDto> getOrderForTable(@RequestParam UUID tableId) {
        return ResponseEntity.ok(orderService.getOrderForTable(tableId));
    }

    /** Get all orders visible on the kitchen display */
    @GetMapping("/kitchen")
    public ResponseEntity<List<OrderDto>> getKitchenOrders() {
        return ResponseEntity.ok(orderService.getKitchenOrders());
    }

    /** Create (or get existing) active order for a table */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(orderService.createOrder(body.get("tableId")));
    }

    /** Add a dish to the active order for a table */
    @PostMapping("/table/{tableId}/items")
    public ResponseEntity<OrderDto> addItem(
            @PathVariable UUID tableId,
            @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(orderService.addItem(
                tableId, request.getDishId(), request.getQuantity() != null ? request.getQuantity() : 1));
    }

    /** Remove an item from an order */
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderDto> removeItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return ResponseEntity.ok(orderService.removeItem(orderId, itemId));
    }

    /** Send all NEW items to the kitchen */
    @PatchMapping("/{orderId}/send-to-kitchen")
    public ResponseEntity<OrderDto> sendToKitchen(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.sendToKitchen(orderId));
    }

    /** Update status of a single item (cooking → ready → served) */
    @PatchMapping("/{orderId}/items/{itemId}/status")
    public ResponseEntity<OrderDto> updateItemStatus(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @RequestBody ItemStatusRequest request) {
        return ResponseEntity.ok(orderService.updateItemStatus(orderId, itemId, request.getStatus()));
    }

    /** Close order: calculate total, create budget transaction */
    @PostMapping("/{orderId}/close")
    public ResponseEntity<OrderDto> closeOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.closeOrder(orderId));
    }

    /** Cancel/clear an active order for a table */
    @DeleteMapping("/table/{tableId}")
    public ResponseEntity<Void> clearOrder(@PathVariable UUID tableId) {
        orderService.clearOrder(tableId);
        return ResponseEntity.noContent().build();
    }

    /** Get calculated total for the active order at a table */
    @GetMapping("/table/{tableId}/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotal(@PathVariable UUID tableId) {
        return ResponseEntity.ok(Map.of("total", orderService.getTotal(tableId)));
    }
}

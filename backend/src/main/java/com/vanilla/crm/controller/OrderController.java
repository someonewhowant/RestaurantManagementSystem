package com.vanilla.crm.controller;

import com.vanilla.crm.service.OrderService;

import com.vanilla.crm.dto.orders.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Заказы", description = "Полный цикл работы с заказами: создание, кухня, оплата, списание")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Заказ по столику", description = "Получить активный заказ для указанного столика.")
    @ApiResponse(responseCode = "200", description = "Активный заказ")
    @ApiResponse(responseCode = "404", description = "Активный заказ не найден")
    @GetMapping
    public ResponseEntity<OrderDto> getOrderForTable(
            @Parameter(description = "UUID столика") @RequestParam UUID tableId) {
        return ResponseEntity.ok(orderService.getOrderForTable(tableId));
    }

    @Operation(summary = "Заказы для кухни", description = "Список всех заказов со статусом IN_PROGRESS для кухонного дисплея.")
    @ApiResponse(responseCode = "200", description = "Список заказов на кухне")
    @GetMapping("/kitchen")
    public ResponseEntity<List<OrderDto>> getKitchenOrders() {
        return ResponseEntity.ok(orderService.getKitchenOrders());
    }

    @Operation(summary = "Создать заказ", description = "Создаёт новый заказ для столика или возвращает существующий активный.")
    @ApiResponse(responseCode = "200", description = "Созданный или существующий заказ")
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(orderService.createOrder(body.get("tableId")));
    }

    @Operation(summary = "Добавить блюдо в заказ", description = "Добавляет позицию в активный заказ для столика.")
    @ApiResponse(responseCode = "200", description = "Обновлённый заказ")
    @ApiResponse(responseCode = "404", description = "Столик или блюдо не найдены")
    @PostMapping("/table/{tableId}/items")
    public ResponseEntity<OrderDto> addItem(
            @PathVariable UUID tableId,
            @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(orderService.addItem(
                tableId, request.getDishId(), request.getQuantity() != null ? request.getQuantity() : 1));
    }

    @Operation(summary = "Удалить позицию", description = "Удаляет позицию из заказа по ID.")
    @ApiResponse(responseCode = "200", description = "Обновлённый заказ")
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<OrderDto> removeItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return ResponseEntity.ok(orderService.removeItem(orderId, itemId));
    }

    @Operation(summary = "Отправить на кухню", description = "Отправляет все новые позиции заказа в работу на кухню.")
    @ApiResponse(responseCode = "200", description = "Заказ отправлен")
    @PatchMapping("/{orderId}/send-to-kitchen")
    public ResponseEntity<OrderDto> sendToKitchen(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.sendToKitchen(orderId));
    }

    @Operation(summary = "Обновить статус позиции", description = "Смена статуса позиции: cooking → ready → served.")
    @ApiResponse(responseCode = "200", description = "Обновлённый заказ")
    @PatchMapping("/{orderId}/items/{itemId}/status")
    public ResponseEntity<OrderDto> updateItemStatus(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @RequestBody ItemStatusRequest request) {
        return ResponseEntity.ok(orderService.updateItemStatus(orderId, itemId, request.getStatus()));
    }

    @Operation(summary = "Закрыть заказ (оплата)",
            description = "Закрывает заказ: рассчитывает итог, создаёт транзакцию в бюджете, списывает ингредиенты со склада.")
    @ApiResponse(responseCode = "200", description = "Закрытый заказ с итогом")
    @ApiResponse(responseCode = "400", description = "Заказ уже закрыт или пуст")
    @PostMapping("/{orderId}/close")
    public ResponseEntity<OrderDto> closeOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.closeOrder(orderId));
    }

    @Operation(summary = "Отменить заказ", description = "Полностью отменяет/удаляет активный заказ для столика.")
    @ApiResponse(responseCode = "204", description = "Заказ отменён")
    @DeleteMapping("/table/{tableId}")
    public ResponseEntity<Void> clearOrder(@PathVariable UUID tableId) {
        orderService.clearOrder(tableId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Итого по столику", description = "Возвращает предварительную сумму активного заказа.")
    @ApiResponse(responseCode = "200", description = "Сумма заказа")
    @GetMapping("/table/{tableId}/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotal(@PathVariable UUID tableId) {
        return ResponseEntity.ok(Map.of("total", orderService.getTotal(tableId)));
    }
}

package com.vanilla.crm.dto.orders;

import com.vanilla.crm.dto.menu.DishDto;
import com.vanilla.crm.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemDto {
    private UUID id;
    private DishDto dish;
    private Integer quantity;
    private String status; // "new", "cooking", "ready", "served"

    private static final java.util.Map<OrderItem.ItemStatus, String> STATUS_REVERSE = java.util.Map.of(
            OrderItem.ItemStatus.NEW, "new",
            OrderItem.ItemStatus.COOKING, "cooking",
            OrderItem.ItemStatus.READY, "ready",
            OrderItem.ItemStatus.SERVED, "served",
            OrderItem.ItemStatus.CANCELLED, "cancelled"
    );

    private static final java.util.Map<String, OrderItem.ItemStatus> STATUS_MAP = java.util.Map.of(
            "new", OrderItem.ItemStatus.NEW,
            "cooking", OrderItem.ItemStatus.COOKING,
            "ready", OrderItem.ItemStatus.READY,
            "served", OrderItem.ItemStatus.SERVED,
            "cancelled", OrderItem.ItemStatus.CANCELLED
    );

    public static OrderItemDto fromEntity(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .dish(DishDto.fromEntity(item.getDish()))
                .quantity(item.getQuantity())
                .status(STATUS_REVERSE.getOrDefault(item.getStatus(), item.getStatus().name().toLowerCase()))
                .build();
    }

    public static OrderItem.ItemStatus toStatusEnum(String status) {
        if (status == null) return OrderItem.ItemStatus.NEW;
        return STATUS_MAP.getOrDefault(status, OrderItem.ItemStatus.NEW);
    }
}

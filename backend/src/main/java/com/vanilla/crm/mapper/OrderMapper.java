package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.orders.OrderDto;
import com.vanilla.crm.dto.orders.OrderItemDto;
import com.vanilla.crm.entity.Order;
import com.vanilla.crm.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final MenuMapper menuMapper;
    
    public OrderMapper(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    private static final Map<OrderItem.ItemStatus, String> STATUS_REVERSE = Map.of(
            OrderItem.ItemStatus.NEW, "new",
            OrderItem.ItemStatus.COOKING, "cooking",
            OrderItem.ItemStatus.READY, "ready",
            OrderItem.ItemStatus.SERVED, "served",
            OrderItem.ItemStatus.CANCELLED, "cancelled"
    );

    private static final Map<String, OrderItem.ItemStatus> STATUS_MAP = Map.of(
            "new", OrderItem.ItemStatus.NEW,
            "cooking", OrderItem.ItemStatus.COOKING,
            "ready", OrderItem.ItemStatus.READY,
            "served", OrderItem.ItemStatus.SERVED,
            "cancelled", OrderItem.ItemStatus.CANCELLED
    );

    public OrderItemDto toDto(OrderItem item) {
        if (item == null) return null;
        return OrderItemDto.builder()
                .id(item.getId())
                .dish(menuMapper.toDto(item.getDish()))
                .quantity(item.getQuantity())
                .status(STATUS_REVERSE.getOrDefault(item.getStatus(), item.getStatus().name().toLowerCase()))
                .build();
    }

    public OrderItem.ItemStatus toItemStatusEnum(String status) {
        if (status == null) return OrderItem.ItemStatus.NEW;
        return STATUS_MAP.getOrDefault(status, OrderItem.ItemStatus.NEW);
    }

    public OrderDto toDto(Order order) {
        if (order == null) return null;
        return OrderDto.builder()
                .id(order.getId())
                .tableId(order.getTable() != null ? order.getTable().getId() : null)
                .status(order.getStatus() != null ? order.getStatus().name().toLowerCase() : null)
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .closedAt(order.getClosedAt() != null ? order.getClosedAt().toString() : null)
                .total(order.getTotal())
                .items(order.getItems() != null
                        ? order.getItems().stream().map(this::toDto).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}

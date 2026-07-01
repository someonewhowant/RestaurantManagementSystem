package com.vanilla.crm.orders.dto;

import com.vanilla.crm.orders.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderDto {
    private UUID id;
    private UUID tableId;
    private String status;
    private String createdAt;
    private String closedAt;
    private BigDecimal total;
    private List<OrderItemDto> items;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .tableId(order.getTable().getId())
                .status(order.getStatus().name().toLowerCase())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .closedAt(order.getClosedAt() != null ? order.getClosedAt().toString() : null)
                .total(order.getTotal())
                .items(order.getItems() != null
                        ? order.getItems().stream().map(OrderItemDto::fromEntity).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}

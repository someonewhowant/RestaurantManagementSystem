package com.vanilla.crm.dto.orders;

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

}

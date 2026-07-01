package com.vanilla.crm.orders.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddItemRequest {
    private UUID dishId;
    private Integer quantity;
}

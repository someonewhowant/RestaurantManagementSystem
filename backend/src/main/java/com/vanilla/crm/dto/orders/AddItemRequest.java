package com.vanilla.crm.dto.orders;

import lombok.Data;

import java.util.UUID;

@Data
public class AddItemRequest {
    private UUID dishId;
    private Integer quantity;
}

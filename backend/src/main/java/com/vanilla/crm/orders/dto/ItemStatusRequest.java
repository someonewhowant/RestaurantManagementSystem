package com.vanilla.crm.orders.dto;

import lombok.Data;

@Data
public class ItemStatusRequest {
    private String status; // "new", "cooking", "ready", "served"
}

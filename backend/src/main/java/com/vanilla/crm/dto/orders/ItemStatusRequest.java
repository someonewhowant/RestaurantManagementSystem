package com.vanilla.crm.dto.orders;

import lombok.Data;

@Data
public class ItemStatusRequest {
    private String status; // "new", "cooking", "ready", "served"
}

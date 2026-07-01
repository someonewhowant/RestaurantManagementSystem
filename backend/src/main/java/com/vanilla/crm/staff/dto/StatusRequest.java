package com.vanilla.crm.staff.dto;

import lombok.Data;

@Data
public class StatusRequest {
    private String status; // "Активен", "В отпуске", "Уволен"
}

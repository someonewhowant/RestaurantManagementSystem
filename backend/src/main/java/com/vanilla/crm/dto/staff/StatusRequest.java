package com.vanilla.crm.dto.staff;

import lombok.Data;

@Data
public class StatusRequest {
    private String status; // "Активен", "В отпуске", "Уволен"
}

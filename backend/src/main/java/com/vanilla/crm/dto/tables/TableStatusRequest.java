package com.vanilla.crm.dto.tables;

import lombok.Data;

import java.util.UUID;

@Data
public class TableStatusRequest {
    private String status;    // "Свободен", "Занят", "Ожидает блюда", "Оплата"
    private UUID waiterId;
}

package com.vanilla.crm.tables.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class WaiterAssignRequest {
    private UUID waiterId;
}

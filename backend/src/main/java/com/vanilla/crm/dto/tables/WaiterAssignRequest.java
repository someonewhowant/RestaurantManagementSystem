package com.vanilla.crm.dto.tables;

import lombok.Data;

import java.util.UUID;

@Data
public class WaiterAssignRequest {
    private UUID waiterId;
}

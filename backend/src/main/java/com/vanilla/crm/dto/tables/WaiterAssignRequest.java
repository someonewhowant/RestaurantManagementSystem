package com.vanilla.crm.dto.tables;

import lombok.Data;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

@Data
public class WaiterAssignRequest {
    @NotNull(message = "ID официанта обязателен")
    private UUID waiterId;
}

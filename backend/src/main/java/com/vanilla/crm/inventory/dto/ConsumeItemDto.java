package com.vanilla.crm.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeItemDto {
    private UUID ingredientId;
    private Double amount;
}

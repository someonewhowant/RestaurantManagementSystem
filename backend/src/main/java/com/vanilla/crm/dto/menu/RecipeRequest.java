package com.vanilla.crm.dto.menu;

import lombok.Data;

import java.util.UUID;

@Data
public class RecipeRequest {
    private UUID ingredientId;
    private Double amount;
}

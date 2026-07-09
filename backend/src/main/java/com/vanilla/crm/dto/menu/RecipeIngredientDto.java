package com.vanilla.crm.dto.menu;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RecipeIngredientDto {
    private UUID id;
    private UUID ingredientId;
    private Double amount;

}

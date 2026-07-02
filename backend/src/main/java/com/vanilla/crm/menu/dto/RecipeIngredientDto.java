package com.vanilla.crm.menu.dto;

import com.vanilla.crm.menu.entity.RecipeIngredient;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RecipeIngredientDto {
    private UUID id;
    private UUID ingredientId;
    private Double amount;

    public static RecipeIngredientDto fromEntity(RecipeIngredient recipeIngredient) {
        return RecipeIngredientDto.builder()
                .id(recipeIngredient.getId())
                .ingredientId(recipeIngredient.getInventoryItem().getId())
                .amount(recipeIngredient.getAmount())
                .build();
    }
}

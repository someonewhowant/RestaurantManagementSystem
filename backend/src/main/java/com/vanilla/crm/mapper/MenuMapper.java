package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.menu.DishDto;
import com.vanilla.crm.dto.menu.RecipeIngredientDto;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.RecipeIngredient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MenuMapper {

    public RecipeIngredientDto toDto(RecipeIngredient recipeIngredient) {
        if (recipeIngredient == null) return null;
        return RecipeIngredientDto.builder()
                .id(recipeIngredient.getId())
                .ingredientId(recipeIngredient.getInventoryItem() != null ? recipeIngredient.getInventoryItem().getId() : null)
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public DishDto toDto(Dish dish) {
        if (dish == null) return null;
        return DishDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .category(dish.getCategory())
                .price(dish.getPrice())
                .status(dish.getStatus() != null ? dish.getStatus().name().toLowerCase() : "available")
                .weight(dish.getWeight())
                .imageIcon(dish.getImageIcon())
                .instructions(dish.getInstructions())
                .allergens(dish.getAllergens() != null ? new ArrayList<>(dish.getAllergens()) : null)
                .macros(dish.getMacros())
                .recipe(dish.getRecipe() != null ? 
                    dish.getRecipe().stream().map(this::toDto).toList() : null)
                .build();
    }
}

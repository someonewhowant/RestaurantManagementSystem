package com.vanilla.crm.dto.menu;

import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.Macros;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Data
@Builder
public class DishDto {
    private UUID id;
    
    @NotBlank(message = "Название блюда не может быть пустым")
    private String name;
    
    @NotBlank(message = "Категория обязательна")
    private String category;
    
    @NotNull(message = "Цена обязательна")
    @PositiveOrZero(message = "Цена не может быть отрицательной")
    private BigDecimal price;
    
    private String status;
    private String weight;
    private String imageIcon;
    private String instructions;
    private List<String> allergens;
    private Macros macros;
    private List<RecipeIngredientDto> recipe;

    public static DishDto fromEntity(Dish dish) {
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
                    dish.getRecipe().stream().map(RecipeIngredientDto::fromEntity).toList() : null)
                .build();
    }
}

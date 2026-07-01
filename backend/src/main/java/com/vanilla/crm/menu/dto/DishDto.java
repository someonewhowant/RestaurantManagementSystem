package com.vanilla.crm.menu.dto;

import com.vanilla.crm.menu.entity.Dish;
import com.vanilla.crm.menu.entity.Macros;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DishDto {
    private UUID id;
    private String name;
    private String category;
    private BigDecimal price;
    private String weight;
    private String imageIcon;
    private String instructions;
    private List<String> allergens;
    private Macros macros;

    public static DishDto fromEntity(Dish dish) {
        return DishDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .category(dish.getCategory())
                .price(dish.getPrice())
                .weight(dish.getWeight())
                .imageIcon(dish.getImageIcon())
                .instructions(dish.getInstructions())
                .allergens(dish.getAllergens())
                .macros(dish.getMacros())
                .build();
    }
}

package com.vanilla.crm.service;

import com.vanilla.crm.dto.menu.DishDto;
import com.vanilla.crm.dto.menu.RecipeRequest;

import java.util.List;
import java.util.UUID;

public interface MenuService {
    List<DishDto> getAllDishes();
    List<DishDto> getDishesByCategory(String category);
    List<DishDto> getMenu(String category);
    DishDto createDish(DishDto dto);
    DishDto updateDish(UUID id, DishDto dto);
    void deleteDish(UUID id);
    DishDto setRecipe(UUID dishId, List<RecipeRequest> recipeRequests);
}

package com.vanilla.crm.service;

import com.vanilla.crm.dto.menu.DishDto;
import com.vanilla.crm.dto.menu.RecipeRequest;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.RecipeIngredient;
import com.vanilla.crm.repository.InventoryRepository;
import com.vanilla.crm.entity.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface MenuService {
    List<DishDto> getAllDishes();
    List<DishDto> getDishesByCategory(String category);
    DishDto createDish(DishDto dto);
    DishDto updateDish(UUID id, DishDto dto);
    void deleteDish(UUID id);
    DishDto setRecipe(UUID dishId, List<RecipeRequest> recipeRequests);
}

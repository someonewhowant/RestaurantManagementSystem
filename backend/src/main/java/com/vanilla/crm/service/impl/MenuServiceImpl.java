package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;

import com.vanilla.crm.repository.MenuRepository;

import com.vanilla.crm.dto.menu.DishDto;
import com.vanilla.crm.dto.menu.RecipeRequest;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.RecipeIngredient;
import com.vanilla.crm.repository.InventoryRepository;
import com.vanilla.crm.entity.InventoryItem;
import com.vanilla.crm.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vanilla.crm.service.MenuService;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final InventoryRepository inventoryRepository;
    private final MenuMapper menuMapper;

    @Transactional(readOnly = true)
    @Override
    public List<DishDto> getAllDishes() {
        return menuRepository.findAll().stream()
                .map(menuMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DishDto> getDishesByCategory(String category) {
        if ("Популярное".equals(category)) {
            // Frontend specific logic: take first 4 as popular for now
            return menuRepository.findAll().stream()
                    .limit(4)
                    .map(menuMapper::toDto)
                    .collect(Collectors.toList());
        }
        return menuRepository.findByCategory(category).stream()
                .map(menuMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DishDto> getMenu(String category) {
        if (category != null && !category.isEmpty()) {
            return getDishesByCategory(category);
        }
        return getAllDishes();
    }

    @Transactional
    @Override
    public DishDto createDish(DishDto dto) {
        log.info("Creating dish: {}", dto.getName());
        Dish dish = Dish.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .weight(dto.getWeight())
                .imageIcon(dto.getImageIcon())
                .instructions(dto.getInstructions())
                .allergens(dto.getAllergens() != null ? new HashSet<>(dto.getAllergens()) : null)
                .macros(dto.getMacros())
                .build();
        return menuMapper.toDto(menuRepository.save(dish));
    }

    @Transactional
    @Override
    public DishDto updateDish(UUID id, DishDto dto) {
        log.info("Updating dish {}", id);
        Dish dish = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));

        if (dto.getName() != null) dish.setName(dto.getName());
        if (dto.getCategory() != null) dish.setCategory(dto.getCategory());
        if (dto.getPrice() != null) dish.setPrice(dto.getPrice());
        if (dto.getWeight() != null) dish.setWeight(dto.getWeight());
        if (dto.getImageIcon() != null) dish.setImageIcon(dto.getImageIcon());
        if (dto.getInstructions() != null) dish.setInstructions(dto.getInstructions());
        if (dto.getAllergens() != null) dish.setAllergens(new HashSet<>(dto.getAllergens()));
        if (dto.getMacros() != null) dish.setMacros(dto.getMacros());
        
        if (dto.getRecipe() != null) {
            dish.getRecipe().clear();
            for (var recDto : dto.getRecipe()) {
                InventoryItem item = inventoryRepository.findById(recDto.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
                dish.getRecipe().add(RecipeIngredient.builder()
                        .dish(dish)
                        .inventoryItem(item)
                        .amount(recDto.getAmount())
                        .build());
            }
        }

        return menuMapper.toDto(menuRepository.save(dish));
    }

    @Transactional
    @Override
    public void deleteDish(UUID id) {
        log.info("Deleting dish {}", id);
        menuRepository.deleteById(id);
    }

    @Transactional
    @Override
    public DishDto setRecipe(UUID dishId, List<RecipeRequest> recipeRequests) {
        log.info("Setting recipe for dish {}", dishId);
        Dish dish = menuRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));

        dish.getRecipe().clear();

        for (RecipeRequest req : recipeRequests) {
            InventoryItem item = inventoryRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
            
            RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                    .dish(dish)
                    .inventoryItem(item)
                    .amount(req.getAmount())
                    .build();
            dish.getRecipe().add(recipeIngredient);
        }

        return menuMapper.toDto(menuRepository.save(dish));
    }
}

package com.vanilla.crm.menu;

import com.vanilla.crm.menu.dto.DishDto;
import com.vanilla.crm.menu.dto.RecipeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "Меню", description = "Управление блюдами и рецептами ресторана")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "Получить меню", description = "Возвращает список всех блюд. Можно фильтровать по категории.")
    @ApiResponse(responseCode = "200", description = "Список блюд")
    @GetMapping
    public ResponseEntity<List<DishDto>> getMenu(
            @Parameter(description = "Фильтр по категории (Горячее, Салаты, Напитки и т.д.)")
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(menuService.getDishesByCategory(category));
        }
        return ResponseEntity.ok(menuService.getAllDishes());
    }

    @Operation(summary = "Добавить блюдо", description = "Создаёт новое блюдо в меню.")
    @ApiResponse(responseCode = "200", description = "Созданное блюдо")
    @PostMapping
    public ResponseEntity<DishDto> createDish(@RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.createDish(dto));
    }

    @Operation(summary = "Обновить блюдо", description = "Полное обновление данных блюда по ID.")
    @ApiResponse(responseCode = "200", description = "Обновлённое блюдо")
    @ApiResponse(responseCode = "404", description = "Блюдо не найдено")
    @PutMapping("/{id}")
    public ResponseEntity<DishDto> updateDish(@PathVariable UUID id, @RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.updateDish(id, dto));
    }

    @Operation(summary = "Удалить блюдо", description = "Удаляет блюдо из меню по ID.")
    @ApiResponse(responseCode = "204", description = "Блюдо удалено")
    @ApiResponse(responseCode = "404", description = "Блюдо не найдено")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable UUID id) {
        menuService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Установить рецепт блюда", description = "Задаёт список ингредиентов и их количество для приготовления одной порции блюда.")
    @ApiResponse(responseCode = "200", description = "Блюдо с обновлённым рецептом")
    @ApiResponse(responseCode = "404", description = "Блюдо или ингредиент не найдены")
    @PatchMapping("/{id}/recipe")
    public ResponseEntity<DishDto> setRecipe(@PathVariable UUID id, @RequestBody List<RecipeRequest> recipeRequests) {
        return ResponseEntity.ok(menuService.setRecipe(id, recipeRequests));
    }
}

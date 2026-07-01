package com.vanilla.crm.menu;

import com.vanilla.crm.menu.dto.DishDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<DishDto>> getMenu(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(menuService.getDishesByCategory(category));
        }
        return ResponseEntity.ok(menuService.getAllDishes());
    }

    @PostMapping
    public ResponseEntity<DishDto> createDish(@RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.createDish(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishDto> updateDish(@PathVariable UUID id, @RequestBody DishDto dto) {
        return ResponseEntity.ok(menuService.updateDish(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable UUID id) {
        menuService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }
}

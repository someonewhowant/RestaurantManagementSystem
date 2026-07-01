package com.vanilla.crm.menu;

import com.vanilla.crm.menu.dto.DishDto;
import com.vanilla.crm.menu.entity.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<DishDto> getAllDishes() {
        return menuRepository.findAll().stream()
                .map(DishDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DishDto> getDishesByCategory(String category) {
        if ("Популярное".equals(category)) {
            // Frontend specific logic: take first 4 as popular for now
            return menuRepository.findAll().stream()
                    .limit(4)
                    .map(DishDto::fromEntity)
                    .collect(Collectors.toList());
        }
        return menuRepository.findByCategory(category).stream()
                .map(DishDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DishDto createDish(DishDto dto) {
        Dish dish = Dish.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .weight(dto.getWeight())
                .imageIcon(dto.getImageIcon())
                .instructions(dto.getInstructions())
                .allergens(dto.getAllergens())
                .macros(dto.getMacros())
                .build();
        return DishDto.fromEntity(menuRepository.save(dish));
    }

    @Transactional
    public DishDto updateDish(UUID id, DishDto dto) {
        Dish dish = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        if (dto.getName() != null) dish.setName(dto.getName());
        if (dto.getCategory() != null) dish.setCategory(dto.getCategory());
        if (dto.getPrice() != null) dish.setPrice(dto.getPrice());
        if (dto.getWeight() != null) dish.setWeight(dto.getWeight());
        if (dto.getImageIcon() != null) dish.setImageIcon(dto.getImageIcon());
        if (dto.getInstructions() != null) dish.setInstructions(dto.getInstructions());
        if (dto.getAllergens() != null) dish.setAllergens(dto.getAllergens());
        if (dto.getMacros() != null) dish.setMacros(dto.getMacros());

        return DishDto.fromEntity(menuRepository.save(dish));
    }

    @Transactional
    public void deleteDish(UUID id) {
        menuRepository.deleteById(id);
    }
}

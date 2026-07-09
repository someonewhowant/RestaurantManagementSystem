package com.vanilla.crm.config.seed;

import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.InventoryItem;
import com.vanilla.crm.entity.Macros;
import com.vanilla.crm.entity.RecipeIngredient;
import com.vanilla.crm.repository.InventoryRepository;
import com.vanilla.crm.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class MenuDataSeeder implements DataSeeder {

    private final MenuRepository menuRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void seed() {
        if (menuRepository.count() > 0) {
            return;
        }

        log.info("Database is empty. Seeding menu data...");

        Dish dish1 = Dish.builder()
                .name("Стейк Рибай")
                .category("Горячее")
                .price(new BigDecimal("2500"))
                .status(Dish.DishStatus.AVAILABLE)
                .weight("300г")
                .imageIcon("🥩")
                .instructions("Прожарка medium rare, подавать с чесночным маслом")
                .allergens(new HashSet<>(List.of("Мясо")))
                .macros(Macros.builder().calories(650.0).protein(45.0).carbs(0.0).fats(50.0).build())
                .build();

        Dish dish2 = Dish.builder()
                .name("Салат Цезарь")
                .category("Салаты")
                .price(new BigDecimal("450"))
                .status(Dish.DishStatus.AVAILABLE)
                .weight("250г")
                .imageIcon("🥗")
                .instructions("Соус отдельно")
                .allergens(new HashSet<>(List.of("Яйцо", "Глютен")))
                .macros(Macros.builder().calories(320.0).protein(12.0).carbs(15.0).fats(25.0).build())
                .build();

        Dish dish3 = Dish.builder()
                .name("Картофель Фри")
                .category("Закуски")
                .price(new BigDecimal("200"))
                .status(Dish.DishStatus.AVAILABLE)
                .weight("150г")
                .imageIcon("🍟")
                .instructions("Жарить во фритюре до золотистой корочки")
                .allergens(new HashSet<>())
                .macros(Macros.builder().calories(290.0).protein(3.0).carbs(35.0).fats(15.0).build())
                .build();

        Dish dish4 = Dish.builder()
                .name("Эспрессо")
                .category("Напитки")
                .price(new BigDecimal("150"))
                .status(Dish.DishStatus.AVAILABLE)
                .weight("30мл")
                .imageIcon("☕")
                .instructions("Классический эспрессо")
                .allergens(new HashSet<>())
                .macros(Macros.builder().calories(5.0).protein(0.0).carbs(0.0).fats(0.0).build())
                .build();

        Dish dish5 = Dish.builder()
                .name("Тирамису")
                .category("Десерты")
                .price(new BigDecimal("350"))
                .status(Dish.DishStatus.AVAILABLE)
                .weight("150г")
                .imageIcon("🍰")
                .instructions("Подавать охлажденным")
                .allergens(new HashSet<>(List.of("Лактоза", "Глютен", "Яйцо")))
                .macros(Macros.builder().calories(280.0).protein(5.0).carbs(30.0).fats(12.0).build())
                .build();

        List<Dish> dishes = menuRepository.saveAll(List.of(dish1, dish2, dish3, dish4, dish5));

        List<InventoryItem> items = inventoryRepository.findAll();
        if (!items.isEmpty()) {
            InventoryItem meat = items.stream().filter(i -> i.getName().equals("Говядина")).findFirst().orElse(items.get(0));
            InventoryItem tomatoes = items.stream().filter(i -> i.getName().equals("Помидоры")).findFirst().orElse(items.get(0));
            InventoryItem potatoes = items.stream().filter(i -> i.getName().equals("Картофель")).findFirst().orElse(items.get(0));
            InventoryItem coffee = items.stream().filter(i -> i.getName().equals("Кофе в зернах")).findFirst().orElse(items.get(0));
            InventoryItem cream = items.stream().filter(i -> i.getName().equals("Сливки 33%")).findFirst().orElse(items.get(0));
            InventoryItem oil = items.stream().filter(i -> i.getName().equals("Оливковое масло")).findFirst().orElse(items.get(0));
            InventoryItem salt = items.stream().filter(i -> i.getName().equals("Соль")).findFirst().orElse(items.get(0));

            Dish savedDish1 = dishes.get(0);
            savedDish1.getRecipe().add(RecipeIngredient.builder().dish(savedDish1).inventoryItem(meat).amount(0.35).build());
            savedDish1.getRecipe().add(RecipeIngredient.builder().dish(savedDish1).inventoryItem(oil).amount(0.02).build());
            savedDish1.getRecipe().add(RecipeIngredient.builder().dish(savedDish1).inventoryItem(salt).amount(0.01).build());
            
            Dish savedDish2 = dishes.get(1);
            savedDish2.getRecipe().add(RecipeIngredient.builder().dish(savedDish2).inventoryItem(tomatoes).amount(0.05).build());
            savedDish2.getRecipe().add(RecipeIngredient.builder().dish(savedDish2).inventoryItem(oil).amount(0.02).build());

            Dish savedDish3 = dishes.get(2);
            savedDish3.getRecipe().add(RecipeIngredient.builder().dish(savedDish3).inventoryItem(potatoes).amount(0.2).build());
            savedDish3.getRecipe().add(RecipeIngredient.builder().dish(savedDish3).inventoryItem(salt).amount(0.01).build());

            Dish savedDish4 = dishes.get(3);
            savedDish4.getRecipe().add(RecipeIngredient.builder().dish(savedDish4).inventoryItem(coffee).amount(0.015).build());

            Dish savedDish5 = dishes.get(4);
            savedDish5.getRecipe().add(RecipeIngredient.builder().dish(savedDish5).inventoryItem(cream).amount(0.05).build());

            menuRepository.saveAll(dishes);
        }

        log.info("Seeded {} dishes with recipes.", dishes.size());
    }
}

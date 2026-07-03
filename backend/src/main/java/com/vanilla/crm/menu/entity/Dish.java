package com.vanilla.crm.menu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // e.g. "Популярное", "Горячее", "Закуски", "Напитки", "Десерты"

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DishStatus status = DishStatus.AVAILABLE;

    private String weight;

    private String imageIcon;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_allergens", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "allergen")
    private java.util.Set<String> allergens;

    @Embedded
    private Macros macros;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private java.util.Set<RecipeIngredient> recipe = new java.util.HashSet<>();

    public enum DishStatus {
        AVAILABLE,
        STOP_LIST
    }
}

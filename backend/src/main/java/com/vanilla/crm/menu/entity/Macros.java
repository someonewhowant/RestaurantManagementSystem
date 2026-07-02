package com.vanilla.crm.menu.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Macros {
    private Double protein;
    private Double fats;
    private Double carbs;
    private Double calories;
}

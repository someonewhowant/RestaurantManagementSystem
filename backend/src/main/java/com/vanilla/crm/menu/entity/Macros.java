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
    private Integer proteins;
    private Integer fats;
    private Integer carbs;
    private Integer calories;
}

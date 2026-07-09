package com.vanilla.crm.dto.orders;

import com.vanilla.crm.dto.menu.DishDto;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.util.Map;

@Data
@Builder
public class OrderItemDto {
    private UUID id;
    private DishDto dish;
    private Integer quantity;
    private String status; // "new", "cooking", "ready", "served"

}

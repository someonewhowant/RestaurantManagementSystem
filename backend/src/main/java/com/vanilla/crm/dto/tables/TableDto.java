package com.vanilla.crm.dto.tables;

import com.vanilla.crm.entity.RestaurantTable;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
public class TableDto {
    private UUID id;
    
    @NotNull(message = "Номер столика обязателен")
    @Positive(message = "Номер столика должен быть положительным")
    private Integer number;
    
    @NotNull(message = "Вместимость обязательна")
    @Positive(message = "Вместимость должна быть положительной")
    private Integer capacity;
    
    @NotBlank(message = "Статус обязателен")
    private String status;   // "Свободен", "Занят", "Ожидает блюда", "Оплата"
    
    private UUID waiterId;
    private String statusUpdatedAt;

    private static final Map<String, RestaurantTable.TableStatus> STATUS_MAP = Map.of(
            "Свободен", RestaurantTable.TableStatus.FREE,
            "Занят", RestaurantTable.TableStatus.OCCUPIED,
            "Ожидает блюда", RestaurantTable.TableStatus.AWAITING_FOOD,
            "Оплата", RestaurantTable.TableStatus.PAYMENT
    );

    private static final Map<RestaurantTable.TableStatus, String> STATUS_REVERSE = Map.of(
            RestaurantTable.TableStatus.FREE, "Свободен",
            RestaurantTable.TableStatus.OCCUPIED, "Занят",
            RestaurantTable.TableStatus.AWAITING_FOOD, "Ожидает блюда",
            RestaurantTable.TableStatus.PAYMENT, "Оплата"
    );

    public static TableDto fromEntity(RestaurantTable table) {
        return TableDto.builder()
                .id(table.getId())
                .number(table.getNumber())
                .capacity(table.getCapacity())
                .status(STATUS_REVERSE.getOrDefault(table.getStatus(), table.getStatus().name()))
                .waiterId(table.getWaiterId())
                .statusUpdatedAt(table.getStatusUpdatedAt() != null ? table.getStatusUpdatedAt().toString() : null)
                .build();
    }

    public static RestaurantTable.TableStatus toStatusEnum(String status) {
        if (status == null) return RestaurantTable.TableStatus.FREE;
        return STATUS_MAP.getOrDefault(status, RestaurantTable.TableStatus.FREE);
    }
}

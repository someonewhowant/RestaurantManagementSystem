package com.vanilla.crm.tables.dto;

import com.vanilla.crm.tables.entity.RestaurantTable;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TableDto {
    private UUID id;
    private Integer number;
    private Integer capacity;
    private String status;   // "Свободен", "Занят", "Ожидает блюда", "Оплата"
    private UUID waiterId;
    private String statusUpdatedAt;

    private static final java.util.Map<String, RestaurantTable.TableStatus> STATUS_MAP = java.util.Map.of(
            "Свободен", RestaurantTable.TableStatus.FREE,
            "Занят", RestaurantTable.TableStatus.OCCUPIED,
            "Ожидает блюда", RestaurantTable.TableStatus.AWAITING_FOOD,
            "Оплата", RestaurantTable.TableStatus.PAYMENT
    );

    private static final java.util.Map<RestaurantTable.TableStatus, String> STATUS_REVERSE = java.util.Map.of(
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

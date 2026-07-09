package com.vanilla.crm.mapper;

import com.vanilla.crm.dto.tables.TableDto;
import com.vanilla.crm.entity.RestaurantTable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TableMapper {

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

    public TableDto toDto(RestaurantTable table) {
        if (table == null) return null;
        return TableDto.builder()
                .id(table.getId())
                .number(table.getNumber())
                .capacity(table.getCapacity())
                .status(STATUS_REVERSE.getOrDefault(table.getStatus(), table.getStatus().name()))
                .waiterId(table.getWaiterId())
                .statusUpdatedAt(table.getStatusUpdatedAt() != null ? table.getStatusUpdatedAt().toString() : null)
                .build();
    }

    public RestaurantTable.TableStatus toStatusEnum(String status) {
        if (status == null) return RestaurantTable.TableStatus.FREE;
        return STATUS_MAP.getOrDefault(status, RestaurantTable.TableStatus.FREE);
    }
}

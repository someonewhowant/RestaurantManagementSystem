package com.vanilla.crm.service;

import com.vanilla.crm.service.BudgetService;
import com.vanilla.crm.dto.budget.TransactionDto;
import com.vanilla.crm.service.InventoryService;
import com.vanilla.crm.repository.MenuRepository;
import com.vanilla.crm.entity.Dish;
import com.vanilla.crm.entity.RecipeIngredient;
import com.vanilla.crm.dto.orders.OrderDto;
import com.vanilla.crm.dto.orders.OrderItemDto;
import com.vanilla.crm.entity.Order;
import com.vanilla.crm.entity.OrderItem;
import com.vanilla.crm.repository.StaffRepository;
import com.vanilla.crm.entity.Employee;
import com.vanilla.crm.repository.TableRepository;
import com.vanilla.crm.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import com.vanilla.crm.dto.inventory.ConsumeItemDto;

public interface OrderService {
    OrderDto getOrderForTable(UUID tableId);
    List<OrderDto> getKitchenOrders();
    OrderDto createOrder(UUID tableId);
    OrderDto addItem(UUID tableId, UUID dishId, int quantity);
    OrderDto removeItem(UUID orderId, UUID itemId);
    OrderDto sendToKitchen(UUID orderId);
    OrderDto updateItemStatus(UUID orderId, UUID itemId, String newStatus);
    OrderDto closeOrder(UUID orderId);
    void clearOrder(UUID tableId);
    BigDecimal getTotal(UUID tableId);
}

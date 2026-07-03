package com.vanilla.crm.orders;

import com.vanilla.crm.budget.BudgetService;
import com.vanilla.crm.budget.dto.TransactionDto;
import com.vanilla.crm.inventory.InventoryService;
import com.vanilla.crm.menu.MenuRepository;
import com.vanilla.crm.menu.entity.Dish;
import com.vanilla.crm.menu.entity.RecipeIngredient;
import com.vanilla.crm.orders.dto.OrderDto;
import com.vanilla.crm.orders.dto.OrderItemDto;
import com.vanilla.crm.orders.entity.Order;
import com.vanilla.crm.orders.entity.OrderItem;
import com.vanilla.crm.staff.StaffRepository;
import com.vanilla.crm.staff.entity.Employee;
import com.vanilla.crm.tables.TableRepository;
import com.vanilla.crm.tables.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final TableRepository tableRepository;
    private final BudgetService budgetService;
    private final InventoryService inventoryService;
    private final StaffRepository staffRepository;

    /**
     * Get the active order for a given table, or return an empty order DTO.
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderForTable(UUID tableId) {
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(OrderDto::fromEntity)
                .orElse(OrderDto.builder()
                        .tableId(tableId)
                        .status("active")
                        .items(List.of())
                        .build());
    }

    /**
     * Get all orders with items that are currently being cooked (for kitchen display).
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getKitchenOrders() {
        // Find all active orders that have at least one item in COOKING status
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.ACTIVE)
                .filter(o -> o.getItems().stream().anyMatch(
                        i -> i.getStatus() == OrderItem.ItemStatus.COOKING || i.getStatus() == OrderItem.ItemStatus.NEW))
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new active order for a table (or return the existing one).
     */
    @Transactional
    public OrderDto createOrder(UUID tableId) {
        // Check if there's already an active order for this table
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(OrderDto::fromEntity)
                .orElseGet(() -> {
                    RestaurantTable table = tableRepository.findById(tableId)
                            .orElseThrow(() -> new RuntimeException("Table not found"));

                    Order order = Order.builder()
                            .table(table)
                            .status(Order.OrderStatus.ACTIVE)
                            .build();

                    return OrderDto.fromEntity(orderRepository.save(order));
                });
    }

    /**
     * Add a dish to the active order for a table.
     */
    @Transactional
    public OrderDto addItem(UUID tableId, UUID dishId, int quantity) {
        // Find or create active order
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        Order order = orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .orElseGet(() -> orderRepository.save(Order.builder().table(table).build()));

        Dish dish = menuRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        // Check if there's already a NEW item for this dish — increment quantity
        OrderItem existingItem = order.getItems().stream()
                .filter(i -> i.getDish().getId().equals(dishId) && i.getStatus() == OrderItem.ItemStatus.NEW)
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            orderItemRepository.save(existingItem);
        } else {
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(quantity)
                    .status(OrderItem.ItemStatus.NEW)
                    .build();
            order.getItems().add(newItem);
            orderRepository.save(order);
        }

        return OrderDto.fromEntity(orderRepository.findById(order.getId()).orElseThrow());
    }

    /**
     * Remove an item from the order (only if status is NEW).
     */
    @Transactional
    public OrderDto removeItem(UUID orderId, UUID itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getStatus() == OrderItem.ItemStatus.NEW) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                orderItemRepository.save(item);
            } else {
                order.getItems().remove(item);
                orderRepository.save(order);
            }
        }

        return OrderDto.fromEntity(orderRepository.findById(orderId).orElseThrow());
    }

    /**
     * Send all NEW items to the kitchen (change status to COOKING).
     */
    @Transactional
    public OrderDto sendToKitchen(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.getItems().forEach(item -> {
            if (item.getStatus() == OrderItem.ItemStatus.NEW) {
                item.setStatus(OrderItem.ItemStatus.COOKING);
            }
        });

        return OrderDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Update status of a single order item.
     */
    @Transactional
    public OrderDto updateItemStatus(UUID orderId, UUID itemId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setStatus(OrderItemDto.toStatusEnum(newStatus)));

        return OrderDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Close an order:
     * 1. Calculate total
     * 2. Create an INCOME transaction in the budget
     * 3. Mark order as CLOSED
     */
    @Transactional
    public OrderDto closeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Calculate total
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(total);
        order.setStatus(Order.OrderStatus.CLOSED);
        order.setClosedAt(Instant.now());

        // Create income transaction in budget
        String finalDescription = "Заказ стол №" + order.getTable().getNumber();
        if (order.getTable().getWaiterId() != null) {
            Employee waiter = staffRepository.findById(order.getTable().getWaiterId()).orElse(null);
            if (waiter != null) {
                finalDescription = finalDescription + " | Официант: " + waiter.getName();
            }
        }

        TransactionDto txDto = TransactionDto.builder()
                .amount(total)
                .type("Доход")
                .category("Оплата заказа")
                .description(finalDescription)
                .orderId(order.getId())
                .build();
        budgetService.createTransaction(txDto);

        // Deduct inventory!
        order.getItems().forEach(orderItem -> {
            Dish dish = orderItem.getDish();
            if (dish.getRecipe() != null) {
                dish.getRecipe().forEach(recipeIngredient -> {
                    double totalAmountToConsume = recipeIngredient.getAmount() * orderItem.getQuantity();
                    inventoryService.consume(recipeIngredient.getInventoryItem().getId(), totalAmountToConsume);
                });
            }
        });

        // Free the table
        RestaurantTable table = order.getTable();
        table.setStatus(RestaurantTable.TableStatus.FREE);
        table.setWaiterId(null);
        table.setStatusUpdatedAt(Instant.now());
        tableRepository.save(table);

        log.info("Order {} closed. Total: {} ₽", orderId, total);

        return OrderDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Clear/cancel an active order for a table.
     */
    @Transactional
    public void clearOrder(UUID tableId) {
        orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .ifPresent(order -> {
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    order.setClosedAt(Instant.now());
                    orderRepository.save(order);
                });
    }

    /**
     * Calculate total for an active order (without closing).
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotal(UUID tableId) {
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(order -> order.getItems().stream()
                        .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }
}

package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;

import com.vanilla.crm.repository.OrderItemRepository;

import com.vanilla.crm.repository.OrderRepository;

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
import com.vanilla.crm.mapper.OrderMapper;
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
import com.vanilla.crm.service.OrderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final TableRepository tableRepository;
    private final BudgetService budgetService;
    private final InventoryService inventoryService;
    private final StaffRepository staffRepository;
    private final OrderMapper orderMapper;

    /**
     * Get the active order for a given table, or return an empty order DTO.
     */
    @Transactional(readOnly = true)
    @Override
    public OrderDto getOrderForTable(UUID tableId) {
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(orderMapper::toDto)
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
    @Override
    public List<OrderDto> getKitchenOrders() {
        // Find all active orders that have at least one item in COOKING status
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.ACTIVE)
                .filter(o -> o.getItems().stream().anyMatch(
                        i -> i.getStatus() == OrderItem.ItemStatus.COOKING || i.getStatus() == OrderItem.ItemStatus.NEW))
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new active order for a table (or return the existing one).
     */
    @Transactional
    @Override
    public OrderDto createOrder(UUID tableId) {
        // Check if there's already an active order for this table
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(orderMapper::toDto)
                .orElseGet(() -> {
                    RestaurantTable table = tableRepository.findById(tableId)
                            .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

                    Order order = Order.builder()
                            .table(table)
                            .status(Order.OrderStatus.ACTIVE)
                            .build();

                    return orderMapper.toDto(orderRepository.save(order));
                });
    }

    /**
     * Add a dish to the active order for a table.
     */
    @Transactional
    @Override
    public OrderDto addItem(UUID tableId, UUID dishId, int quantity) {
        log.info("Adding {} of dish {} to table {}", quantity, dishId, tableId);
        // Find or create active order
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        Order order = orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .orElseGet(() -> orderRepository.save(Order.builder().table(table).build()));

        Dish dish = menuRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));

        // Create individual items for each portion to allow independent status management
        for (int i = 0; i < quantity; i++) {
            OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(1)
                    .status(OrderItem.ItemStatus.NEW)
                    .build();
            order.getItems().add(newItem);
        }
        orderRepository.save(order);

        return orderMapper.toDto(orderRepository.findById(order.getId()).orElseThrow());
    }

    /**
     * Remove an item from the order (only if status is NEW).
     */
    @Transactional
    @Override
    public OrderDto removeItem(UUID orderId, UUID itemId) {
        log.info("Removing item {} from order {}", itemId, orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getStatus() == OrderItem.ItemStatus.NEW) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                orderItemRepository.save(item);
            } else {
                order.getItems().remove(item);
                orderRepository.save(order);
            }
        }

        return orderMapper.toDto(orderRepository.findById(orderId).orElseThrow());
    }

    /**
     * Send all NEW items to the kitchen (change status to COOKING).
     */
    @Transactional
    @Override
    public OrderDto sendToKitchen(UUID orderId) {
        log.info("Sending order {} to kitchen", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.getItems().forEach(item -> {
            if (item.getStatus() == OrderItem.ItemStatus.NEW) {
                item.setStatus(OrderItem.ItemStatus.COOKING);
            }
        });

        return orderMapper.toDto(orderRepository.save(order));
    }

    /**
     * Update status of a single order item.
     */
    @Transactional
    @Override
    public OrderDto updateItemStatus(UUID orderId, UUID itemId, String newStatus) {
        log.info("Updating item {} status to {}", itemId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setStatus(orderMapper.toItemStatusEnum(newStatus)));

        return orderMapper.toDto(orderRepository.save(order));
    }

    /**
     * Close an order:
     * 1. Calculate total
     * 2. Create an INCOME transaction in the budget
     * 3. Mark order as CLOSED
     */
    @Transactional
    @Override
    public OrderDto closeOrder(UUID orderId) {
        log.info("Closing order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Calculate total
        BigDecimal total = order.getItems().stream()
                .filter(item -> item.getStatus() != OrderItem.ItemStatus.CANCELLED)
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
        inventoryService.consumeByOrder(order);

        // Free the table
        RestaurantTable table = order.getTable();
        table.setStatus(RestaurantTable.TableStatus.FREE);
        table.setWaiterId(null);
        table.setStatusUpdatedAt(Instant.now());
        tableRepository.save(table);

        log.info("Order {} closed. Total: {} ₽", orderId, total);

        return orderMapper.toDto(orderRepository.save(order));
    }

    /**
     * Clear/cancel an active order for a table.
     */
    @Transactional
    @Override
    public void clearOrder(UUID tableId) {
        log.info("Clearing order for table {}", tableId);
        orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .ifPresent(order -> {
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    order.setClosedAt(Instant.now());
                    orderRepository.save(order);

                    RestaurantTable table = order.getTable();
                    if (table != null) {
                        table.setStatus(RestaurantTable.TableStatus.FREE);
                        table.setWaiterId(null);
                        table.setStatusUpdatedAt(Instant.now());
                        tableRepository.save(table);
                    }
                });
    }

    /**
     * Calculate total for an active order (without closing).
     */
    @Transactional(readOnly = true)
    @Override
    public BigDecimal getTotal(UUID tableId) {
        return orderRepository.findFirstByTableIdAndStatus(tableId, Order.OrderStatus.ACTIVE)
                .map(order -> order.getItems().stream()
                        .filter(item -> item.getStatus() != OrderItem.ItemStatus.CANCELLED)
                        .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }
}

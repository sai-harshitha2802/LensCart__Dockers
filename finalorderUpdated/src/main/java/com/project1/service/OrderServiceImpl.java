package com.project1.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.project1.dto.*;
import com.project1.entity.Order;
import com.project1.entity.OrderItem;
import com.project1.entity.OrderStatus;
import com.project1.entity.PaymentStatus;
import com.project1.exception.StockNotAvailableException;
import com.project1.feign.CartFeignClient;
import com.project1.feign.ProductFeignClient;
import com.project1.repository.OrderItemRepository;
import com.project1.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartFeignClient cartClient;
    private final ProductFeignClient productFeignClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            CartFeignClient cartClient,
                            ProductFeignClient productFeignClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartClient = cartClient;
        this.productFeignClient = productFeignClient;
    }

@Override
public OrderDTO placeOrder(Long customerId, String address) {
    logger.info("Placing order for customer: {}", customerId);

    // Fetch cart for customer
    CartDTO cart = cartClient.getCart(customerId);

    if (cart == null || cart.getCartItems().isEmpty()) {
        logger.warn("Cart is empty for customer: {}", customerId);
        throw new RuntimeException("Cart is empty. Add items before placing an order.");
    }

    // 🛑 Check stock for each product
    List<String> outOfStockItems = new ArrayList<>();

    for (var item : cart.getCartItems()) {
        var product = productFeignClient.getProductById(item.getProductId());

        if (item.getQuantity() > product.getQuantity()) {
            outOfStockItems.add(
                product.getId() + " - Requested: " + item.getQuantity()
                + ", Available: " + product.getQuantity()
            );
        }
    }

//    if (!outOfStockItems.isEmpty()) {
//        throw new RuntimeException("Not enough stock for:\n" + String.join("\n", outOfStockItems));
//    }
    if (!outOfStockItems.isEmpty()) {
        throw new StockNotAvailableException("Not enough stock for:\n" + String.join("\n", outOfStockItems));
    }
    // ✅ Calculate total
    double totalAmount = cart.getCartItems().stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

    logger.info("Total amount: {}", totalAmount);

    // ✅ Create order
    Order order = new Order();
    order.setCustomerId(customerId);
    order.setAddress(address);
    order.setOrderDateTime(LocalDateTime.now());
    order.setTotalAmount(totalAmount);
    order.setPaymentStatus(PaymentStatus.PENDING);
    order.setOrderStatus(OrderStatus.PROCESSING);

    order = orderRepository.save(order);
    Long savedOrderId = order.getOrderId();
    logger.info("Order saved with ID: {}", savedOrderId);

    // ✅ Save order items
    List<OrderItem> orderItems = cart.getCartItems().stream().map(item -> {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(savedOrderId);
        orderItem.setProductId(item.getProductId());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setPrice(item.getPrice());
        orderItem.setImageUrl(item.getImageUrl());
        return orderItem;
    }).collect(Collectors.toList());

    orderItemRepository.saveAll(orderItems);
    logger.info("Order items saved for Order ID: {}", savedOrderId);

    // ✅ Return response
    return new OrderDTO(
            savedOrderId,
            customerId,
            address,
            order.getOrderDateTime(),
            totalAmount,
            PaymentStatus.PENDING,
            OrderStatus.PROCESSING,
            orderItems.stream().map(item -> new OrderItemDTO(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getImageUrl()
            )).collect(Collectors.toList())
    );
}


    @Override
    public List<OrderDTO> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateTimeDesc(customerId);

        return orders.stream().map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
            List<OrderItemDTO> itemDTOs = orderItems.stream()
                    .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getPrice(), item.getImageUrl()))
                    .collect(Collectors.toList());

            return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
                    order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(),
                    order.getOrderStatus(), itemDTOs);
        }).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateTimeDesc();

        return orders.stream().map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
            List<OrderItemDTO> itemDTOs = orderItems.stream()
                    .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getPrice(), item.getImageUrl()))
                    .collect(Collectors.toList());

            return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
                    order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(),
                    order.getOrderStatus(), itemDTOs);
        }).collect(Collectors.toList());
    }

@Override
public PaymentResponse makePayment(Long orderId, double paymentAmount) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
        return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Completed",
                "Payment already completed for this order.");
    }

    // ✅ Fix: Float-safe comparison
    if (Math.abs(paymentAmount - order.getTotalAmount()) > 0.01) {
        return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Failed",
                "Invalid payment amount. Expected: " + order.getTotalAmount());
    }

    // ✅ Reduce stock for each item
    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
    for (OrderItem item : items) {
        productFeignClient.reduceStock(item.getProductId(), item.getQuantity());
    }

    // ✅ Mark order as paid
    order.setPaymentStatus(PaymentStatus.COMPLETED);
    orderRepository.save(order);

    return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Completed",
            "Payment successful.");
}



    @Override
    public List<OrderDTO> getPaidOrdersForAdmin() {
        List<Order> paidOrders = orderRepository.findByPaymentStatusAndOrderStatusNotIn(
                PaymentStatus.COMPLETED, Arrays.asList(OrderStatus.DELIVERED, OrderStatus.SHIPPED));

        return paidOrders.stream().map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
            List<OrderItemDTO> itemDTOs = orderItems.stream()
                    .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getPrice(), item.getImageUrl()))
                    .collect(Collectors.toList());

            return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
                    order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(),
                    order.getOrderStatus(), itemDTOs);
        }).collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatusBulk(List<Long> orderIds, String status) {
        List<Order> orders = orderRepository.findAllById(orderIds);

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

        for (Order order : orders) {
            if (order.getOrderStatus() != OrderStatus.PROCESSING && order.getOrderStatus() != OrderStatus.SHIPPED) {
                continue;
            }
            order.setOrderStatus(newStatus);
        }

        orderRepository.saveAll(orders);
    }

    @Override
    public boolean deletePendingOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (order.getOrderStatus() == OrderStatus.PROCESSING && order.getPaymentStatus() == PaymentStatus.PENDING) {
                orderRepository.delete(order);
                return true;
            }
        }
        return false;
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

        List<OrderItemDTO> itemDTOs = orderItems.stream()
                .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity(), item.getPrice(), item.getImageUrl()))
                .collect(Collectors.toList());

        return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
                order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(),
                order.getOrderStatus(), itemDTOs);
    }
    
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        // Restore stock for each product in the order
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            String productId = item.getProductId();
            int quantity = item.getQuantity();

            productFeignClient.increaseStock(productId, quantity);
        }

        // Update order status
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

}
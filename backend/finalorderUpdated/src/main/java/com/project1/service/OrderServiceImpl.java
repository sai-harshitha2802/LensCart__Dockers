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
import com.project1.repository.OrderItemRepository;
import com.project1.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartFeignClient cartClient;

	public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			CartFeignClient cartClient) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartClient = cartClient;

	}

	@Override
	public OrderDTO placeOrder(Long customerId, String address) {
		logger.info("Placing order for customer: {}", customerId);

		CartDTO cart = cartClient.getCart(customerId);

		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Cart is empty for customer: {}", customerId);
			throw new RuntimeException("Cart is empty. Add items before placing an order.");
		}

		double totalAmount = cart.getCartItems().stream().mapToDouble(item -> {
			return item.getPrice() * item.getQuantity();
		}).sum();

		logger.info("Total discounted amount calculated: {}", totalAmount);

		Order order = new Order();
		order.setCustomerId(customerId);
		order.setAddress(address);

		order.setOrderDateTime(LocalDateTime.now());
		order.setTotalAmount(totalAmount);
		order.setPaymentStatus(PaymentStatus.PENDING); // Using PaymentStatus enum
		order.setOrderStatus(OrderStatus.PROCESSING); // Using OrderStatus enum (Only PROCESSING)

		order = orderRepository.save(order);
		final Long savedOrderId = order.getOrderId();

		if (savedOrderId == null) {
			// throw new Exception("Order could not be saved for customer: " + customerId);
		}

		logger.info("Order saved with ID: {}", savedOrderId);

		List<OrderItem> orderItems = cart.getCartItems().stream().map(item -> {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrderId(savedOrderId); // Manually setting order ID
			orderItem.setProductId(item.getProductId());
			// orderItem.setProductName(item.getProductName());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setPrice(item.getPrice());
			orderItem.setImageUrl(item.getImageUrl());
			// orderItem.setWeightingm(item.getWeightingm());
			// orderItem.setPrice(item.getPrice());
			// orderItem.setDiscountedPrice(item.getDiscountedPrice());
			return orderItem;
		}).collect(Collectors.toList());

		orderItemRepository.saveAll(orderItems);
		logger.info("Order items saved for Order ID: {}", savedOrderId);

		return new OrderDTO(savedOrderId, // Long
				customerId, // String
				address, // String
				// String
				order.getOrderDateTime(), // LocalDateTime
				totalAmount, // double
				PaymentStatus.PENDING, // PaymentStatus
				OrderStatus.PROCESSING, // OrderStatus
				orderItems.stream().map(item -> new OrderItemDTO(item.getProductId(),

						item.getQuantity(),

						item.getPrice(),
						item.getImageUrl()

				)).collect(Collectors.toList()) // List<OrderItemDTO>
		);
	}

	@Override
	public List<OrderDTO> getCustomerOrders(Long customerId) {
		logger.info("Fetching orders for customer: {}", customerId);

		List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateTimeDesc(customerId);

		return orders.stream().map(order -> {
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

			List<OrderItemDTO> orderItemDTOs = orderItems.stream().map(item -> new OrderItemDTO(item.getProductId(),

					item.getQuantity(),

					item.getPrice(),
					item.getImageUrl()

			)).collect(Collectors.toList());

			return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
					// order.getPincode(),
					order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(), // ✅ use directly
					order.getOrderStatus(), // ✅ use directly
					orderItemDTOs);
		}).collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getAllOrders() {
		logger.info("Fetching all orders for admin");
		List<Order> orders = orderRepository.findAllByOrderByOrderDateTimeDesc();

		return orders.stream().map(order -> {
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

			List<OrderItemDTO> orderItemDTOs = orderItems.stream().map(item -> new OrderItemDTO(item.getProductId(),

					item.getQuantity(),

					item.getPrice(),
					
					item.getImageUrl()
					
					

			)).collect(Collectors.toList());

			return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
					// order.getPincode(),
					order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(), // ✅ Use directly
					order.getOrderStatus(), // ✅ Use directly
					orderItemDTOs);
		}).collect(Collectors.toList());
	}

	public PaymentResponse makePayment(Long orderId, double paymentAmount) {
		logger.info("Processing payment for Order ID: {}", orderId);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		if (order.getPaymentStatus() == PaymentStatus.COMPLETED) { // ✅ Compare enum directly
			return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Completed",
					"Payment already completed for this order.");
		}

		if (paymentAmount != order.getTotalAmount()) {
			return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Failed",
					"Invalid payment amount. Expected: " + order.getTotalAmount());
		}

		order.setPaymentStatus(PaymentStatus.COMPLETED); // ✅ Set enum directly
		orderRepository.save(order);

		logger.info("Payment successful for Order ID: {}", orderId);

		return new PaymentResponse(orderId, order.getCustomerId(), order.getTotalAmount(), "Completed",
				"Payment successful.");
	}

	@Override
	public List<OrderDTO> getPaidOrdersForAdmin() {
		logger.info("Fetching all paid orders (excluding delivered orders)");

		List<Order> paidOrders = orderRepository.findByPaymentStatusAndOrderStatusNotIn(PaymentStatus.COMPLETED, // ✅
																													// Use
																													// enum
																													// directly
				Arrays.asList(OrderStatus.DELIVERED, OrderStatus.SHIPPED) // ✅ Use enum directly
		);

		return paidOrders.stream().map(order -> {
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

			List<OrderItemDTO> orderItemDTOs = orderItems.stream().map(item -> new OrderItemDTO(item.getProductId(),
					// item.getProductName(),
					item.getQuantity(),
					// item.getWeightingm(),
					item.getPrice(),
					item.getImageUrl()
			// item.getDiscountedPrice()
			)).collect(Collectors.toList());

			return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
					// order.getPincode(),
					order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(), // ✅ Use enum directly
					order.getOrderStatus(), // ✅ Use enum directly
					orderItemDTOs);
		}).collect(Collectors.toList());
	}

	@Override
	public void updateOrderStatusBulk(List<Long> orderIds, String status) {
		logger.info("Updating order statuses in bulk: {} orders", orderIds.size());

		if (orderIds == null || orderIds.isEmpty()) {
			throw new IllegalArgumentException("Order ID list cannot be empty.");
		}

		List<Order> orders = orderRepository.findAllById(orderIds);

		if (orders.isEmpty()) {
			throw new RuntimeException("No orders found for the given IDs.");
		}

		OrderStatus newStatus;
		try {
			newStatus = OrderStatus.valueOf(status.toUpperCase()); // ✅ Convert string to enum
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid order status: " + status);
		}

		for (Order order : orders) {
			if (order.getOrderStatus() != OrderStatus.PROCESSING && order.getOrderStatus() != OrderStatus.SHIPPED) {
				logger.warn("Skipping order {} as it's already {}", order.getOrderId(), order.getOrderStatus());
				continue;
			}
			order.setOrderStatus(newStatus); // ✅ Set enum
		}

		orderRepository.saveAll(orders);

		logger.info("Order statuses updated successfully.");
	}

	public boolean deletePendingOrder(Long orderId) {
		Optional<Order> orderOptional = orderRepository.findById(orderId);

		if (orderOptional.isPresent()) {
			Order order = orderOptional.get();

			// Log the status and payment status to debug
			System.out.println("Order Status: " + order.getOrderStatus());
			System.out.println("Payment Status: " + order.getPaymentStatus());

			// Compare the enums directly
			if (order.getOrderStatus() == OrderStatus.PROCESSING && order.getPaymentStatus() == PaymentStatus.PENDING) {
				System.out.println("Deleting order with ID: " + orderId);

				// Delete the order if both conditions are met
				orderRepository.delete(order);
				return true;
			} else {
				// Log the reason why the order is not being deleted
				System.out.println("Order deletion conditions not met. " + "Order Status: " + order.getOrderStatus()
						+ ", " + "Payment Status: " + order.getPaymentStatus());
				return false;
			}
		}
		System.out.println("Order with ID " + orderId + " not found.");
		return false;
	}

	public OrderDTO getOrderById(Long orderId) {
		Order order = orderRepository.findById(orderId).get();

		List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

		List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> new OrderItemDTO(item.getProductId(),
				// item.getProductName(),
				item.getQuantity(),
				// item.getWeightingm(),
				item.getPrice(),
				item.getImageUrl()
		// item.getDiscountedPrice()
		)).collect(Collectors.toList());

		return new OrderDTO(order.getOrderId(), order.getCustomerId(), order.getAddress(),
				// order.getPincode(),
				order.getOrderDateTime(), order.getTotalAmount(), order.getPaymentStatus(), order.getOrderStatus(),
				itemDTOs);
	}

}

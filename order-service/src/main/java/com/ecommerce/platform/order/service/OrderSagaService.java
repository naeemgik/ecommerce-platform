package com.ecommerce.platform.order.service;

import com.ecommerce.platform.events.DeliveryScheduledEvent;
import com.ecommerce.platform.events.InventoryRejectedEvent;
import com.ecommerce.platform.events.InventoryReservedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.OrderCancelledEvent;
import com.ecommerce.platform.events.OrderCreatedEvent;
import com.ecommerce.platform.events.OrderLineItem;
import com.ecommerce.platform.events.OrderStatus;
import com.ecommerce.platform.events.PaymentFailedEvent;
import com.ecommerce.platform.events.PaymentProcessedEvent;
import com.ecommerce.platform.events.ProductView;
import com.ecommerce.platform.events.FulfillmentConfirmedEvent;
import com.ecommerce.platform.order.domain.PurchaseOrder;
import com.ecommerce.platform.order.domain.PurchaseOrderItem;
import com.ecommerce.platform.order.repository.PurchaseOrderRepository;
import com.ecommerce.platform.order.web.dto.CreateOrderLineRequest;
import com.ecommerce.platform.order.web.dto.CreateOrderRequest;
import com.ecommerce.platform.order.web.dto.OrderItemResponse;
import com.ecommerce.platform.order.web.dto.OrderResponse;
import jakarta.transaction.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderSagaService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductCatalogClient productCatalogClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderSagaService(
            PurchaseOrderRepository purchaseOrderRepository,
            ProductCatalogClient productCatalogClient,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productCatalogClient = productCatalogClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<ProductView> products = request.items().stream()
                .map(CreateOrderLineRequest::productId)
                .map(productCatalogClient::fetchProduct)
                .map(CompletableFuture::join)
                .toList();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> orderItems = new java.util.ArrayList<>();

        for (int index = 0; index < request.items().size(); index++) {
            CreateOrderLineRequest lineRequest = request.items().get(index);
            ProductView product = products.get(index);
            BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(lineRequest.quantity()));
            totalAmount = totalAmount.add(lineTotal);
            orderItems.add(new PurchaseOrderItem(product.id(), product.name(), lineRequest.quantity(), product.price()));
        }

        PurchaseOrder order = new PurchaseOrder(request.customerId(), totalAmount);
        orderItems.forEach(order::addItem);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, toOrderCreatedEvent(savedOrder));
        return toResponse(savedOrder);
    }

    @Transactional
    public void markInventoryReserved(InventoryReservedEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.updateStatus(OrderStatus.INVENTORY_RESERVED);
    }

    @Transactional
    public void markInventoryFailed(InventoryRejectedEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.markFailed(OrderStatus.INVENTORY_FAILED, event.reason());
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, new OrderCancelledEvent(order.getId(), order.getCustomerId(), event.reason()));
    }

    @Transactional
    public void markPaymentCompleted(PaymentProcessedEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.updateStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    @Transactional
    public void markPaymentFailed(PaymentFailedEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.markFailed(OrderStatus.PAYMENT_FAILED, event.reason());
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, new OrderCancelledEvent(order.getId(), order.getCustomerId(), event.reason()));
    }

    @Transactional
    public void markFulfillmentConfirmed(FulfillmentConfirmedEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.updateStatus(OrderStatus.FULFILLMENT_CONFIRMED);
    }

    @Transactional
    public void markDeliveryScheduled(DeliveryScheduledEvent event) {
        PurchaseOrder order = loadOrder(event.orderId());
        order.updateStatus(OrderStatus.COMPLETED);
    }

    public OrderResponse getOrder(Long orderId) {
        return toResponse(loadOrder(orderId));
    }

    private PurchaseOrder loadOrder(Long orderId) {
        return purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order %d not found".formatted(orderId)));
    }

    private OrderCreatedEvent toOrderCreatedEvent(PurchaseOrder order) {
        List<OrderLineItem> items = order.getItems().stream()
                .map(item -> new OrderLineItem(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                .toList();
        return new OrderCreatedEvent(order.getId(), order.getCustomerId(), items, order.getTotalAmount());
    }

    private OrderResponse toResponse(PurchaseOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getFailureReason(),
                order.getCreatedAt(),
                items
        );
    }
}

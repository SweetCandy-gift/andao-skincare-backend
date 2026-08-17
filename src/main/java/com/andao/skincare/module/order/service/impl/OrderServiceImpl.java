package com.andao.skincare.module.order.service.impl;

import com.andao.skincare.module.cart.service.CartService;
import com.andao.skincare.module.cart.vo.CartItemVO;
import com.andao.skincare.module.cart.vo.CartVO;
import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.entity.Order;
import com.andao.skincare.module.order.entity.OrderItem;
import com.andao.skincare.module.order.mapper.OrderItemMapper;
import com.andao.skincare.module.order.mapper.OrderMapper;
import com.andao.skincare.module.order.service.OrderService;
import com.andao.skincare.module.order.vo.OrderItemVO;
import com.andao.skincare.module.order.vo.OrderVO;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final int ORDER_STATUS_CREATED = 0;
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartService cartService,
                            CurrentUserProvider currentUserProvider) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartService = cartService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(OrderCreateDTO request) {
        CartVO cart = cartService.list();
        validateCart(cart);

        LocalDateTime now = LocalDateTime.now();
        Order order = buildOrder(request, cart, now);
        if (orderMapper.insert(order) != 1) {
            throw new IllegalStateException("订单保存失败");
        }

        List<OrderItem> orderItems = cart.items().stream()
                .map(item -> buildOrderItem(order.getId(), item, now))
                .toList();
        for (OrderItem orderItem : orderItems) {
            if (orderItemMapper.insert(orderItem) != 1) {
                throw new IllegalStateException("订单明细保存失败");
            }
        }

        clearCartAfterCommit(order.getOrderNo());
        return toVO(order, orderItems);
    }

    private void validateCart(CartVO cart) {
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        boolean hasUnavailableItem = cart.items().stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.available()));
        if (hasUnavailableItem) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "购物车包含不可用或库存不足的商品");
        }
    }

    private Order buildOrder(OrderCreateDTO request, CartVO cart, LocalDateTime now) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo(now));
        order.setUserId(currentUserProvider.getCurrentUserId());
        order.setTotalAmount(cart.totalAmount());
        order.setTotalQuantity(cart.totalQuantity());
        order.setStatus(ORDER_STATUS_CREATED);
        order.setRemark(StringUtils.hasText(request.remark()) ? request.remark().trim() : null);
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private OrderItem buildOrderItem(Long orderId, CartItemVO cartItem, LocalDateTime now) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cartItem.productId());
        orderItem.setProductName(cartItem.productName());
        orderItem.setCoverUrl(cartItem.coverUrl());
        orderItem.setProductPrice(cartItem.price());
        orderItem.setQuantity(cartItem.quantity());
        orderItem.setSubtotal(cartItem.subtotal());
        orderItem.setCreatedAt(now);
        return orderItem;
    }

    private String generateOrderNo(LocalDateTime now) {
        String randomPart = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
        return "AD" + now.format(ORDER_TIME_FORMATTER) + randomPart;
    }

    private void clearCartAfterCommit(String orderNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cartService.clear();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    cartService.clear();
                } catch (RuntimeException exception) {
                    log.error("订单已提交但购物车清理失败，orderNo={}", orderNo, exception);
                }
            }
        });
    }

    private OrderVO toVO(Order order, List<OrderItem> orderItems) {
        List<OrderItemVO> itemVOs = orderItems.stream()
                .map(item -> new OrderItemVO(
                        item.getProductId(), item.getProductName(), item.getCoverUrl(),
                        item.getProductPrice(), item.getQuantity(), item.getSubtotal()))
                .toList();
        return new OrderVO(
                order.getId(), order.getOrderNo(), order.getUserId(), order.getTotalAmount(),
                order.getTotalQuantity(), order.getStatus(), order.getRemark(),
                order.getCreatedAt(), itemVOs);
    }
}

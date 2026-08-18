package com.andao.skincare.module.order.service.impl;

import com.andao.skincare.common.exception.BusinessException;
import com.andao.skincare.common.exception.ErrorCode;
import com.andao.skincare.module.cart.service.CartService;
import com.andao.skincare.module.cart.vo.CartItemVO;
import com.andao.skincare.module.cart.vo.CartVO;
import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.entity.Order;
import com.andao.skincare.module.order.entity.OrderItem;
import com.andao.skincare.module.order.entity.OrderStatus;
import com.andao.skincare.module.order.mapper.OrderItemMapper;
import com.andao.skincare.module.order.mapper.OrderMapper;
import com.andao.skincare.module.order.service.OrderService;
import com.andao.skincare.module.order.vo.OrderItemVO;
import com.andao.skincare.module.order.vo.OrderVO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final ProductService productService;
    private final CurrentUserProvider currentUserProvider;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartService cartService,
                            ProductService productService,
                            CurrentUserProvider currentUserProvider) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartService = cartService;
        this.productService = productService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * 从当前用户购物车创建订单。订单主表和全部明细必须作为一个整体成功或失败，
     * rollbackFor 确保任意保存异常都会回滚 MySQL 数据，避免出现无明细订单或残缺订单。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(OrderCreateDTO request) {
        CartVO cart = cartService.list();
        validateCart(cart);

        // 库存扣减与订单写入共用当前 MySQL 事务，任一步失败都会整体回滚。
        cart.items().forEach(item -> productService.deductStock(item.productId(), item.quantity()));

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

    @Override
    public List<OrderVO> list() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt)
                .orderByDesc(Order::getId));
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrderId = orderItemMapper
                .selectList(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
                        .orderByAsc(OrderItem::getId))
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream()
                .map(order -> toVO(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    @Override
    public OrderVO getById(Long id) {
        Order order = findOwnedOrder(id);
        return toVO(order, findOrderItems(order.getId()));
    }

    /**
     * 只有已创建订单允许取消，因为支付、发货或完成后的订单需要退款、物流等额外流程，
     * 当前阶段没有实现这些能力。条件更新同时校验原状态，避免并发状态变化被错误覆盖。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long id) {
        Order order = findOwnedOrder(id);
        if (!Integer.valueOf(OrderStatus.ORDER_CREATED.getCode()).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getUserId, order.getUserId())
                .eq(Order::getStatus, OrderStatus.ORDER_CREATED.getCode())
                .set(Order::getStatus, OrderStatus.ORDER_CANCELLED.getCode())
                .set(Order::getUpdatedAt, now));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_CHANGED);
        }

        order.setStatus(OrderStatus.ORDER_CANCELLED.getCode());
        order.setUpdatedAt(now);
        return toVO(order, findOrderItems(order.getId()));
    }

    private void validateCart(CartVO cart) {
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        boolean hasUnavailableItem = cart.items().stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.available()));
        if (hasUnavailableItem) {
            throw new BusinessException(ErrorCode.CART_PRODUCT_UNAVAILABLE);
        }
    }

    private Order buildOrder(OrderCreateDTO request, CartVO cart, LocalDateTime now) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo(now));
        order.setUserId(currentUserProvider.getCurrentUserId());
        order.setTotalAmount(cart.totalAmount());
        order.setTotalQuantity(cart.totalQuantity());
        order.setStatus(OrderStatus.ORDER_CREATED.getCode());
        order.setRemark(StringUtils.hasText(request.remark()) ? request.remark().trim() : null);
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private OrderItem buildOrderItem(Long orderId, CartItemVO cartItem, LocalDateTime now) {
        // 下单时保存商品名称、图片和成交价快照，后续商品修改不会改变历史订单。
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

    /**
     * 查询条件始终同时包含订单 ID 和当前用户 ID。这样既阻止越权读取或修改他人订单，
     * 也不会通过不同错误响应向调用方泄露其他用户的订单是否存在。
     */
    private Order findOwnedOrder(Long orderId) {
        Long userId = currentUserProvider.getCurrentUserId();
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private List<OrderItem> findOrderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId));
    }

    private void clearCartAfterCommit(String orderNo) {
        /*
         * Redis 不参与 MySQL 本地事务，因此只在数据库提交成功后清空购物车：
         * 既避免订单回滚时丢失购物车，也避免已下单商品继续留在购物车造成重复下单。
         */
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

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
import com.andao.skincare.module.order.vo.OrderVO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private final OrderMapper orderMapper = mock(OrderMapper.class);
    private final OrderItemMapper orderItemMapper = mock(OrderItemMapper.class);
    private final CartService cartService = mock(CartService.class);
    private final ProductService productService = mock(ProductService.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "order-test"), Order.class);
        orderService = new OrderServiceImpl(
                orderMapper, orderItemMapper, cartService, productService, currentUserProvider);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1001L);
    }

    @Test
    void shouldListCurrentUserOrdersWithItems() {
        Order order = order(2001L, OrderStatus.ORDER_CREATED);
        OrderItem item = orderItem(3001L, order.getId());
        when(orderMapper.selectList(any())).thenReturn(List.of(order));
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));

        List<OrderVO> result = orderService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(order.getId());
        assertThat(result.get(0).items()).hasSize(1);
    }

    @Test
    void shouldDeductStockBeforeSavingOrder() {
        CartItemVO item = cartItem();
        when(cartService.list()).thenReturn(new CartVO(
                1001L, List.of(item), item.quantity(), item.subtotal()));
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(2001L);
            return 1;
        }).when(orderMapper).insert(any(Order.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        orderService.create(new OrderCreateDTO(null));

        InOrder invocationOrder = inOrder(productService, orderMapper);
        invocationOrder.verify(productService).deductStock(item.productId(), item.quantity());
        invocationOrder.verify(orderMapper).insert(any(Order.class));
    }

    @Test
    void shouldNotSaveOrderWhenStockDeductionFails() {
        CartItemVO item = cartItem();
        when(cartService.list()).thenReturn(new CartVO(
                1001L, List.of(item), item.quantity(), item.subtotal()));
        doThrow(new BusinessException(ErrorCode.STOCK_CONFLICT))
                .when(productService).deductStock(item.productId(), item.quantity());

        assertThatThrownBy(() -> orderService.create(new OrderCreateDTO(null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_CONFLICT));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    void shouldHideOrderThatDoesNotBelongToCurrentUser() {
        when(orderMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> orderService.getById(2001L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    void shouldCancelCreatedOrder() {
        Order order = order(2001L, OrderStatus.ORDER_CREATED);
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(1);
        when(orderItemMapper.selectList(any())).thenReturn(List.of());

        OrderVO result = orderService.cancel(order.getId());

        assertThat(result.status()).isEqualTo(OrderStatus.ORDER_CANCELLED.getCode());
        verify(orderMapper).update(isNull(), any());
    }

    @Test
    void shouldRejectCancellationWhenOrderIsNotCreated() {
        Order order = order(2001L, OrderStatus.ORDER_PAID);
        when(orderMapper.selectOne(any())).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancel(order.getId()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANNOT_CANCEL));
        verify(orderMapper, never()).update(isNull(), any());
    }

    private Order order(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("AD202608180001");
        order.setUserId(1001L);
        order.setTotalAmount(new BigDecimal("129.00"));
        order.setTotalQuantity(1);
        order.setStatus(status.getCode());
        order.setCreatedAt(LocalDateTime.of(2026, 8, 18, 20, 0));
        order.setUpdatedAt(order.getCreatedAt());
        return order;
    }

    private OrderItem orderItem(Long id, Long orderId) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductId(4001L);
        item.setProductName("安稻修护面霜");
        item.setProductPrice(new BigDecimal("129.00"));
        item.setQuantity(1);
        item.setSubtotal(new BigDecimal("129.00"));
        item.setCreatedAt(LocalDateTime.of(2026, 8, 18, 20, 0));
        return item;
    }

    private CartItemVO cartItem() {
        return new CartItemVO(
                4001L, "安稻修护面霜", null, new BigDecimal("129.00"),
                2, new BigDecimal("258.00"), 10, true);
    }
}

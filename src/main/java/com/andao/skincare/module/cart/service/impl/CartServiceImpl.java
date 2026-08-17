package com.andao.skincare.module.cart.service.impl;

import com.andao.skincare.module.cart.dto.CartAddDTO;
import com.andao.skincare.module.cart.dto.CartUpdateDTO;
import com.andao.skincare.module.cart.entity.CartItem;
import com.andao.skincare.module.cart.service.CartService;
import com.andao.skincare.module.cart.vo.CartItemVO;
import com.andao.skincare.module.cart.vo.CartVO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductDetailVO;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final int MAX_QUANTITY = 999;

    private final StringRedisTemplate redisTemplate;
    private final ProductService productService;
    private final CurrentUserProvider currentUserProvider;

    public CartServiceImpl(StringRedisTemplate redisTemplate,
                           ProductService productService,
                           CurrentUserProvider currentUserProvider) {
        this.redisTemplate = redisTemplate;
        this.productService = productService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public CartItemVO add(CartAddDTO request) {
        ProductDetailVO product = productService.getById(request.productId());
        String key = currentCartKey();
        String field = request.productId().toString();
        int currentQuantity = parseQuantity(redisTemplate.opsForHash().get(key, field));
        int newQuantity = currentQuantity + request.quantity();
        validateQuantity(product, newQuantity);

        redisTemplate.opsForHash().put(key, field, Integer.toString(newQuantity));
        return toAvailableVO(new CartItem(request.productId(), newQuantity), product);
    }

    @Override
    public CartVO list() {
        Long userId = currentUserProvider.getCurrentUserId();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));
        List<CartItemVO> items = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = parseProductId(entry.getKey());
            int quantity = parseQuantity(entry.getValue());
            if (productId == null || quantity <= 0) {
                continue;
            }
            items.add(toCartItemVO(new CartItem(productId, quantity)));
        }
        items.sort(Comparator.comparing(CartItemVO::productId));

        int totalQuantity = items.stream()
                .filter(CartItemVO::available)
                .mapToInt(CartItemVO::quantity)
                .sum();
        BigDecimal totalAmount = items.stream()
                .filter(CartItemVO::available)
                .map(CartItemVO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartVO(userId, items, totalQuantity, totalAmount);
    }

    @Override
    public CartItemVO update(CartUpdateDTO request) {
        String key = currentCartKey();
        String field = request.productId().toString();
        if (!Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, field))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车中不存在该商品");
        }

        ProductDetailVO product = productService.getById(request.productId());
        validateQuantity(product, request.quantity());
        redisTemplate.opsForHash().put(key, field, request.quantity().toString());
        return toAvailableVO(new CartItem(request.productId(), request.quantity()), product);
    }

    @Override
    public void remove(Long productId) {
        redisTemplate.opsForHash().delete(currentCartKey(), productId.toString());
    }

    @Override
    public void clear() {
        redisTemplate.delete(currentCartKey());
    }

    private CartItemVO toCartItemVO(CartItem item) {
        try {
            return toAvailableVO(item, productService.getById(item.productId()));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != HttpStatus.NOT_FOUND.value()) {
                throw exception;
            }
            return new CartItemVO(
                    item.productId(), null, null, null, item.quantity(),
                    null, 0, false);
        }
    }

    private CartItemVO toAvailableVO(CartItem item, ProductDetailVO product) {
        BigDecimal subtotal = product.price().multiply(BigDecimal.valueOf(item.quantity()));
        boolean available = product.stock() != null && product.stock() >= item.quantity();
        return new CartItemVO(
                product.id(), product.name(), product.coverUrl(), product.price(), item.quantity(),
                subtotal, product.stock(), available);
    }

    private void validateQuantity(ProductDetailVO product, int quantity) {
        if (quantity > MAX_QUANTITY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "单个商品数量不能超过999");
        }
        if (product.stock() == null || product.stock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品库存不足");
        }
    }

    private String currentCartKey() {
        return cartKey(currentUserProvider.getCurrentUserId());
    }

    private String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    private int parseQuantity(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private Long parseProductId(Object value) {
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

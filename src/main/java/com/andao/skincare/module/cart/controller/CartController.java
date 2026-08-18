package com.andao.skincare.module.cart.controller;

import com.andao.skincare.common.result.Result;
import com.andao.skincare.module.cart.dto.CartAddDTO;
import com.andao.skincare.module.cart.dto.CartUpdateDTO;
import com.andao.skincare.module.cart.service.CartService;
import com.andao.skincare.module.cart.vo.CartItemVO;
import com.andao.skincare.module.cart.vo.CartVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Result<CartItemVO> add(@Valid @RequestBody CartAddDTO request) {
        return Result.success(cartService.add(request));
    }

    @GetMapping("/list")
    public Result<CartVO> list() {
        return Result.success(cartService.list());
    }

    @PutMapping("/update")
    public Result<CartItemVO> update(@Valid @RequestBody CartUpdateDTO request) {
        return Result.success(cartService.update(request));
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(
            @PathVariable @Positive(message = "商品ID必须为正数") Long productId) {
        cartService.remove(productId);
        return Result.success();
    }
}

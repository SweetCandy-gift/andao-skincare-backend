package com.andao.skincare.module.cart.service;

import com.andao.skincare.module.cart.dto.CartAddDTO;
import com.andao.skincare.module.cart.dto.CartUpdateDTO;
import com.andao.skincare.module.cart.vo.CartItemVO;
import com.andao.skincare.module.cart.vo.CartVO;

public interface CartService {

    CartItemVO add(CartAddDTO request);

    CartVO list();

    CartItemVO update(CartUpdateDTO request);

    void remove(Long productId);

    void clear();
}

package com.andao.skincare.module.order.service;

import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO create(OrderCreateDTO request);

    List<OrderVO> list();

    OrderVO getById(Long id);

    OrderVO cancel(Long id);
}

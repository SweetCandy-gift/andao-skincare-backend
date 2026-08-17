package com.andao.skincare.module.order.service;

import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.vo.OrderVO;

public interface OrderService {

    OrderVO create(OrderCreateDTO request);
}

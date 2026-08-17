package com.andao.skincare.module.order.controller;

import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.service.OrderService;
import com.andao.skincare.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderVO create(@Valid @RequestBody OrderCreateDTO request) {
        return orderService.create(request);
    }
}

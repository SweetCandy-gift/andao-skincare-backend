package com.andao.skincare.module.order.controller;

import com.andao.skincare.common.result.Result;
import com.andao.skincare.module.order.dto.OrderCreateDTO;
import com.andao.skincare.module.order.service.OrderService;
import com.andao.skincare.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO request) {
        return Result.success(orderService.create(request));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return Result.success(orderService.list());
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getById(
            @PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        return Result.success(orderService.getById(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(
            @PathVariable @Positive(message = "订单ID必须为正数") Long id) {
        return Result.success(orderService.cancel(id));
    }
}

package com.andao.skincare.module.product.controller;

import com.andao.skincare.common.result.Result;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductDetailVO;
import com.andao.skincare.module.product.vo.ProductListVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    public Result<List<ProductListVO>> list(@Valid ProductQueryDTO query) {
        return Result.success(productService.list(query));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> getById(
            @PathVariable @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(productService.getById(id));
    }
}

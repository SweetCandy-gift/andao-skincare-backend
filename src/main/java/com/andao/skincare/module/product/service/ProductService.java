package com.andao.skincare.module.product.service;

import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.vo.ProductDetailVO;
import com.andao.skincare.module.product.vo.ProductListVO;

import java.util.List;

public interface ProductService {

    List<ProductListVO> list(ProductQueryDTO query);

    ProductDetailVO getById(Long id);

    void deductStock(Long productId, Integer quantity);
}

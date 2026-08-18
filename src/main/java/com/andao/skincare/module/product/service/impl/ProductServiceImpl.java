package com.andao.skincare.module.product.service.impl;

import com.andao.skincare.common.exception.BusinessException;
import com.andao.skincare.common.exception.ErrorCode;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.entity.Category;
import com.andao.skincare.module.product.entity.Product;
import com.andao.skincare.module.product.mapper.CategoryMapper;
import com.andao.skincare.module.product.mapper.ProductMapper;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductDetailVO;
import com.andao.skincare.module.product.vo.ProductListVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int STATUS_ENABLED = 1;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<ProductListVO> list(ProductQueryDTO query) {
        Map<Long, Category> categoryMap = getEnabledCategoryMap();
        if (categoryMap.isEmpty()
                || query.getCategoryId() != null && !categoryMap.containsKey(query.getCategoryId())) {
            return Collections.emptyList();
        }

        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .in(Product::getCategoryId, categoryMap.keySet())
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .like(keyword != null, Product::getName, keyword)
                .eq(Product::getStatus, STATUS_ENABLED)
                .orderByDesc(Product::getCreatedAt)
                .orderByDesc(Product::getId));

        return products.stream()
                .map(product -> toListVO(product, categoryMap.get(product.getCategoryId())))
                .toList();
    }

    @Override
    public ProductDetailVO getById(Long id) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getStatus, STATUS_ENABLED));
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, product.getCategoryId())
                .eq(Category::getStatus, STATUS_ENABLED));
        if (category == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return toDetailVO(product, category);
    }

    /**
     * 使用数据库条件更新完成库存扣减。stock >= quantity 保证库存不会变为负数，
     * version 条件保证并发请求只能有一个使用当前版本成功更新，从而防止超卖。
     * MANDATORY 要求该方法必须加入订单事务，订单保存失败时库存才能一起回滚。
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deductStock(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_INVALID);
        }
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getStatus, STATUS_ENABLED));
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_UNAVAILABLE);
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        int updated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getStatus, STATUS_ENABLED)
                .eq(Product::getVersion, product.getVersion())
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - {0}", quantity)
                .setSql("version = version + 1"));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.STOCK_CONFLICT);
        }
    }

    private Map<Long, Category> getEnabledCategoryMap() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, STATUS_ENABLED)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId))
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private ProductListVO toListVO(Product product, Category category) {
        return new ProductListVO(
                product.getId(), product.getCategoryId(), category.getName(), product.getName(),
                product.getSubtitle(), product.getCoverUrl(), product.getPrice(),
                product.getStock(), product.getSales());
    }

    private ProductDetailVO toDetailVO(Product product, Category category) {
        return new ProductDetailVO(
                product.getId(), product.getCategoryId(), category.getName(), product.getName(),
                product.getSubtitle(), product.getDescription(), product.getCoverUrl(),
                product.getPrice(), product.getStock(), product.getSales(), product.getCreatedAt());
    }
}

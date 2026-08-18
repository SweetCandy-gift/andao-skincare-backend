package com.andao.skincare.module.product.service.impl;

import com.andao.skincare.common.exception.BusinessException;
import com.andao.skincare.common.exception.ErrorCode;
import com.andao.skincare.module.product.entity.Product;
import com.andao.skincare.module.product.mapper.CategoryMapper;
import com.andao.skincare.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final CategoryMapper categoryMapper = mock(CategoryMapper.class);
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "product-test"), Product.class);
        productService = new ProductServiceImpl(productMapper, categoryMapper);
    }

    @Test
    void shouldDeductStockWithStockAndVersionConditions() {
        Product product = product(10, 3);
        when(productMapper.selectOne(any())).thenReturn(product);
        when(productMapper.update(isNull(), any())).thenReturn(1);

        assertThatCode(() -> productService.deductStock(product.getId(), 2))
                .doesNotThrowAnyException();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaUpdateWrapper<Product>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(productMapper).update(isNull(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue().getSqlSegment())
                .contains("version", "stock");
        assertThat(wrapperCaptor.getValue().getSqlSet())
                .contains("stock = stock -", "version = version + 1");
    }

    @Test
    void shouldRejectDeductionWhenStockIsInsufficient() {
        Product product = product(1, 3);
        when(productMapper.selectOne(any())).thenReturn(product);

        assertThatThrownBy(() -> productService.deductStock(product.getId(), 2))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_INSUFFICIENT));
        verify(productMapper, never()).update(isNull(), any());
    }

    @Test
    void shouldRejectDeductionWhenConcurrentUpdateChangesVersion() {
        Product product = product(10, 3);
        when(productMapper.selectOne(any())).thenReturn(product);
        when(productMapper.update(isNull(), any())).thenReturn(0);

        assertThatThrownBy(() -> productService.deductStock(product.getId(), 2))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_CONFLICT));
    }

    private Product product(int stock, int version) {
        Product product = new Product();
        product.setId(1001L);
        product.setStock(stock);
        product.setVersion(version);
        product.setStatus(1);
        return product;
    }
}

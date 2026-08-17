package com.andao.skincare.module.product.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductQueryDTO {

    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    @Size(max = 100, message = "搜索关键词不能超过100个字符")
    private String keyword;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

package com.andao.skincare.module.user.service.impl;

import com.andao.skincare.module.user.service.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixedCurrentUserProvider implements CurrentUserProvider {

    private final Long testUserId;

    public FixedCurrentUserProvider(@Value("${app.auth.test-user-id}") Long testUserId) {
        this.testUserId = testUserId;
    }

    @Override
    public Long getCurrentUserId() {
        return testUserId;
    }
}

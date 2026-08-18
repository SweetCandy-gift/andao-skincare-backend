package com.andao.skincare.module.user.service.impl;

import com.andao.skincare.common.exception.BusinessException;
import com.andao.skincare.common.exception.ErrorCode;
import com.andao.skincare.module.user.dto.UserLoginDTO;
import com.andao.skincare.module.user.dto.UserRegisterDTO;
import com.andao.skincare.module.user.entity.User;
import com.andao.skincare.module.user.mapper.UserMapper;
import com.andao.skincare.module.user.service.UserService;
import com.andao.skincare.module.user.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private static final int USER_STATUS_ENABLED = 1;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO request) {
        String username = request.username().trim();
        if (findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(username);
        user.setStatus(USER_STATUS_ENABLED);
        user.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS, exception);
        }
        return toVO(user);
    }

    @Override
    public UserVO login(UserLoginDTO request) {
        User user = findByUsername(request.username().trim());
        // 密码只与 BCrypt 哈希比较；统一错误信息可避免暴露用户名是否存在。
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!Integer.valueOf(USER_STATUS_ENABLED).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return toVO(user);
    }

    @Override
    public boolean isActiveUser(Long userId, String username) {
        if (userId == null || username == null) {
            return false;
        }
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getUsername, username)
                .eq(User::getStatus, USER_STATUS_ENABLED)) > 0;
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getCreatedAt());
    }
}

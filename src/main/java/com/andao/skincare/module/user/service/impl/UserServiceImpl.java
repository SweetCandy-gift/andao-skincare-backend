package com.andao.skincare.module.user.service.impl;

import com.andao.skincare.module.user.dto.UserLoginDTO;
import com.andao.skincare.module.user.dto.UserRegisterDTO;
import com.andao.skincare.module.user.entity.User;
import com.andao.skincare.module.user.mapper.UserMapper;
import com.andao.skincare.module.user.service.UserService;
import com.andao.skincare.module.user.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在", exception);
        }
        return toVO(user);
    }

    @Override
    public UserVO login(UserLoginDTO request) {
        User user = findByUsername(request.username().trim());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(USER_STATUS_ENABLED).equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "用户已被禁用");
        }
        return toVO(user);
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getCreatedAt());
    }
}

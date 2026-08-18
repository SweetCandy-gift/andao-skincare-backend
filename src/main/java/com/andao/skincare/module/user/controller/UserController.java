package com.andao.skincare.module.user.controller;

import com.andao.skincare.module.user.dto.UserLoginDTO;
import com.andao.skincare.module.user.dto.UserRegisterDTO;
import com.andao.skincare.common.security.JwtTokenService;
import com.andao.skincare.common.result.Result;
import com.andao.skincare.module.user.service.UserService;
import com.andao.skincare.module.user.vo.UserLoginVO;
import com.andao.skincare.module.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public UserController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO request) {
        // Service 负责校验密码和用户状态；只有校验成功后，Controller 才签发访问 Token。
        UserVO user = userService.login(request);
        String token = jwtTokenService.generateToken(user.id(), user.username());
        return Result.success(new UserLoginVO(token, user));
    }
}

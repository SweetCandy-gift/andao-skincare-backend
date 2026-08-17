package com.andao.skincare.module.user.controller;

import com.andao.skincare.module.user.dto.UserLoginDTO;
import com.andao.skincare.module.user.dto.UserRegisterDTO;
import com.andao.skincare.module.user.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserVO register(@Valid @RequestBody UserRegisterDTO request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public UserVO login(@Valid @RequestBody UserLoginDTO request) {
        return userService.login(request);
    }
}

package com.andao.skincare.module.user.service;

import com.andao.skincare.module.user.dto.UserLoginDTO;
import com.andao.skincare.module.user.dto.UserRegisterDTO;
import com.andao.skincare.module.user.vo.UserVO;

public interface UserService {

    UserVO register(UserRegisterDTO request);

    UserVO login(UserLoginDTO request);

    boolean isActiveUser(Long userId, String username);
}

package com.takeout.controller.user;

import com.takeout.common.result.Result;
import com.takeout.pojo.dto.LoginDTO;
import com.takeout.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户端 - 登录（唯一免鉴权的用户端接口）
 */
@RestController
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(Map.of("token", token));
    }
}

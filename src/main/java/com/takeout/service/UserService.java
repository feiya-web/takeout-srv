package com.takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.common.constant.MessageConstant;
import com.takeout.common.constant.StatusConstant;
import com.takeout.common.exception.BaseException;
import com.takeout.common.properties.JwtProperties;
import com.takeout.common.utils.JwtUtil;
import com.takeout.common.utils.PasswordUtil;
import com.takeout.mapper.UserMapper;
import com.takeout.pojo.dto.LoginDTO;
import com.takeout.pojo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务：C端登录
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    public String login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername()));
        if (user == null || !PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        if (StatusConstant.DISABLE.equals(user.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_DISABLED);
        }
        return JwtUtil.createToken(jwtProperties.getUserSecret(), jwtProperties.getUserTtl(), user.getId());
    }
}

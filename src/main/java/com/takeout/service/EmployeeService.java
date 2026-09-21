package com.takeout.service;

import com.takeout.common.constant.MessageConstant;
import com.takeout.common.constant.StatusConstant;
import com.takeout.common.properties.JwtProperties;
import com.takeout.common.utils.JwtUtil;
import com.takeout.common.utils.PasswordUtil;
import com.takeout.mapper.EmployeeMapper;
import com.takeout.pojo.dto.LoginDTO;
import com.takeout.pojo.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 员工服务：管理端登录
 */
@Service
public class EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 员工登录：校验密码与账号状态，签发管理端 JWT
     */
    public String login(LoginDTO loginDTO) {
        Employee employee = employeeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Employee>()
                        .eq(Employee::getUsername, loginDTO.getUsername()));
        if (employee == null || !PasswordUtil.matches(loginDTO.getPassword(), employee.getPassword())) {
            throw new com.takeout.common.exception.BaseException(MessageConstant.PASSWORD_ERROR);
        }
        if (StatusConstant.DISABLE.equals(employee.getStatus())) {
            throw new com.takeout.common.exception.BaseException(MessageConstant.ACCOUNT_DISABLED);
        }
        return JwtUtil.createToken(jwtProperties.getAdminSecret(), jwtProperties.getAdminTtl(), employee.getId());
    }

    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee != null) {
            employee.setPassword(null);
        }
        return employee;
    }
}

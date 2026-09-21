package com.takeout.controller.admin;

import com.takeout.annotation.AutoLog;
import com.takeout.common.result.Result;
import com.takeout.pojo.dto.LoginDTO;
import com.takeout.pojo.entity.Employee;
import com.takeout.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 员工
 */
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录（唯一免鉴权的管理端接口）
     */
    @AutoLog(module = "员工模块", type = "登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        String token = employeeService.login(loginDTO);
        return Result.success(Map.of("token", token));
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }
}

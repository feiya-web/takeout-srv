package com.takeout.controller.user;

import com.takeout.common.result.Result;
import com.takeout.pojo.entity.Setmeal;
import com.takeout.pojo.vo.SetmealVO;
import com.takeout.pojo.vo.DishVO;
import com.takeout.service.SetmealService;
import com.takeout.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - 菜品/套餐浏览（高频查询，Cache Aside 缓存覆盖的主要读路径）
 */
@RestController
@RequestMapping("/user")
public class UserDishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/dish/list")
    public Result<List<DishVO>> list(Long categoryId) {
        return Result.success(dishService.listByCategoryId(categoryId));
    }

    @GetMapping("/setmeal/list")
    public Result<List<Setmeal>> listSetmeal(Long categoryId) {
        return Result.success(setmealService.listByCategoryId(categoryId));
    }

    @GetMapping("/setmeal/{id}")
    public Result<SetmealVO> getById(Long id) {
        return Result.success(setmealService.getByIdWithDishes(id));
    }
}

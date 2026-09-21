package com.takeout.controller.user;

import com.takeout.common.context.BaseContext;
import com.takeout.common.result.Result;
import com.takeout.pojo.dto.ShoppingCartDTO;
import com.takeout.pojo.entity.ShoppingCart;
import com.takeout.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 购物车
 */
@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ShoppingCartDTO dto) {
        shoppingCartService.add(BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    @PostMapping("/sub")
    public Result<Void> sub(@RequestBody ShoppingCartDTO dto) {
        shoppingCartService.sub(BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        return Result.success(shoppingCartService.list(BaseContext.getCurrentId()));
    }

    @DeleteMapping("/clean")
    public Result<Void> clean() {
        shoppingCartService.clean(BaseContext.getCurrentId());
        return Result.success();
    }
}

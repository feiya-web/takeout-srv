package com.takeout.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.context.BaseContext;
import com.takeout.common.result.Result;
import com.takeout.pojo.dto.OrdersSubmitDTO;
import com.takeout.pojo.vo.OrderSubmitVO;
import com.takeout.pojo.vo.OrderVO;
import com.takeout.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.takeout.annotation.AutoLog;

import javax.validation.Valid;

/**
 * 用户端 - 订单：下单 / 模拟支付 / 取消 / 历史订单 / 详情
 */
@RestController
@RequestMapping("/user/order")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 下单（@Transactional + Redis 幂等防重）
     */
    @AutoLog(module = "订单模块", type = "用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody @Valid OrdersSubmitDTO dto) {
        return Result.success(orderService.submit(BaseContext.getCurrentId(), dto));
    }

    /**
     * 模拟支付成功（状态机：待付款 -> 待接单）
     */
    @AutoLog(module = "订单模块", type = "用户支付")
    @PutMapping("/payment/{orderNumber}")
    public Result<Void> paySuccess(@PathVariable String orderNumber) {
        orderService.paySuccess(orderNumber, BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 取消订单（状态机：仅待付款/待接单可取消）
     */
    @AutoLog(module = "订单模块", type = "用户取消")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 历史订单分页
     */
    @GetMapping("/history")
    public Result<Page<OrderVO>> history(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         Integer status) {
        return Result.success(orderService.userPage(BaseContext.getCurrentId(), page, pageSize, status));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        return Result.success(orderService.details(id, BaseContext.getCurrentId()));
    }
}

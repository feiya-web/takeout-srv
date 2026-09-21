package com.takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.MessageConstant;
import com.takeout.common.exception.BaseException;
import com.takeout.common.exception.ParameterException;
import com.takeout.mapper.OrderDetailMapper;
import com.takeout.mapper.OrdersMapper;
import com.takeout.mapper.ShoppingCartMapper;
import com.takeout.order.OrderEvent;
import com.takeout.order.OrderStateMachine;
import com.takeout.pojo.dto.OrdersPageQueryDTO;
import com.takeout.pojo.dto.OrdersSubmitDTO;
import com.takeout.pojo.entity.OrderDetail;
import com.takeout.pojo.entity.Orders;
import com.takeout.pojo.entity.ShoppingCart;
import com.takeout.pojo.vo.OrderSubmitVO;
import com.takeout.pojo.vo.OrderVO;
import com.takeout.pojo.vo.OrderStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务：
 * 1. 下单 @Transactional 保证 orders / order_detail / shopping_cart 多表一致
 * 2. Redis SETNX 幂等锁防止重复提交
 * 3. 所有状态流转经由 OrderStateMachine 校验
 */
@Slf4j
@Service
public class OrderService {

    /** 下单幂等锁 key 前缀（5 秒防重） */
    private static final String SUBMIT_LOCK_PREFIX = "order:submit:lock:";

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderStateMachine orderStateMachine;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户下单（事务 + 幂等）
     */
    @Transactional
    public OrderSubmitVO submit(Long userId, OrdersSubmitDTO dto) {
        // 幂等防重：同一用户 5 秒内只允许一次下单请求
        String lockKey = SUBMIT_LOCK_PREFIX + userId;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5));
        if (!Boolean.TRUE.equals(locked)) {
            throw new BaseException(MessageConstant.ORDER_DUPLICATE_SUBMIT);
        }
        try {
            // 只采信当前用户购物车中真实存在的条目，金额由服务端计算
            List<ShoppingCart> cartItems = dto.getCartItemIds().isEmpty() ? List.of()
                    : shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                    .in(ShoppingCart::getId, dto.getCartItemIds())
                    .eq(ShoppingCart::getUserId, userId));
            if (CollectionUtils.isEmpty(cartItems)) {
                throw new BaseException(MessageConstant.CART_EMPTY);
            }
            BigDecimal amount = cartItems.stream()
                    .map(c -> c.getAmount().multiply(BigDecimal.valueOf(c.getNumber())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 订单主表
            Orders orders = new Orders();
            orders.setNumber(generateOrderNumber(userId));
            orders.setUserId(userId);
            orders.setConsignee(dto.getConsignee());
            orders.setPhone(dto.getPhone());
            orders.setAddress(dto.getAddress());
            orders.setRemark(dto.getRemark());
            orders.setAmount(amount);
            orders.setPayMethod(dto.getPayMethod());
            orders.setPayStatus(com.takeout.common.constant.StatusConstant.PAY_UNPAID);
            orders.setStatus(Orders.PENDING_PAYMENT);
            orders.setOrderTime(LocalDateTime.now());
            ordersMapper.insert(orders);

            // 订单明细（购物车快照）
            for (ShoppingCart cart : cartItems) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(orders.getId());
                detail.setDishId(cart.getDishId());
                detail.setSetmealId(cart.getSetmealId());
                detail.setName(cart.getName());
                detail.setImage(cart.getImage());
                detail.setAmount(cart.getAmount());
                detail.setNumber(cart.getNumber());
                orderDetailMapper.insert(detail);
            }

            // 清除已下单的购物车条目
            shoppingCartMapper.deleteBatchIds(dto.getCartItemIds());

            return new OrderSubmitVO(orders.getId(), orders.getNumber(), amount, orders.getStatus());
        } finally {
            // 无论成功失败都释放防重锁（正常重试不受影响）
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 模拟支付成功：待付款 -> 待接单
     */
    public void paySuccess(String orderNumber, Long userId) {
        Orders orders = getByNumberAndUser(orderNumber, userId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.PAY));
        orders.setPayStatus(com.takeout.common.constant.StatusConstant.PAY_PAID);
        orders.setCheckoutTime(LocalDateTime.now());
        ordersMapper.updateById(orders);
    }

    /**
     * 用户取消订单：仅待付款/待接单可取消
     */
    public void cancel(Long orderId, Long userId) {
        Orders orders = getByIdAndUser(orderId, userId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.CANCEL));
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(orders);
    }

    /**
     * 商家接单：待接单 -> 已接单
     */
    public void accept(Long orderId) {
        Orders orders = getById(orderId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.ACCEPT));
        ordersMapper.updateById(orders);
    }

    /**
     * 商家拒单：待接单 -> 已取消
     */
    public void reject(Long orderId, String reason) {
        Orders orders = getById(orderId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.REJECT));
        orders.setRejectionReason(reason);
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(orders);
    }

    /**
     * 开始派送：已接单 -> 派送中
     */
    public void delivery(Long orderId) {
        Orders orders = getById(orderId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.DELIVER));
        ordersMapper.updateById(orders);
    }

    /**
     * 完成订单：派送中 -> 已完成
     */
    public void complete(Long orderId) {
        Orders orders = getById(orderId);
        orders.setStatus(orderStateMachine.apply(orders.getStatus(), OrderEvent.COMPLETE));
        ordersMapper.updateById(orders);
    }

    /**
     * 用户历史订单分页（走联合索引 idx_user_order_time）
     */
    public Page<OrderVO> userPage(Long userId, int page, int pageSize, Integer status) {
        Page<Orders> ordersPage = ordersMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .eq(status != null, Orders::getStatus, status)
                        .orderByDesc(Orders::getOrderTime));
        return (Page<OrderVO>) ordersPage.convert(orders -> toVO(orders, false));
    }

    public OrderVO details(Long orderId, Long userId) {
        Orders orders = getById(orderId);
        // userId 为 null 表示管理端查询，跳过归属校验
        if (userId != null && !orders.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.NO_PERMISSION);
        }
        return toVO(orders, true);
    }

    /**
     * 管理端订单分页（走联合索引 idx_status_order_time）
     */
    public Page<Orders> adminPage(OrdersPageQueryDTO dto) {
        Page<Orders> page = new Page<>(dto.getPage(), dto.getPageSize());
        return ordersMapper.pageQuery(page, dto);
    }

    public OrderStatisticsVO statistics() {
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(countByStatus(Orders.TO_BE_ACCEPTED));
        vo.setConfirmed(countByStatus(Orders.ACCEPTED));
        vo.setDeliveryInProgress(countByStatus(Orders.DELIVERING));
        return vo;
    }

    private Long countByStatus(int status) {
        return ordersMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, status));
    }

    private Orders getById(Long orderId) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new ParameterException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
    }

    private Orders getByIdAndUser(Long orderId, Long userId) {
        Orders orders = getById(orderId);
        if (!orders.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.NO_PERMISSION);
        }
        return orders;
    }

    private Orders getByNumberAndUser(String number, Long userId) {
        Orders orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, number));
        if (orders == null) {
            throw new ParameterException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orders.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.NO_PERMISSION);
        }
        return orders;
    }

    private OrderVO toVO(Orders orders, boolean withDetails) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders, vo);
        if (withDetails) {
            vo.setOrderDetailList(orderDetailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId())));
        }
        return vo;
    }

    /**
     * 订单号：时间戳 + 用户id后四位 + 两位随机数
     */
    private String generateOrderNumber(Long userId) {
        return System.currentTimeMillis() + String.format("%04d", userId % 10000)
                + String.format("%02d", (int) (Math.random() * 100));
    }
}

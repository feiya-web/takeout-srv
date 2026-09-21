package com.takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.common.exception.ParameterException;
import com.takeout.mapper.DishMapper;
import com.takeout.mapper.SetmealMapper;
import com.takeout.mapper.ShoppingCartMapper;
import com.takeout.pojo.dto.ShoppingCartDTO;
import com.takeout.pojo.entity.Dish;
import com.takeout.pojo.entity.Setmeal;
import com.takeout.pojo.entity.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务：同一用户 + 同一商品 + 同一口味 视为同一条目，重复添加只累加数量
 */
@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public void add(Long userId, ShoppingCartDTO dto) {
        ShoppingCart existing = findOne(userId, dto);
        if (existing != null) {
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartMapper.updateById(existing);
            return;
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setDishFlavor(dto.getDishFlavor());
        if (dto.getDishId() != null) {
            Dish dish = dishMapper.selectById(dto.getDishId());
            if (dish == null) {
                throw new ParameterException("菜品不存在");
            }
            cart.setDishId(dto.getDishId());
            cart.setName(dish.getName());
            cart.setImage(dish.getImage());
            cart.setAmount(dish.getPrice());
        } else if (dto.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(dto.getSetmealId());
            if (setmeal == null) {
                throw new ParameterException("套餐不存在");
            }
            cart.setSetmealId(dto.getSetmealId());
            cart.setName(setmeal.getName());
            cart.setImage(setmeal.getImage());
            cart.setAmount(setmeal.getPrice());
        } else {
            throw new ParameterException("购物车参数错误");
        }
        cart.setNumber(1);
        shoppingCartMapper.insert(cart);
    }

    /**
     * 数量减一：减到 0 删除条目
     */
    public void sub(Long userId, ShoppingCartDTO dto) {
        ShoppingCart existing = findOne(userId, dto);
        if (existing == null) {
            throw new ParameterException("购物车中不存在该商品");
        }
        if (existing.getNumber() > 1) {
            existing.setNumber(existing.getNumber() - 1);
            shoppingCartMapper.updateById(existing);
        } else {
            shoppingCartMapper.deleteById(existing.getId());
        }
    }

    public List<ShoppingCart> list(Long userId) {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByAsc(ShoppingCart::getCreateTime));
    }

    public void clean(Long userId) {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));
    }

    private ShoppingCart findOne(Long userId, ShoppingCartDTO dto) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dto.getDishId() != null, ShoppingCart::getDishId, dto.getDishId())
                .eq(dto.getSetmealId() != null, ShoppingCart::getSetmealId, dto.getSetmealId())
                .eq(ShoppingCart::getDishFlavor, dto.getDishFlavor() == null ? "" : dto.getDishFlavor());
        return shoppingCartMapper.selectOne(wrapper);
    }
}

package com.takeout.common.constant;

/**
 * 提示信息常量
 */
public class MessageConstant {

    public static final String ALREADY_EXISTS = " 已存在";
    public static final String UNKNOWN_ERROR = "系统繁忙，请稍后再试";
    public static final String PASSWORD_ERROR = "用户名或密码错误";
    public static final String ACCOUNT_DISABLED = "账号已被禁用";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String CATEGORY_USED_BY_DISH = "当前分类下存在菜品，无法删除";
    public static final String CATEGORY_USED_BY_SETMEAL = "当前分类下存在套餐，无法删除";
    public static final String DISH_ON_SALE = "存在起售中的菜品，无法删除";
    public static final String DISH_IN_SETMEAL = "存在套餐关联的菜品，无法删除";
    public static final String SETMEAL_ON_SALE = "存在起售中的套餐，无法删除";
    public static final String SETMEAL_CONTAINS_DISABLE_DISH = "套餐内包含停售菜品，无法起售";
    public static final String CART_EMPTY = "购物车为空，不能下单";
    public static final String ORDER_DUPLICATE_SUBMIT = "订单提交中，请勿重复点击";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String EMPLOYEE_NOT_FOUND = "员工不存在";
    public static final String CANNOT_DISABLE_SELF = "不能禁用自己";
    public static final String STATUS_INVALID = "状态值不合法，只允许 0(禁用) 或 1(启用)";
    public static final String NO_PERMISSION = "无权限操作该订单";
    public static final String OPERATION_FAILED = "操作失败";
}

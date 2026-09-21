package com.takeout.order;

/**
 * 订单事件
 */
public enum OrderEvent {
    /** 用户支付（模拟） */
    PAY,
    /** 商家接单 */
    ACCEPT,
    /** 商家拒单 */
    REJECT,
    /** 开始派送 */
    DELIVER,
    /** 完成 */
    COMPLETE,
    /** 用户取消 */
    CANCEL
}

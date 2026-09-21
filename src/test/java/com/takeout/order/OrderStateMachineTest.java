package com.takeout.order;

import com.takeout.common.exception.OrderStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.takeout.pojo.entity.Orders.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态机单元测试（对应异常用例「状态流转」组）
 */
class OrderStateMachineTest {

    private final OrderStateMachine machine = new OrderStateMachine();

    @Test
    @DisplayName("合法流转：待付款 -> 支付 -> 待接单")
    void payFromPendingPayment() {
        assertEquals(TO_BE_ACCEPTED, machine.apply(PENDING_PAYMENT, OrderEvent.PAY));
    }

    @Test
    @DisplayName("合法流转：待接单 -> 接单 -> 已接单")
    void acceptFromToBeAccepted() {
        assertEquals(ACCEPTED, machine.apply(TO_BE_ACCEPTED, OrderEvent.ACCEPT));
    }

    @Test
    @DisplayName("合法流转：待接单 -> 拒单 -> 已取消")
    void rejectFromToBeAccepted() {
        assertEquals(CANCELLED, machine.apply(TO_BE_ACCEPTED, OrderEvent.REJECT));
    }

    @Test
    @DisplayName("合法流转：已接单 -> 派送 -> 派送中")
    void deliverFromAccepted() {
        assertEquals(DELIVERING, machine.apply(ACCEPTED, OrderEvent.DELIVER));
    }

    @Test
    @DisplayName("合法流转：派送中 -> 完成 -> 已完成")
    void completeFromDelivering() {
        assertEquals(COMPLETED, machine.apply(DELIVERING, OrderEvent.COMPLETE));
    }

    @Test
    @DisplayName("合法流转：待付款 -> 取消 -> 已取消")
    void cancelFromPendingPayment() {
        assertEquals(CANCELLED, machine.apply(PENDING_PAYMENT, OrderEvent.CANCEL));
    }

    @Test
    @DisplayName("合法流转：待接单 -> 取消 -> 已取消")
    void cancelFromToBeAccepted() {
        assertEquals(CANCELLED, machine.apply(TO_BE_ACCEPTED, OrderEvent.CANCEL));
    }

    @Test
    @DisplayName("非法流转：待付款不允许直接接单")
    void acceptFromPendingPaymentShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(PENDING_PAYMENT, OrderEvent.ACCEPT));
    }

    @Test
    @DisplayName("非法流转：已接单不允许取消（越权流转被拦截）")
    void cancelFromAcceptedShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(ACCEPTED, OrderEvent.CANCEL));
    }

    @Test
    @DisplayName("非法流转：派送中不允许取消")
    void cancelFromDeliveringShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(DELIVERING, OrderEvent.CANCEL));
    }

    @Test
    @DisplayName("非法流转：已完成不允许拒单")
    void rejectFromCompletedShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(COMPLETED, OrderEvent.REJECT));
    }

    @Test
    @DisplayName("非法流转：已取消不允许支付")
    void payFromCancelledShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(CANCELLED, OrderEvent.PAY));
    }

    @Test
    @DisplayName("非法流转：待付款不允许直接派送")
    void deliverFromPendingPaymentShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(PENDING_PAYMENT, OrderEvent.DELIVER));
    }

    @Test
    @DisplayName("非法流转：待付款不允许完成")
    void completeFromPendingPaymentShouldThrow() {
        assertThrows(OrderStatusException.class,
                () -> machine.apply(PENDING_PAYMENT, OrderEvent.COMPLETE));
    }
}

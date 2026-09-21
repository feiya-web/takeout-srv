package com.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.pojo.entity.Orders;
import com.takeout.pojo.dto.OrdersPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 管理端订单分页（条件：订单号/状态/下单日期区间，走 idx_status_order_time 联合索引）
     */
    Page<Orders> pageQuery(Page<Orders> page, @Param("q") OrdersPageQueryDTO query);
}

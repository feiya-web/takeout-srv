package com.takeout.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐 DTO（新增/修改，含菜品明细）
 */
@Data
public class SetmealDTO {

    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private List<com.takeout.pojo.entity.SetmealDish> setmealDishes;
}

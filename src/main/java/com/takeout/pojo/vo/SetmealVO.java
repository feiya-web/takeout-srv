package com.takeout.pojo.vo;

import com.takeout.pojo.entity.SetmealDish;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐 VO
 */
@Data
public class SetmealVO {

    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
    private String categoryName;
    private List<SetmealDish> setmealDishes;
}

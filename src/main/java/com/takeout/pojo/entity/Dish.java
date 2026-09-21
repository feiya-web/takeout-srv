package com.takeout.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
public class Dish implements Serializable {

    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    /** 状态 0停售 1起售 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

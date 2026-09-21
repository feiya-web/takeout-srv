package com.takeout.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品 DTO（新增/修改，含口味）
 */
@Data
public class DishDTO {

    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private java.util.List<com.takeout.pojo.entity.DishFlavor> flavors;
}

package com.takeout.pojo.vo;

import com.takeout.pojo.entity.DishFlavor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品 VO
 */
@Data
public class DishVO {

    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private LocalDateTime updateTime;
    private String categoryName;
    private List<DishFlavor> flavors;
}

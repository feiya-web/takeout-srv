package com.takeout.pojo.dto;

import lombok.Data;

/**
 * 菜品分页查询 DTO
 */
@Data
public class DishPageQueryDTO {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;
    private Long categoryId;
    private Integer status;
}

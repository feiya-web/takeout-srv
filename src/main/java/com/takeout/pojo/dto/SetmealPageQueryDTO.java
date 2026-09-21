package com.takeout.pojo.dto;

import lombok.Data;

/**
 * 套餐分页查询 DTO
 */
@Data
public class SetmealPageQueryDTO {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;
    private Long categoryId;
    private Integer status;
}

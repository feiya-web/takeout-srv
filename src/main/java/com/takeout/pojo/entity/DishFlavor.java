package com.takeout.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜品口味
 */
@Data
public class DishFlavor implements Serializable {

    private Long id;
    private Long dishId;
    private String name;
    private String value;
}

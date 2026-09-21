package com.takeout.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类
 */
@Data
public class Category implements Serializable {

    private Long id;
    private String name;
    /** 类型 1菜品分类 2套餐分类 */
    private Integer type;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

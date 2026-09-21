package com.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.pojo.entity.Dish;
import com.takeout.pojo.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 分页查询：dish 关联 category 取分类名（管理端）
     */
    Page<DishVO> pageQuery(Page<DishVO> page, @Param("name") String name,
                           @Param("categoryId") Long categoryId, @Param("status") Integer status);
}

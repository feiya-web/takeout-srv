package com.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.pojo.entity.Setmeal;
import com.takeout.pojo.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    Page<SetmealVO> pageQuery(Page<SetmealVO> page, @Param("name") String name,
                              @Param("categoryId") Long categoryId, @Param("status") Integer status);
}

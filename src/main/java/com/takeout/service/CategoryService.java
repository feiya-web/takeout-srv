package com.takeout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.MessageConstant;
import com.takeout.common.exception.DeletionNotAllowedException;
import com.takeout.mapper.CategoryMapper;
import com.takeout.mapper.DishMapper;
import com.takeout.mapper.SetmealMapper;
import com.takeout.pojo.dto.CategoryPageQueryDTO;
import com.takeout.pojo.entity.Category;
import com.takeout.pojo.entity.Dish;
import com.takeout.pojo.entity.Setmeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 分类服务
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public void save(Category category) {
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类：分类下有菜品或套餐时禁止删除
     */
    public void deleteById(Long id) {
        Long dishCount = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (dishCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_USED_BY_DISH);
        }
        Long setmealCount = setmealMapper.selectCount(
                new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
        if (setmealCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_USED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
    }

    public Page<Category> page(CategoryPageQueryDTO dto) {
        Page<Category> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .like(StringUtils.hasText(dto.getName()), Category::getName, dto.getName())
                .eq(dto.getType() != null, Category::getType, dto.getType())
                .orderByAsc(Category::getSort);
        return categoryMapper.selectPage(page, wrapper);
    }

    public List<Category> list(Integer type) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(type != null, Category::getType, type)
                        .orderByAsc(Category::getSort));
    }
}

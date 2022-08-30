package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    //新增套餐，以及新增套餐和菜品关系表
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐关联数据
    void removeWithDish(List<Long> ids);
}

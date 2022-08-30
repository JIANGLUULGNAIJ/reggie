package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;



    //新增套餐，以及新增套餐和菜品关系表
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐表setmeal，套餐唯一，只有一个ID
        this.save(setmealDto);
        //然后存套餐和菜品关系表setmeal_dish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) ->{//遍历
            item.setSetmealId(setmealDto.getId());//为每个菜品添加套餐ID
            return item;
        }).collect(Collectors.toList());//收集起来返回集合

        setmealDishService.saveBatch(setmealDishes);

    }


    //删除套餐，同时删除套餐关联数据
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {

        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);//根据id查数据
        queryWrapper.eq(Setmeal::getStatus,1);//且必须是起售状态
        int count = this.count(queryWrapper);//根据这些条件查询到条数

        //如果不能删除，抛出业务异常
        if (count>0){
            throw new CustomException("套餐正在售卖中。");//这是我们的自定义异常
        }

        //如果可以删除，先删除套餐表数据
        this.removeByIds(ids);//批量删除

        //再删除关系表中的数据setmeal_dish
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);//删除跟套餐ID相关的菜品

        setmealDishService.remove(queryWrapper1);//批量删除
    }
}

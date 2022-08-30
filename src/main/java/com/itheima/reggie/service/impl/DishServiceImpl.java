package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMaper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMaper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional//事务注解
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);//因为DishDto继承自Dish,所以可以直接保存

        Long id = dishDto.getId();//菜品ID

        //将菜品ID挨个封装到口味对象中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {//使用stream流来遍历口味对象并封装菜品id
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到口味表dish_flavor
        dishFlavorService.saveBatch(flavors);//批量保存
    }



    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        //查询菜品基本信息
        Dish dish = this.getById(id);

        //对象拷贝
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);//根据queryWrapper条件查出结果封装成集合

        dishDto.setFlavors(flavors);//给拷贝对象设置口味

        return dishDto;
    }


    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @Override
    @Transactional//多表操作要加事务，保证一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表
        this.updateById(dishDto);
        //清理当前对应口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();//虽然拿到口味了，但是不能直接存，因为没有id
        flavors = flavors.stream().map((item) -> {//使用stream流来遍历口味对象并封装菜品id
            item.setDishId(dishDto.getId());//将那个口味设置id
            return item;
        }).collect(Collectors.toList());
        //更新口味表
        dishFlavorService.saveBatch(flavors);//批量保存
    }
}

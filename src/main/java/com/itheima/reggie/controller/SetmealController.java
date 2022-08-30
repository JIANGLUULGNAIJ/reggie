package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.impl.SetmealServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController{

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    //保存套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("保存成功");
    }


    //套餐分页功能
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){//分页查询返回Page对象不需要返回json格式

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);//这个查出来除了套餐分类那个无法显示，其他都是好的
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(name!=null,Setmeal::getName,name);//等值查询
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);//这个查出来除了套餐分类那个无法显示，其他都是好的

        //处理套餐分类
        Page<SetmealDto> page1 = new Page<>();
        //拷贝原来分页的信息,但是不要数据对象setmeal
        BeanUtils.copyProperties(pageInfo,page1,"records");

        //我们自己封装数据对象，封装成setmealDto就可以符合页面显示要求
        List<Setmeal> records = pageInfo.getRecords();//拿到数据对象setmeal们
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);//拷贝

            //给setmealDto设置categoryName
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());//设置categoryName
            return setmealDto;
        }).collect(Collectors.toList());//到这里，records就可以符合要求了，里面的数据对象都是setmealDto，被我们处理过的

        page1.setRecords(list);

        return R.success(page1);
    }

    //删除套餐
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功！");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){//传递key-value时，不需要@RequestBody

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

}

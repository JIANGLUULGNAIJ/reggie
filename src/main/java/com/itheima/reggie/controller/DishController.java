package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);


        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //name不为空时，添加这个过滤条件
        queryWrapper.like(name!=null,Dish::getName,name);
        //根据更新时间降序排列
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //page对象拷贝
        //records是列表数据集合，我们自己处理，因为原来的records中没有categoryName
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        /**
         * 遍历Dish中的records，根据categoryId，调用categoryService.getById方法来获取对象，然后对象点出分类名称
         * 再将分类名称设置到dishDto中，返回，把每个dishDto收集起来，变成一个集合
         */
        List<DishDto> list = records.stream().map((item) -> {//一行对象一个item
            DishDto dishDto = new DishDto();

            //将list中常规数据拷贝到dishDto中，只有categoryName还没有这个时候
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//拿到分类ID
            //根据ID查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!= null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());//将dishDto收集起来变成集合给到list

        dishDtoPage.setRecords(list);//设置records

        return R.success(dishDtoPage);
    }

    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        //要查两张表
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }



    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        //涉及两张表的更新
        dishService.updateWithFlavor(dishDto);

        //清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("更新菜品成功");
    }

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());//等值查询
//        queryWrapper.eq(Dish::getStatus,1);//状态是起售状态
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList =null;

        //动态构造key
        String key = "dish" + "_" +dish.getCategoryId() + "_" +dish.getStatus();

        //根据key查询缓存
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，返回数据
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());//等值查询
        queryWrapper.eq(Dish::getStatus,1);//状态是起售状态
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);


        dishDtoList = list.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);//把原来的拷贝过来

            //添加口味
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(queryWrapper1);

            //设置进去
            dishDto.setFlavors(flavors);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，根据数据库查询，然后存入redis缓存
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}

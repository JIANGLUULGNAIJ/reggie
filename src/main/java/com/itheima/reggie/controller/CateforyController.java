package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CateforyController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("保存成功！");
    }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){

        Page<Category> pageIngo = new Page<>(page,pageSize);//分页构造器

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();//条件构造器

        queryWrapper.orderByAsc(Category::getSort);//添加排序条件，根据sort进行排序

        categoryService.page(pageIngo,queryWrapper);//进行分页查询

        return R.success(pageIngo);
    }


    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类，id为：{}",ids);

//        categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功！");
    }

    /**
     * 修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类成功。");
    }


    /**
     * 根据条件来查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件,两种排序策略
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);


        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}



























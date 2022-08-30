package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {

    //new一个对象，线程隔离
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //工具方法都设置成静态
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurentId(){
        return threadLocal.get();
    }

}

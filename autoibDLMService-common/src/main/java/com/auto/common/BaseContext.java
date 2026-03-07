package com.auto.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id以及后面构建网络数据的TaskId
 */
public class BaseContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){
        USER_ID.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return USER_ID.get();
    }


    public static void remove(){
        USER_ID.remove();
    }
}
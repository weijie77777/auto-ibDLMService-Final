package com.auto.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {
    public CustomException(){
        super();
    }
    public CustomException(String message){
        super(message);
    }
}

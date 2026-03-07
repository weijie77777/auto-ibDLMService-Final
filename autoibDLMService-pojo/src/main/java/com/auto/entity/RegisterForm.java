package com.auto.entity;

import lombok.Data;

@Data
public class RegisterForm {

    private String username;

    private String password;

    private String phone;

    private String sex;
    private String checkCode;
}

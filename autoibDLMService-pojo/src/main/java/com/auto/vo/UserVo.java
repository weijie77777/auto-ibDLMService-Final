package com.auto.vo;

import lombok.Data;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String phone;
    private String sex;
    private String token;
}

package com.auto.service;

import com.auto.common.R;
import com.auto.entity.LoginForm;
import com.auto.entity.User;
import com.auto.vo.UserVo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;


public interface UserService extends IService<User> {
   R<UserVo> login(HttpServletRequest request, LoginForm loginForm);
   R<String> saveUser(User user);
}

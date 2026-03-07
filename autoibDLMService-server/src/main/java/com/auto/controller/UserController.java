package com.auto.controller;

import com.auto.common.R;
import com.auto.constant.MessageConstant;
import com.auto.entity.LoginForm;
import com.auto.entity.RegisterForm;
import com.auto.entity.User;
import com.auto.service.UserService;
import com.auto.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.time.LocalDateTime;

@Slf4j
//日志注解
@RestController
//Rest风格
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public R<UserVo> login(HttpServletRequest request, @RequestBody LoginForm loginForm){
        log.info("当前登录的用户:{}", loginForm);
        return userService.login(request, loginForm);
    }

    /*
    用户退出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //在用户退出登录后，清除上传的数据集
        return R.success("退出成功");
    }
    //用户注册 User user
    @PostMapping("/sign")
    public R<String> save (HttpServletRequest request, @RequestBody RegisterForm registerForm){
        User user = User.builder().username(registerForm.getUsername())
                .phone(registerForm.getPhone())
                .sex(registerForm.getSex())
                .password(registerForm.getPassword())
                .build();
        log.info("用户注册: {}",user.toString());
        //获取当前存储的验证码
        HttpSession session=request.getSession();
        String checkCodeGen=(String) session.getAttribute("checkCodeGen");
        //获取用户输入的验证码
        String checkCode1=registerForm.getCheckCode();
        if(!checkCode1.equals(checkCodeGen)){
            return R.error(MessageConstant.CODE_ERROR);
        }
        //调用用户保存方法
        return userService.saveUser(user);
    }
}

package com.auto.service.impl;

import com.auto.common.R;
import com.auto.constant.JwtClaimsConstant;
import com.auto.constant.MessageConstant;
import com.auto.entity.LoginForm;
import com.auto.entity.User;
import com.auto.mapper.UserMapper;
import com.auto.properties.JWTProperties;
import com.auto.service.UserService;
import com.auto.util.JwtUtil;
import com.auto.util.RegexUtils;
import com.auto.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;

//直接使用mybatisplus提供的使用规范
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JWTProperties jwtProperties;
    @Override
    public R<UserVo> login(HttpServletRequest request, LoginForm loginForm) {
        User user = User.builder()
                .username(loginForm.getUsername())
                .password(loginForm.getPassword())
                .build();
        //1、将页面提交的密码password进行MD5加密处理
        String password= user.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交用户名username查询数据库
        LambdaQueryWrapper<User> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User emp= this.getOne(queryWrapper);
        //获取用户输入的验证码
        String checkCode1=loginForm.getCheckCode();
        log.info("用户输入的验证码: {}", checkCode1);
        log.info("用户名: {}", loginForm.getUsername());
        //获取当前存储的验证码
        HttpSession session=request.getSession();
        String checkCodeGen=(String) session.getAttribute("checkCodeGen");
        log.info("存储的验证码: {}", checkCodeGen);
        //3、如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error(MessageConstant.PASSWORD_ERROR);
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==MessageConstant.Status_FAIL){
            return R.error(MessageConstant.ACCOUNT_LOCKED);
        }
       // 验证码比对
        if(!checkCode1.equals(checkCodeGen)){
            return R.error(MessageConstant.CODE_ERROR);
        }
       //6、登录成功，将员工信息封装到UserVo中返回给前端
        UserVo userVo=new UserVo();
        BeanUtils.copyProperties(emp, userVo);
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userVo.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        userVo.setToken(token);
        return R.success(userVo);
    }

    @Override
    public R<String> saveUser(User user) {
        String password= user.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        // 1. 查看用户名是否已注册
        LambdaQueryWrapper<User> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User queryUser= this.getOne(queryWrapper);
        if(queryUser!=null){
            return R.error(MessageConstant.ALREADY_EXISTS);
        }
        // 2. 判断手机号格式是否正确
        if(RegexUtils.isPhoneInvalid(user.getPhone())){
            return R.error(MessageConstant.PHONE_ERROR);
        }
        // 3. 判断手机号是否已经注册过
        LambdaQueryWrapper<User> queryWrapper1 =new LambdaQueryWrapper<>();
        queryWrapper1.eq(User::getPhone, user.getPhone());
        User queryUser1= this.getOne(queryWrapper1);
        if(queryUser1!=null){
            return R.error(MessageConstant.PHONE_REGISTERED);
        }
        //4. 调用用户保存方法
        this.save(user);
        return R.success("新增员工成功");
    }
}

package com.auto.controller;

//import com.example.NewProject.common.CommonUtil;
//import com.example.NewProject.common.R;

import com.auto.constant.CodeImageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

@Slf4j
//日志注解
@RestController
//Rest风格
@RequestMapping("/front/codeImage")
public class FrontCodeImageController {
    // 基础字符内容
    private String words = CodeImageConstant.CODE_BASIC_WORDS;
    // 验证码模块的长宽
    private int width = CodeImageConstant.CODE_WIDTH;
    private int height = CodeImageConstant.CODE_HEIGHT;
    // 验证码的字符个数
    private int count = CodeImageConstant.CODE_COUNT;
    // 设置干扰点的数量
    private int points = CodeImageConstant.CODE_POINTS;
    private Random r =new Random();

    /**
     * 获取验证码 并将其以输出流的形式返回到前端
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/get")
    public void getCode(HttpServletRequest request,HttpServletResponse response)throws IOException {
        //验证码的绘制
        //创建画板
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        // 获取笔触
        Graphics gs = image.getGraphics();
        //设置笔触颜色
        gs.setColor(Color.WHITE);
        //fillRect 填充一个矩形
        gs.fillRect(0,0,width,height);
        //设置绘制的字体
        gs.setFont(new Font("宋体",Font.BOLD + Font.ITALIC,40));
        //获取字符中words中字符的个数
        int length = words.length();
        //记录生成的code
        StringBuffer buffer=new StringBuffer();
        for(int i=0;i<count;i++){
            //绘制文字
            //随机颜色
            gs.setColor(new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256)));
            //随机0~words的length 的值，取不到最大值
            int index=r.nextInt(length);
            char c = words.charAt(index);
            buffer.append(c);
            //绘制该字符
            gs.drawString(String.valueOf(c),10 + (i * 20),30);
        }
        //生成的验证码
        String code=buffer.toString();
        //存储生成的验证码 用session存储不能实现负载均衡 当后台有多台服务器时 session间的数据不能实现共享
        HttpSession session=request.getSession();
        session.setAttribute("checkCodeGen",code);
        log.info("生成的code: {}",session.getAttribute("checkCodeGen"));
        System.out.println("生成验证码sessionId: "+session.getId());
        //绘制干扰点
        gs.setColor(Color.LIGHT_GRAY);
        for(int i=0;i<points;i++){
            gs.drawOval(r.nextInt(width),r.nextInt(height),1,1);
        }
        //获取响应流
       ServletOutputStream os= response.getOutputStream();
        //输出图片到客户端
        ImageIO.write(image,"png",os);
    }

}

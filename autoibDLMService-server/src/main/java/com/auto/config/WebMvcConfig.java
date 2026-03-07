package com.auto.config;

import com.auto.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtTokenUserInterceptor)
                .excludePathPatterns(
                        "/user/**",
                        "/front/**",
                        "/developers/**",
                        "/displayout/**",
                        "/searchByTitle/**");
    }
    @Bean
    public RestTemplate restTemplate() {
        // 基础配置
        return new RestTemplate();
    }
}

package com.roger.urbanlifestyle.config;

import com.roger.urbanlifestyle.utils.LoginIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO:这里给upload放行只是为了测试，之后要删除
        registry.addInterceptor(new LoginIntercepter()).excludePathPatterns("/user/login", "/user/code", "/blog/hot", "/shop/**", "/shop-type/**", "/upload/**", "/voucher/**");

    }
}

package com.heima.user.config;

import com.heima.user.inteceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class UseridConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String>exs=new ArrayList<>();
        exs.add("/user/login_auth");
        exs.add("/user/register");
        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/**").excludePathPatterns(exs);
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
package com.heima.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.blog.mapper.BlogUserMapper;
import com.heima.blog.service.BlogService;
import com.heima.blog.service.BlogUserService;
import com.heima.model.blog.pojos.OjBlogUser;
import org.springframework.stereotype.Service;

@Service
public class BlogUserServiceImpl extends ServiceImpl<BlogUserMapper, OjBlogUser> implements BlogUserService {
}

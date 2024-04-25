package com.heima.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.blog.pojos.OjBlogUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogUserMapper extends BaseMapper<OjBlogUser> {
}

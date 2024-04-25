package com.heima.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.blog.pojos.OjBlogList;
import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.PageRequestDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.ques.pojos.OjQuestion;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService extends IService<OjBlogList> {

    ResponseResult bl(String type,Integer p,Integer ps);

    ResponseResult bo(Long id);

    ResponseResult hl(Bloglistwithstate dto);

    ResponseResult hs(Bloglistwithstate dto);

    ResponseResult upl(MultipartFile file);

    ResponseResult sub(Bloglistwithstate blog);

    ResponseResult bal(Integer p,Integer ps);

    ResponseResult dt();
}

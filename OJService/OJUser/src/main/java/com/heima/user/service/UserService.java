package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.OjUser;
import org.apache.ibatis.annotations.Select;

public interface UserService extends IService<OjUser> {

    public ResponseResult  login(LoginDto dto);

    ResponseResult reg(LoginDto dto);

    ResponseResult upi(LoginDto dto);//更新信息

    ResponseResult gu();

    OjUser getu(Long id);  //根据id获取任何人

    ResponseResult hf(Bloglistwithstate dto);//处理关注关系

    ResponseResult cf(Long id);
}

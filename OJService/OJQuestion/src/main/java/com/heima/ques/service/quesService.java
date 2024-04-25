package com.heima.ques.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.*;
import com.heima.model.ques.pojos.OjQuestion;
import com.heima.model.ques.pojos.OjSolu;

public interface quesService extends IService<OjQuestion> {
    public ResponseResult ql(PageRequestDto dto);

    ResponseResult qo(Long id);

    ResponseResult cc(CodeDto cd);

    ResponseResult rc(Rquerydto rd);

    ResponseResult dp(String d);

    ResponseResult ft(Long uid);

    ResponseResult ss(OjSolu ojSolu);

    ResponseResult sl(Long id);

    ResponseResult prc(Rquerydto d);

    ResponseResult sn(Long id);

    ResponseResult addl(Long id);
}

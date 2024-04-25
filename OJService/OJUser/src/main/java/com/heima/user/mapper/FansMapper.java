package com.heima.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.blog.pojos.OjFans;
import com.heima.model.common.dtos.ResponseResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FansMapper extends BaseMapper<OjFans> {

    @Select("select distinct a.uid from oj_fans as a join oj_fans as b on a.uid=b.uid " +
            "where a.fid=#{me} and b.fid=#{id}")
    List<Long> cf(Long id, Long me);
}

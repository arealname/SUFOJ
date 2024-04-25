package com.heima.ques.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.dtos.SoluNumsDto;
import com.heima.model.ques.pojos.OjQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuesMapper extends BaseMapper<OjQuestion> {
    @Select("select id from oj_question")
    public List<Long> allids();

    @Select("SELECT\n" +
            "  COUNT(CASE WHEN difficulty = '简单' THEN 1 END) AS easynums,\n" +
            "  COUNT(CASE WHEN difficulty = '中等' THEN 1 END) AS midnums,\n" +
            "  COUNT(*) - COUNT(CASE WHEN difficulty = '简单' THEN 1 END) - COUNT(CASE WHEN difficulty = '中等' THEN 1 END) AS diffnums\n" +
            "FROM \n" +
            "oj_user_ques join ojdb.oj_question oq on oj_user_ques.qid = oq.id and uid=#{uid};")
    SoluNumsDto getnums(Long uid);

    @Select("select \n"+
            "  COUNT(CASE WHEN difficulty = '简单' THEN 1 END) AS easynums,\n" +
            "  COUNT(CASE WHEN difficulty = '中等' THEN 1 END) AS midnums,\n" +
            "  COUNT(*) - COUNT(CASE WHEN difficulty = '简单' THEN 1 END) - COUNT(CASE WHEN difficulty = '中等' THEN 1 END) AS diffnums\n" +
            "FROM \n" +
    "oj_question")
    SoluNumsDto gettotalnums();

    @Update("update oj_solu set ups=ups+1 where id=#{id}")
    void addl(Long id);
}

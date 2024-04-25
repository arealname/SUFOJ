package com.heima.ques.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.api.UserClient;
import com.heima.model.common.dtos.*;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.ques.pojos.*;
import com.heima.model.submit.pojos.OjSubmitRecord;
import com.heima.model.user.pojos.OjUser;
import com.heima.ques.mapper.*;
import com.heima.ques.service.quesService;
import com.heima.utils.common.UserContext;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class qsServiceImpl extends ServiceImpl<QuesMapper, OjQuestion> implements quesService {

    private static final String OJ_TEST_EXCHANGE = "oj.test.exchange";
    private static final String OJ_TEST_KEY = "oj.test.key";
    private static final String PRO_PREFIX = "Dayproblem:";
    @Autowired
    private SampleMapper sampleMapper;

    @Autowired
    private QuesUserMapper quesUserMapper;

    @Override
    public ResponseResult ql(PageRequestDto dto) {

        Long user = UserContext.getUser();

        System.out.println(user);
        List<Long> collect = new ArrayList<>();

        if (user != null) {
            LambdaQueryWrapper<OjUserQues> l1 = new LambdaQueryWrapper<>();

            l1.eq(user != null, OjUserQues::getUid, user);

            collect = quesUserMapper.selectList(l1).stream().map(i -> {
                return i.getQid();
            }).collect(Collectors.toList());
        }


        String w = dto.getW();

        LambdaQueryWrapper<OjQuestion> l = new LambdaQueryWrapper<>();

        l.like(StringUtils.isNotBlank(w), OjQuestion::getQuestionName, w);

        Page<OjQuestion> p = new Page<>(dto.getP(), dto.getPs());


        Page<OjQuestion> page = page(p, l);
        Page<queswithsamples> pp = new Page<>();


        BeanUtils.copyProperties(page, pp, "records");

        List<OjQuestion> records = page.getRecords();

        List<queswithsamples> re = new ArrayList<>();

        for (OjQuestion q : records) {
            queswithsamples qw = new queswithsamples();

            BeanUtils.copyProperties(q, qw);

            LambdaQueryWrapper<OjTestSamples> ll = new LambdaQueryWrapper<>();
            ll.eq(OjTestSamples::getQid, q.getId());

            List<OjTestSamples> testSamples = sampleMapper.selectList(ll);

            qw.setTestSamples(testSamples);
            qw.setIspass(collect.contains(q.getId()));
            re.add(qw);
        }

        pp.setRecords(re);

        ResponseResult responseResult = PageResponseResult.okResult(pp);

        return responseResult;
    }

    @Override
    public ResponseResult qo(Long id) {


        Long user = UserContext.getUser();

        LambdaQueryWrapper<OjUserQues> l1 = new LambdaQueryWrapper<>();
        l1.eq(user != null, OjUserQues::getUid, user);

        List<Long> collect = quesUserMapper.selectList(l1).stream().map(i -> {
            return i.getQid();
        }).collect(Collectors.toList());


        OjQuestion q = null;

        //先查看缓存中有没有
        String s = stringRedisTemplate.opsForValue().get(PRO_PREFIX + id);

        //没有的话，查询数据库
        if (s == null) {
            System.out.println("缓存中暂时没有");

            LambdaQueryWrapper<OjQuestion> l = new LambdaQueryWrapper<>();
            l.like(id != null, OjQuestion::getId, id);
            q = getOne(l);
            //数据库也没有的话返回错误
            if (q == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "题目不存在");
            }
            //否则，更新缓存，返回数据
            else {
                System.out.println("更新缓存");
                stringRedisTemplate.opsForValue().set(PRO_PREFIX + id, JSON.toJSONString(q), 24, TimeUnit.HOURS);
            }
        } else {
            //有则不需要查数据库
            System.out.println("缓存中已经有了");
            q = JSONUtil.toBean(s, OjQuestion.class);
        }


        queswithsamples qw = new queswithsamples();

        BeanUtils.copyProperties(q, qw);

        LambdaQueryWrapper<OjTestSamples> ll = new LambdaQueryWrapper<>();
        ll.eq(OjTestSamples::getQid, q.getId());

        List<OjTestSamples> testSamples = sampleMapper.selectList(ll);

        qw.setTestSamples(testSamples);

        qw.setIspass(collect.contains(q.getId()));


        ResponseResult responseResult = PageResponseResult.okResult(qw);

        return responseResult;
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public ResponseResult cc(CodeDto cd) {//检验代码


        LambdaQueryWrapper<OjTestSamples> l = new LambdaQueryWrapper<>();
        System.out.println("cid:" + cd.getQid());

        l.eq(OjTestSamples::getQid, cd.getQid());
        OjTestSamples ojTestSamples = sampleMapper.selectOne(l);


        String sampleInput = ojTestSamples.getSampleInput();

        String sampleOutput = ojTestSamples.getSampleOutput();

        Map<String, String> data = new HashMap();
        data.put("input", sampleInput);
        data.put("code", cd.getCode());
        data.put("output", sampleOutput);
        data.put("uqid", UserContext.getUser().toString() + "-" + cd.getQid());

        rabbitTemplate.convertAndSend(OJ_TEST_EXCHANGE, OJ_TEST_KEY, data);

        return ResponseResult.okResult("提交成功，等待结果");
    }

    @Autowired
    private OjSubmitRecordMapper ojSubmitRecordMapper;
    @Autowired
    private QuesMapper qm;

    @Override
    public ResponseResult rc(Rquerydto rd) {

        LambdaQueryWrapper<OjSubmitRecord> l = new LambdaQueryWrapper<>();
        l.eq(OjSubmitRecord::getUid, rd.getUid()).eq(OjSubmitRecord::getTitle,"Accepted");
        List<OjSubmitRecord> ojSubmitRecords = ojSubmitRecordMapper.selectList(l);

        List<RecordDto> collect = ojSubmitRecords.stream().map(i -> {
            RecordDto rt = new RecordDto();
            BeanUtils.copyProperties(i, rt);

            long qid = i.getQid();
            String questionName = qm.selectOne(new LambdaQueryWrapper<OjQuestion>().eq(OjQuestion::getId, qid)).getQuestionName();
            rt.setQuesName(questionName);
            return rt;
        }).collect(Collectors.toList());


        return ResponseResult.okResult(collect);

    }

    @Autowired
    private OjDayProMapper ojDayProMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public ResponseResult dp(String d) {

        LambdaQueryWrapper<OjDayPro> l = new LambdaQueryWrapper<>();
        l.eq(OjDayPro::getDayStr, d);
        OjDayPro ojDayPro = ojDayProMapper.selectOne(l);
        if (ojDayPro != null) return ResponseResult.okResult(ojDayPro.getQid());
        else {
            List<Long> allids = qm.allids();
            int i = RandomUtils.nextInt(0, allids.size() - 1);
            Long rl = allids.get(i);
            OjDayPro ojDayPro1 = new OjDayPro();
            ojDayPro1.setDayStr(d);
            ojDayPro1.setQid(rl.toString());
            ojDayProMapper.insert(ojDayPro1);
            return ResponseResult.okResult(rl);
        }
    }


    @Override
    public ResponseResult ft(Long uid) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String td = formatter.format(date);

        LambdaQueryWrapper<OjDayPro> l = new LambdaQueryWrapper<>();
        l.eq(OjDayPro::getDayStr, td);
        OjDayPro ojDayPro = ojDayProMapper.selectOne(l);

        if (ojDayPro != null) {
            Integer i = quesUserMapper.selectCount(new LambdaQueryWrapper<OjUserQues>().eq(OjUserQues::getUid, uid).eq(OjUserQues::getQid, ojDayPro.getQid()));
            return ResponseResult.okResult(i);
        } else {
            List<Long> allids = qm.allids();
            int i = RandomUtils.nextInt(0, allids.size() - 1);
            Long rl = allids.get(i);
            OjDayPro ojDayPro1 = new OjDayPro();
            ojDayPro1.setDayStr(td);
            ojDayPro1.setQid(rl.toString());
            ojDayProMapper.insert(ojDayPro1);
            Integer ii = quesUserMapper.selectCount(new LambdaQueryWrapper<OjUserQues>().eq(OjUserQues::getUid, uid).eq(OjUserQues::getQid, rl));
            return ResponseResult.okResult(ii);
        }
    }

    @Autowired
    private OjUserSoluMapper ojUserSoluMapper;

    @Resource
    public UserClient userClient;

    @Override
    public ResponseResult ss(OjSolu ojSolu) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String td = formatter.format(date);

        OjSolu ojSolu1 = new OjSolu(ojSolu.getUid(), ojSolu.getQid(), ojSolu.getText(), td, 0, ojSolu.getTitle());
        System.out.println(ojSolu1);
        ojUserSoluMapper.insert(ojSolu1);
        return ResponseResult.okResult("发布成功");
    }

    @Override
    public ResponseResult sl(Long id) {
        LambdaQueryWrapper<OjSolu> l = new LambdaQueryWrapper<>();
        l.eq(Rquerydto::getQid, id);
        List<OjSolu> ojSolus = ojUserSoluMapper.selectList(l);
        List<SoluDto> collect = ojSolus.stream().map(i -> {
            Long uid = i.getUid();
            OjUser getuser = userClient.getuser(uid);
            String url = getuser.getUrl();
            String nickName = getuser.getNickName();
            SoluDto sd = new SoluDto();
            BeanUtils.copyProperties(i, sd);
            sd.setUrl(url);
            sd.setNickName(nickName);
            return sd;
        }).collect(Collectors.toList());
        return ResponseResult.okResult(collect);
    }

    @Override
    public ResponseResult prc(Rquerydto rd) {
        List<RecordDto> collect = new ArrayList<>();
        if (rd.getQid() != null) {

            LambdaQueryWrapper<OjSubmitRecord> l = new LambdaQueryWrapper<>();
            l.eq(OjSubmitRecord::getQid, rd.getQid()).eq(OjSubmitRecord::getUid, rd.getUid());
            List<OjSubmitRecord> ojSubmitRecords = ojSubmitRecordMapper.selectList(l);

            collect = ojSubmitRecords.stream().map(i -> {
                RecordDto rt = new RecordDto();
                BeanUtils.copyProperties(i, rt);
                long qid = i.getQid();
                String questionName = qm.selectOne(new LambdaQueryWrapper<OjQuestion>().eq(OjQuestion::getId, qid)).getQuestionName();
                rt.setQuesName(questionName);
                return rt;
            }).collect(Collectors.toList());

        }
        return ResponseResult.okResult(collect);

    }


    @Override
    public ResponseResult sn(Long uid) {
        SoluNumsDto getnums = qm.getnums(uid);
        SoluNumsDto gettotalnums = qm.gettotalnums();
        List<SoluNumsDto>data=Arrays.asList(getnums,gettotalnums);
        return  ResponseResult.okResult(data);
    }
    @Override
    public ResponseResult addl(Long id) {
        qm.addl(id);
        return ResponseResult.okResult("66");
    }
}

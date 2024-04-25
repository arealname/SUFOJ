package com.heima.judge.listener;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.judge.config.CustomizeWebSocketHandler;
import com.heima.judge.judge.judgeService;
import com.heima.judge.judge.judgeSe2;
import com.heima.judge.mapper.OjSubmitRecordMapper;
import com.heima.judge.mapper.OjUserQuesMapper;
import com.heima.model.ques.pojos.OjUserQues;
import com.heima.model.submit.pojos.OjSubmitRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
public class TestListener {
    @Autowired
    private judgeService ju;

    @Autowired
    private judgeSe2 judgeSe;

    @Autowired
    private CustomizeWebSocketHandler customizeWebSocketHandler;

    @Autowired
    OjSubmitRecordMapper ojSubmitRecordMapper;

    @Autowired
    OjUserQuesMapper ojUserQuesMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "oj.test.queue"),
            exchange = @Exchange(name = "oj.test.exchange", type = ExchangeTypes.DIRECT),
            key = {"oj.test.key"}
    ))
    public void listenDirectQueue1(Map msg) throws InterruptedException {

//        List judge = ju.judge(msg);        //一个一个执行，可以看出在第几个通过了
//        String res="";
//        if(judge.get(0)==judge.get(1))res="通过全部用例";
//        else res="通过"+ judge.get(0) +"/"+ judge.get(1);


        String uqid = (String) msg.get("uqid");
        System.out.println("uqid:"+uqid);


        OjSubmitRecord osr = new OjSubmitRecord();

        String[] split = uqid.split("-");

        osr.setUid(Long.parseLong(split[0]));
        osr.setQid(Long.parseLong(split[1]));

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
        String formattedDate = formatter.format(date);
        osr.setSubmitTime(formattedDate);

        osr.setLanguage("C++");
        osr.setCode((String) msg.get("code"));

        String res = null;      //全部测试用例一次执行
        try {
            res = judgeSe.judge2(msg);
            System.out.println(res);
            Map parse = (Map) JSON.parse(res);
            osr.setMemory(Double.parseDouble((String) parse.get("memory")));
            osr.setTime(Double.parseDouble((String) parse.get("time")));
            osr.setTitle((String) parse.get("description"));

            if ((parse.get("description")).equals("Accepted")) {//成功执行则插入做题成功记录
                OjUserQues ojUserQues = new OjUserQues();
                ojUserQues.setQid(Long.parseLong(split[1]));
                ojUserQues.setUid(Long.parseLong(split[0]));
                LambdaQueryWrapper<OjUserQues> l = new LambdaQueryWrapper<>();
                l.eq(OjUserQues::getUid, ojUserQues.getUid()).eq(OjUserQues::getQid, ojUserQues.getQid());
                Integer i = ojUserQuesMapper.selectCount(l);
                if (i.intValue() == 0) ojUserQuesMapper.insert(ojUserQues);
            }
            customizeWebSocketHandler.sendtoone(uqid, res);
        } catch (Exception e) {
            osr.setMessage("complier error");
            try {
               res="{\"description\":\"complier error\"}";
                customizeWebSocketHandler.sendtoone(uqid, res);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            ojSubmitRecordMapper.insert(osr);//无论成功与否都插入提交记录
        }

//        try {
//
//        } catch (Exception e) {
//            try {
//                customizeWebSocketHandler.sendtoone(uid, "Network down");
//            } catch (Exception ex) {
//                throw new RuntimeException(ex);
//            }
//        }

    }
}

package com.heima.judge.judge;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MySer2 {
    @Autowired
    private RestTemplate restTemplate;

    public String handle2(String url, Map<String, String> d) throws HttpClientErrorException, InterruptedException {

        String s = fetchResult2(url, d);

        System.out.println(s);//代码运行的结果   {"token":"xxxxxx"}

        String re = "";

        Thread.sleep(10000);
        Map parse = (Map) JSON.parse(s);

        re = getres2((String) parse.get("token"));  //解析出token并再次请求

        return re;
    }


    public String fetchResult2(String url, Map<String, String> d) {//提交代码

        RestTemplate restTemplate = new RestTemplate();

// 设置请求体内容
        JSONObject requestBody = new JSONObject();

        requestBody.put("source_code", d.get("code"));
        requestBody.put("stdin", d.get("inp"));
        requestBody.put("language_id", 52);
        requestBody.put("expected_output", d.get("ex_outp") + "\n");


        HttpHeaders headers = new HttpHeaders();
        //设置请求体中包含的数据类型为json
        headers.setContentType(MediaType.APPLICATION_JSON);

// 将请求头和请求体封装成 HttpEntity 对象
        org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity(requestBody, headers);
// 发送 POST 请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

// 获取响应体内容
        String responseBody = responseEntity.getBody();

// 获取服务器返回的参数
        int statusCode = responseEntity.getStatusCodeValue();
        HttpHeaders responseHeaders = responseEntity.getHeaders();


        return responseBody;
    }

    public String getres2(String token) throws InterruptedException,HttpClientErrorException {          //获取结果

        String forObject = restTemplate.getForObject("http://192.168.128.130:3000/submissions/" + token, String.class);

        Map parse = (Map) JSON.parse(forObject);

        Map status = (Map) parse.get("status");

        String res = "";
        String description = (String) status.get("description");
        String time = (String) parse.get("time");
        int memo = (int) parse.get("memory");


        int id = (int) status.get("id");


        if (id <= 2) {

            Thread.sleep(3000);
            res = getres2(token);

        } else {
            Map<String, String> resm = new HashMap<>();
            resm.put("description", description);
            resm.put("time", time);
            resm.put("memory", memo+"");

            res = JSON.toJSONString(resm);
        }

        return res;
    }
}

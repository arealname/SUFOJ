package com.heima.judge.judge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.*;

@Service

public class MyService {
    @Autowired
    private RestTemplate restTemplate;

    public boolean handle(String url, Map<String, String> d, int n) throws InterruptedException {
        String s = fetchResult(url, d);
        Thread.sleep(6000*n);       //测评机是排队进行的
        Map parse = (Map) JSON.parse(s);
        boolean re = getres((String) parse.get("token"));
        return re;
    }

    public String fetchResult(String url, Map<String, String> d) {//提交代码

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

    public boolean getres(String token) {          //获取结果

        String forObject = restTemplate.getForObject("http://192.168.128.130:3000/submissions/" + token, String.class);

        Map parse = (Map) JSON.parse(forObject);

        Map status = (Map) parse.get("status");

        String description = (String) status.get("description");
        System.out.println(description);

        if (description.equals("Accepted")) return true;
        else return false;
    }
}
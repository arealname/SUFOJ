package com.heima.judge.judge;


import com.alibaba.fastjson.JSON;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class judgeService {
    @Autowired
    private MyService myService;

    public List<Integer> judge(Map<String, String> data) throws InterruptedException, ExecutionException {

        List<String> inputs = (List) JSON.parse(data.get("input"));
        List<String> outputs = (List) JSON.parse(data.get("output"));

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        String code = data.get("code");

        long begin = System.currentTimeMillis();
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            int finalI = i;
            futures.add(executorService.submit(() -> {
                // 执行线程任务并返回结果
                Map<String, String> da = new HashMap<>();
                da.put("code", code);
                da.put("inp", inputs.get(finalI));
                da.put("ex_outp", outputs.get(finalI));
                // 执行网络请求的代码
                boolean handle = myService.handle("http://192.168.128.130:3000/submissions", da, inputs.size());
                return handle;
            }));
        }
        executorService.shutdown(); // 关闭线程池

        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> future : futures) {
            results.add(future.get()); // 等待线程完成并获取结果
        }

        long end = System.currentTimeMillis();

        executorService.shutdown();

        int pa=0,t=0;
        for (Boolean b : results){if(b)pa++;t++;}

        List<Integer>a=new ArrayList<>();
        a.add(pa);a.add(t);
        return a;
    }
}

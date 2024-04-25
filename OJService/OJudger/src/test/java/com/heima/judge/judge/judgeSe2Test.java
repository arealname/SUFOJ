package com.heima.judge.judge;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.i18n.qual.LocalizableKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class judgeSe2Test {


    @Test
    void test1() {
        Map<String, String> data=new HashMap<>();

        data.put("code","#include <bits/stdc++.h>\r\nusing namespace std;\r\n\r\nint main   (){\r\n   \r\n   int a,b,c;\r\n   \r\n  cin>>a>>b;\r\n  c=a+b;\r\n    cout<<c<<endl;\r\n    \r\n  return 0;  \r\n    \r\n    \r\n}");
        data.put("input","[\"4 2\",\"4 2\",\"4 1\"]");
        data.put("output","[\"6\",\"6\",\"5\"]");

        judge2(data);
    }

    public boolean judge2(Map<String, String> data) {
        List<String> inputs = (List) JSON.parse(data.get("input"));   //输入
        List<String> outputs = (List) JSON.parse(data.get("output"));

        String ocode = data.get("code");//原始代码

        long begin = System.currentTimeMillis();

        String allin = StringUtils.join(inputs, " ");//拼接全部输入
        String allout = StringUtils.join(outputs, "\n");//拼接全部输出
        System.out.println(allin);
        System.out.println(allout);

        StringBuilder code=new StringBuilder(ocode);


        int i = code.indexOf("main");    //找到main后面的第一个"{"
        String substring = code.substring(i);
        int i1 = substring.indexOf("{");

        int t1=i+i1+1;

        String a=" for(int test_num=0;test_num<"+inputs.size()+";test_num++){\n";

        code.insert(t1,a);//for循环前面一段部分加好了

        System.out.println(code.charAt(t1+1));

        int m=1;
        int j;
        for(j=t1+1;j<code.length();j++){
            char c=code.charAt(j);
            if(c=='{')m++;
            else if(c=='}'){m--;  if(m==1)break;}
        }

        System.out.println(code.charAt(j));

        int pos=code.lastIndexOf("return 0");

        if(pos==-1)code.insert(j,"}");
        else code.insert(pos,"}");

//        code.insert(code.lastIndexOf("return 0"),"}");

        System.out.println(code);
        Map<String, String> da = new HashMap<>();
        da.put("code", code.toString());
        da.put("inp", allin);
        da.put("ex_outp", allout);
        // 执行网络请求的代码
//        boolean handle = myService.handle("http://192.168.128.130:3000/submissions", da, inputs.size());
        return true;
    }


    @Test
    public void trst(){
        String res="{\"memory\":\"22688\",\"description\":\"Accepted\",\"time\":\"0.016\"}";
        Map parse =(Map) JSON.parse(res);
        System.out.println(Double.parseDouble( (String) parse.get("memory")));
//        System.out.println((Double) parse.get("time"));
        System.out.println((String) parse.get("description"));

    }

    @Test
    public void t(){
        List<Long> allids =new ArrayList<>();
        allids.add(100001010l);
        allids.add(1028919l);
        allids.add(19289191l);
        int i = RandomUtils.nextInt(0, allids.size()-1);
        Long rl = allids.get(i);
        System.out.println(rl);
    }


}
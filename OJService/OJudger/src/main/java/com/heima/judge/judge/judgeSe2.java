package com.heima.judge.judge;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class judgeSe2 {
    @Autowired
    private MySer2 mySer2;
    public String judge2(Map<String, String> data) throws Exception {
        List<String> inputs = (List) JSON.parse(data.get("input"));   //输入
        List<String> outputs = (List) JSON.parse(data.get("output"));

        String ocode = data.get("code");//原始代码

        String allin = StringUtils.join(inputs, "\n");//拼接全部输入
        String allout = StringUtils.join(outputs, "\n");//拼接全部输出

        StringBuilder code=new StringBuilder(ocode);     //构造测试代码

        System.out.println("allin"+allin);
        System.out.println("oringin:"+ocode);

        int i = code.indexOf("main");    //找到main后面的第一个"{"
        if(i==-1)return "error";
        String substring = code.substring(i);
        int i1 = substring.indexOf("{");

        int t1=i+i1+1;

        String a=" for(int test_num=0;test_num<"+inputs.size()+";test_num++){\n";

        code.insert(t1,a);//for循环前面一段部分加好了

        int m=0;
        int j;
        for(j=t1+1;j<code.length();j++){
            char c=code.charAt(j);
            if(c=='{')m++;
            else if(c=='}'){m--;if(m==0)break;}
        }                               //找到与main(){匹配的右括号

        int pos=code.lastIndexOf("return 0");

        if(pos==-1)code.insert(j,"}");
        else code.insert(pos,"}");

        System.out.println("changed:"+code);
        Map<String, String> da = new HashMap<>();
        da.put("code", code.toString());
        da.put("inp", allin);
        da.put("ex_outp", allout);
        // 执行网络请求的代码
        String handle = mySer2.handle2("http://192.168.128.130:3000/submissions", da);

        return handle;
    }

}



package com.heima.ques.controller;


import com.heima.model.common.dtos.*;

import com.heima.model.ques.pojos.OjSolu;
import com.heima.ques.service.quesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/problem")
public class Quescontroller {
    @Autowired
    private quesService qs;
    @PostMapping("/list")
    public ResponseResult qlist(@RequestBody PageRequestDto dto) {
        return qs.ql(dto);
    }

    @GetMapping("/{id}")
    public ResponseResult qone(@PathVariable Long id) {
        return qs.qo(id);
    }

    @PostMapping("/code")
    public ResponseResult subcode(@RequestBody CodeDto cd){
        return qs.cc(cd);
    }

    @PostMapping("/record")
    public ResponseResult record(@RequestBody Rquerydto rd){
        return qs.rc(rd);
    }

    @PostMapping("/precord")
    public ResponseResult pre(@RequestBody Rquerydto d){
        return qs.prc(d);
    }

    @GetMapping("/dayproblem")
    public ResponseResult dayp(@RequestParam String d){
        return qs.dp(d);
    }

    @GetMapping("/finishtoday")
    public ResponseResult dayp(@RequestParam Long uid){
        return qs.ft(uid);
    }

    @PostMapping("/solusubmit")
    public ResponseResult ss(@RequestBody OjSolu ojSolu){
        return qs.ss(ojSolu);
    }

    @GetMapping("/solus/{id}")
    public ResponseResult solus(@PathVariable Long id){
        return qs.sl(id);
    }

    @GetMapping("/solunums/{id}")
    public ResponseResult snums(@PathVariable Long id){
        return qs.sn(id);
    }
    @PostMapping("/addlc/{sid}")
    public ResponseResult addl(@PathVariable Long sid){
        System.out.println(sid);
        return qs.addl(sid);
    }


}

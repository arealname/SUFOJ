package com.heima.blog.controller;


import com.heima.blog.service.BlogService;
import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.PageRequestDto;
import com.heima.model.common.dtos.ResponseResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


@RestController
@RequestMapping("/blog")
public class Blogcontroller {
    @Autowired
    private BlogService blogService;

    @GetMapping("/")
    public ResponseResult balist(Integer p,Integer ps) {

        return blogService.bal(p,ps);
    }

    @GetMapping("/{type}")
    public ResponseResult blist(@PathVariable String type,Integer p,Integer ps) {

        return blogService.bl(type,p,ps);
    }

    @GetMapping("/detail/{id}")
    public ResponseResult bo(@PathVariable Long id){
        return blogService.bo(id);
    }

    @PostMapping("/hlike")
    public ResponseResult hlike(@RequestBody Bloglistwithstate dto){

        return blogService.hl(dto);
    }

    @PostMapping("/hstar")
    public ResponseResult hstar(@RequestBody Bloglistwithstate dto){

        return blogService.hs(dto);
    }

    @PostMapping("/upload/file")
    public ResponseResult upload(@RequestBody MultipartFile file){
        System.out.println(file);

        return blogService.upl(file);
    }

    @PostMapping("/submit")
    public ResponseResult sub(@RequestBody Bloglistwithstate d){
        System.out.println(d.getTitle());
        return blogService.sub(d);
    }


    @GetMapping("/dt")
    public ResponseResult dt(){
        return blogService.dt();
    }

}

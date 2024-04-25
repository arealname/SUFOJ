package com.heima.user.controller;


import com.heima.model.common.dtos.Bloglistwithstate;
import com.heima.model.common.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.OjUser;
import com.heima.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class Usercontroller {


    @Autowired
    private UserService userService;

    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto dto) {
        return userService.login(dto);
    }

    @PostMapping("/register")
    public ResponseResult reg(@RequestBody LoginDto dto) {
        return userService.reg(dto);
    }

    @PostMapping("/update")
    public ResponseResult upinfo(@RequestBody LoginDto dto) {
        return userService.upi(dto);

    }

    @GetMapping("/getUser")
    public ResponseResult getinfo() {
        return userService.gu();
    }

    @GetMapping("/one/{id}")
    public OjUser getuser(@PathVariable Long id) {
        return userService.getu(id);
    }

    @GetMapping()
    public ResponseResult personpage(@RequestParam Long id){
        return ResponseResult.okResult(userService.getu(id));
    }


    @PostMapping("/follow")
    public ResponseResult hfol(@RequestBody Bloglistwithstate dto){
        return userService.hf(dto);
    }

    @PostMapping("/follow/common/{id}")
    public ResponseResult commonfollow(@PathVariable Long id){
        return userService.cf(id);
    }
}

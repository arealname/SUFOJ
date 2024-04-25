package com.heima.api;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.OjUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@FeignClient(value="oj-user")
@RestController
public interface UserClient {
    @GetMapping("/user/one/{id}")
    public OjUser getuser(@PathVariable Long id);
}

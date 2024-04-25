package com.heima.model.common.dtos;

import lombok.Data;

@Data
public class LoginDto {
    private String nickName;
    private String password;
    private String email;

    private String school;
    private String location;
    private String web;
    private String gender;
}

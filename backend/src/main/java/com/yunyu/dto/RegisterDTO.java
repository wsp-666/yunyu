package com.yunyu.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String account;
    private String password;
    private String nickname;
    private String phone;
    private Integer role;
}

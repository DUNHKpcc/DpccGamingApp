package com.dpccgaming.backend.auth.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReigisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3,max = 32 ,message = "用户名长度需要在3-32之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需要在6-64之间")
    private String password;
}

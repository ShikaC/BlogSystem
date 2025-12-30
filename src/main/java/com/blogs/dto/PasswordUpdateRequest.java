package com.blogs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码修改请求
 */
@Data
public class PasswordUpdateRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

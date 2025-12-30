package com.blogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求DTO
 */
@Data
public class CommentRequest {
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字")
    private String content;
    
    @Size(max = 50, message = "昵称不能超过50字")
    private String nickname;
    
    @Size(max = 100, message = "邮箱不能超过100字")
    private String email;
    
    @Size(max = 200, message = "网址不能超过200字")
    private String website;
    
    private Long parentId;
    
    private Long replyToId;
    
    // 验证码
    private String captcha;
    private String captchaKey;
}

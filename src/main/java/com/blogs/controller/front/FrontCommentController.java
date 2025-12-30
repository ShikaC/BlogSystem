package com.blogs.controller.front;

import com.blogs.common.Result;
import com.blogs.dto.CommentRequest;
import com.blogs.dto.CommentVO;
import com.blogs.exception.BusinessException;
import com.blogs.service.CaptchaService;
import com.blogs.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 */
@RestController
@RequestMapping("/front/comments")
public class FrontCommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private CaptchaService captchaService;
    
    /**
     * 发表评论
     */
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentRequest request,
                                           HttpServletRequest httpRequest) {
        // 验证码验证
        if (request.getCaptchaKey() != null && request.getCaptcha() != null) {
            if (!captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new BusinessException("验证码错误");
            }
        }
        
        String ipAddress = getClientIp(httpRequest);
        CommentVO comment = commentService.createComment(request, ipAddress);
        return Result.success(comment);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

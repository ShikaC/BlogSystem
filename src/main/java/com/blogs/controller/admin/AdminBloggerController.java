package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.dto.BloggerDTO;
import com.blogs.dto.BloggerUpdateRequest;
import com.blogs.dto.PasswordUpdateRequest;
import com.blogs.service.BloggerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台博主信息管理控制器
 */
@RestController
@RequestMapping("/admin/blogger")
public class AdminBloggerController {
    
    @Autowired
    private BloggerService bloggerService;
    
    /**
     * 获取博主信息
     */
    @GetMapping
    public Result<BloggerDTO> getBloggerInfo() {
        BloggerDTO dto = bloggerService.getBloggerInfo();
        return Result.success(dto);
    }
    
    /**
     * 更新博主信息
     */
    @PutMapping
    public Result<Void> updateBloggerInfo(@RequestBody BloggerUpdateRequest request) {
        bloggerService.updateBloggerInfo(request);
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        bloggerService.updatePassword(request);
        return Result.success();
    }
}

package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.entity.FriendLink;
import com.blogs.service.FriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台友链管理控制器
 */
@RestController
@RequestMapping("/admin/friend-links")
public class AdminFriendLinkController {
    
    @Autowired
    private FriendLinkService friendLinkService;
    
    /**
     * 获取所有友链
     */
    @GetMapping
    public Result<List<FriendLink>> getAllFriendLinks() {
        List<FriendLink> links = friendLinkService.getAllFriendLinks();
        return Result.success(links);
    }
    
    /**
     * 创建友链
     */
    @PostMapping
    public Result<FriendLink> createFriendLink(@RequestParam String name,
                                               @RequestParam String url,
                                               @RequestParam(required = false) String logo,
                                               @RequestParam(required = false) String description) {
        FriendLink link = friendLinkService.createFriendLink(name, url, logo, description);
        return Result.success(link);
    }
    
    /**
     * 更新友链
     */
    @PutMapping("/{id}")
    public Result<FriendLink> updateFriendLink(@PathVariable Long id,
                                               @RequestParam String name,
                                               @RequestParam String url,
                                               @RequestParam(required = false) String logo,
                                               @RequestParam(required = false) String description,
                                               @RequestParam(required = false) Integer sortOrder,
                                               @RequestParam(required = false) Boolean isVisible) {
        FriendLink link = friendLinkService.updateFriendLink(id, name, url, logo, description, sortOrder, isVisible);
        return Result.success(link);
    }
    
    /**
     * 删除友链
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFriendLink(@PathVariable Long id) {
        friendLinkService.deleteFriendLink(id);
        return Result.success();
    }
}

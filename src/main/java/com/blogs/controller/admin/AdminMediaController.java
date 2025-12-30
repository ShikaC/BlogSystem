package com.blogs.controller.admin;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.entity.Media;
import com.blogs.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 后台媒体库管理控制器
 */
@RestController
@RequestMapping("/admin/media")
public class AdminMediaController {
    
    @Autowired
    private MediaService mediaService;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Media> uploadFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false) String category) throws IOException {
        Media media = mediaService.uploadFile(file, category);
        return Result.success(media);
    }
    
    /**
     * 获取媒体列表
     */
    @GetMapping
    public Result<PageResult<Media>> getMediaList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageResult<Media> result = mediaService.getMediaList(type, category, keyword, page, size);
        return Result.success(result);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) throws IOException {
        mediaService.deleteFile(id);
        return Result.success();
    }
}

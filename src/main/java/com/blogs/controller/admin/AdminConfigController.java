package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.service.SiteConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台站点配置控制器
 */
@RestController
@RequestMapping("/admin/config")
public class AdminConfigController {
    
    @Autowired
    private SiteConfigService siteConfigService;
    
    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<Map<String, String>> getAllConfigs() {
        Map<String, String> configs = siteConfigService.getAllConfigs();
        return Result.success(configs);
    }
    
    /**
     * 批量保存配置
     */
    @PostMapping
    public Result<Void> saveConfigs(@RequestBody Map<String, String> configs) {
        siteConfigService.batchSetConfigs(configs);
        return Result.success();
    }
    
    /**
     * 初始化默认配置
     */
    @PostMapping("/init")
    public Result<Void> initConfigs() {
        siteConfigService.initDefaultConfigs();
        return Result.success();
    }
}

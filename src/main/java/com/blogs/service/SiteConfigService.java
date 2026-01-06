package com.blogs.service;

import com.blogs.entity.SiteConfig;
import com.blogs.repository.SiteConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点配置服务
 */
@Service
@Transactional
public class SiteConfigService {
    
    @Autowired
    private SiteConfigRepository siteConfigRepository;
    
    /**
     * 获取配置值
     */
    public String getConfig(String key) {
        return siteConfigRepository.findByConfigKey(key)
                .map(SiteConfig::getConfigValue)
                .orElse(null);
    }
    
    /**
     * 获取配置值（带默认值）
     */
    public String getConfig(String key, String defaultValue) {
        return siteConfigRepository.findByConfigKey(key)
                .map(SiteConfig::getConfigValue)
                .orElse(defaultValue);
    }
    
    /**
     * 设置配置值
     */
    public void setConfig(String key, String value, String description) {
        SiteConfig config = siteConfigRepository.findByConfigKey(key)
                .orElseGet(() -> {
                    SiteConfig newConfig = new SiteConfig();
                    newConfig.setConfigKey(key);
                    return newConfig;
                });
        
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        siteConfigRepository.save(config);
    }
    
    /**
     * 批量获取配置
     */
    public Map<String, String> getAllConfigs() {
        List<SiteConfig> configs = siteConfigRepository.findAll();
        Map<String, String> result = new HashMap<>();
        for (SiteConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }
    
    /**
     * 批量设置配置
     */
    public void batchSetConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            setConfig(entry.getKey(), entry.getValue(), null);
        }
    }
    
    /**
     * 初始化默认配置
     */
    public void initDefaultConfigs() {
        setConfigIfAbsent("site_name", "我的博客", "博客名称");
        setConfigIfAbsent("site_subtitle", "记录生活，分享技术", "博客副标题");
        setConfigIfAbsent("site_logo", "", "博客Logo");
        setConfigIfAbsent("site_favicon", "", "网站图标");
        setConfigIfAbsent("site_footer", "© 2025 我的博客. All rights reserved.", "页脚版权信息");
        setConfigIfAbsent("site_icp", "", "备案信息");
        setConfigIfAbsent("site_announcement", "", "网站公告（首页展示）");
        setConfigIfAbsent("article_page_size", "10", "首页文章展示条数");
        setConfigIfAbsent("article_summary_length", "200", "文章摘要字数");
        setConfigIfAbsent("comment_enabled", "true", "是否开启评论");
        setConfigIfAbsent("view_count_enabled", "true", "是否显示阅读量");
        setConfigIfAbsent("like_count_enabled", "true", "是否显示点赞量");
        setConfigIfAbsent("seo_title", "", "SEO标题");
        setConfigIfAbsent("seo_keywords", "", "SEO关键词");
        setConfigIfAbsent("seo_description", "", "SEO描述");
        // 安全配置
        setConfigIfAbsent("jwt_expiration", "604800000", "JWT Token有效期（毫秒），默认7天");
        setConfigIfAbsent("password_min_length", "6", "密码最小长度");
        setConfigIfAbsent("password_require_uppercase", "false", "密码是否要求大写字母");
        setConfigIfAbsent("password_require_lowercase", "false", "密码是否要求小写字母");
        setConfigIfAbsent("password_require_number", "false", "密码是否要求数字");
        setConfigIfAbsent("password_require_special", "false", "密码是否要求特殊字符");
        setConfigIfAbsent("sensitive_words", "", "敏感词库（每行一个，用换行符分隔）");
    }
    
    private void setConfigIfAbsent(String key, String value, String description) {
        if (!siteConfigRepository.findByConfigKey(key).isPresent()) {
            setConfig(key, value, description);
        }
    }
}

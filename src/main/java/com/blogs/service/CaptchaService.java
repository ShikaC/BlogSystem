package com.blogs.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务
 */
@Service
public class CaptchaService {
    
    // 简单的内存缓存，生产环境建议使用Redis
    private final Map<String, String> captchaCache = new ConcurrentHashMap<>();
    
    /**
     * 生成验证码
     */
    public Map<String, String> generateCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        String code = captcha.getCode();
        String key = UUID.randomUUID().toString();
        String imageBase64 = captcha.getImageBase64Data();
        
        // 保存验证码
        captchaCache.put(key, code.toLowerCase());
        
        // 5分钟后过期（简单实现，生产环境用Redis设置过期时间）
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
                captchaCache.remove(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return Map.of("key", key, "image", imageBase64);
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        String cachedCode = captchaCache.get(key);
        if (cachedCode == null) {
            return false;
        }
        
        boolean result = cachedCode.equalsIgnoreCase(code);
        if (result) {
            captchaCache.remove(key);
        }
        return result;
    }
}

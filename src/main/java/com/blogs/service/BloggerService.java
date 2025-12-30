package com.blogs.service;

import com.blogs.dto.BloggerDTO;
import com.blogs.dto.BloggerUpdateRequest;
import com.blogs.dto.LoginRequest;
import com.blogs.dto.LoginResponse;
import com.blogs.dto.PasswordUpdateRequest;
import com.blogs.entity.Blogger;
import com.blogs.exception.BusinessException;
import com.blogs.repository.BloggerRepository;
import com.blogs.security.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 博主服务
 */
@Service
@Transactional
public class BloggerService {
    
    @Autowired
    private BloggerRepository bloggerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        Blogger blogger = bloggerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), blogger.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        String token;
        if (Boolean.TRUE.equals(request.getRememberMe())) {
            token = jwtUtils.generateRememberMeToken(blogger.getUsername());
        } else {
            token = jwtUtils.generateToken(blogger.getUsername());
        }
        
        return LoginResponse.of(token, blogger.getNickname(), blogger.getAvatar());
    }
    
    /**
     * 获取博主信息
     */
    public BloggerDTO getBloggerInfo() {
        Blogger blogger = bloggerRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("博主信息不存在"));
        
        BloggerDTO dto = new BloggerDTO();
        BeanUtils.copyProperties(blogger, dto);
        return dto;
    }
    
    /**
     * 更新博主信息
     */
    public void updateBloggerInfo(BloggerUpdateRequest request) {
        Blogger blogger = bloggerRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("博主信息不存在"));
        
        if (request.getNickname() != null) {
            blogger.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            blogger.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            blogger.setBio(request.getBio());
        }
        if (request.getEmail() != null) {
            blogger.setEmail(request.getEmail());
        }
        if (request.getGithub() != null) {
            blogger.setGithub(request.getGithub());
        }
        if (request.getZhihu() != null) {
            blogger.setZhihu(request.getZhihu());
        }
        if (request.getWeixin() != null) {
            blogger.setWeixin(request.getWeixin());
        }
        
        bloggerRepository.save(blogger);
    }
    
    /**
     * 修改密码
     */
    public void updatePassword(PasswordUpdateRequest request) {
        Blogger blogger = bloggerRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BusinessException("博主信息不存在"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), blogger.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        blogger.setPassword(passwordEncoder.encode(request.getNewPassword()));
        bloggerRepository.save(blogger);
    }
    
    /**
     * 初始化博主账号（仅首次使用）
     */
    public void initBlogger(String username, String password, String nickname) {
        if (bloggerRepository.count() > 0) {
            throw new BusinessException("博主账号已存在");
        }
        
        Blogger blogger = new Blogger();
        blogger.setUsername(username);
        blogger.setPassword(passwordEncoder.encode(password));
        blogger.setNickname(nickname);
        bloggerRepository.save(blogger);
    }
}

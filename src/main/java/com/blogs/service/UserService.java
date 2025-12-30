package com.blogs.service;

import com.blogs.dto.UserDTO;
import com.blogs.dto.UserUpdateRequest;
import com.blogs.dto.LoginRequest;
import com.blogs.dto.LoginResponse;
import com.blogs.dto.PasswordUpdateRequest;
import com.blogs.entity.User;
import com.blogs.exception.BusinessException;
import com.blogs.repository.UserRepository;
import com.blogs.security.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        String token;
        if (Boolean.TRUE.equals(request.getRememberMe())) {
            token = jwtUtils.generateRememberMeToken(user.getUsername());
        } else {
            token = jwtUtils.generateToken(user.getUsername());
        }
        
        return LoginResponse.of(token, user.getNickname(), user.getAvatar(), user.getRole());
    }
    
    /**
     * 注册（默认为普通注册用户）
     */
    public void register(String username, String password, String nickname) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole("USER");
        user.setStatus(1);
        userRepository.save(user);
    }
    
    /**
     * 获取用户信息
     */
    public UserDTO getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户信息不存在"));
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户信息不存在"));
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getGithub() != null) {
            user.setGithub(request.getGithub());
        }
        if (request.getZhihu() != null) {
            user.setZhihu(request.getZhihu());
        }
        if (request.getWeixin() != null) {
            user.setWeixin(request.getWeixin());
        }
        
        userRepository.save(user);
    }
    
    /**
     * 修改密码
     */
    public void updatePassword(String username, PasswordUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户信息不存在"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 获取所有用户（管理员）
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 更新用户状态（管理员）
     */
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }
    
    /**
     * 初始化管理员账号（仅首次使用）
     */
    public void initAdmin(String username, String password, String nickname) {
        if (userRepository.count() > 0) {
            throw new BusinessException("系统已初始化");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole("ADMIN");
        user.setStatus(1);
        userRepository.save(user);
    }
}

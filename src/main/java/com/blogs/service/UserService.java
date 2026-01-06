package com.blogs.service;

import com.blogs.dto.LoginResponse;
import com.blogs.dto.UserDTO;
import com.blogs.dto.UserDetailDTO;
import com.blogs.dto.UserListDTO;
import com.blogs.dto.LoginRequest;
import com.blogs.dto.PasswordUpdateRequest;
import com.blogs.dto.UserUpdateRequest;
import com.blogs.entity.Article;
import com.blogs.entity.ForumPost;
import com.blogs.entity.User;
import com.blogs.exception.BusinessException;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.CommentRepository;
import com.blogs.repository.ForumPostRepository;
import com.blogs.repository.UserRepository;
import com.blogs.security.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private CommentRepository commentRepository;

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

        return LoginResponse.of(token, user.getId(), user.getNickname(), user.getAvatar(), user.getRole());
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

    /**
     * 分页获取用户列表（管理员）- 支持筛选
     */
    public Map<String, Object> getUserList(String keyword, String role, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;

        // 根据条件组合查询
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasRole = role != null && !role.trim().isEmpty();
        boolean hasStatus = status != null;

        if (hasKeyword && hasRole && hasStatus) {
            userPage = userRepository.findByKeywordAndRoleAndStatus(keyword, role, status, pageable);
        } else if (hasKeyword && hasRole) {
            userPage = userRepository.findByKeywordAndRole(keyword, role, pageable);
        } else if (hasKeyword && hasStatus) {
            userPage = userRepository.findByKeywordAndStatus(keyword, status, pageable);
        } else if (hasRole && hasStatus) {
            userPage = userRepository.findByRoleAndStatusOrderByCreatedAtDesc(role, status, pageable);
        } else if (hasKeyword) {
            userPage = userRepository.findByKeyword(keyword, pageable);
        } else if (hasRole) {
            userPage = userRepository.findByRoleOrderByCreatedAtDesc(role, pageable);
        } else if (hasStatus) {
            userPage = userRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            userPage = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<UserListDTO> list = userPage.getContent().stream().map(user -> {
            UserListDTO dto = new UserListDTO();
            BeanUtils.copyProperties(user, dto);
            // 统计用户发布的文章和帖子数量
            dto.setArticleCount(articleRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 1))
                    .getTotalElements());
            dto.setPostCount(forumPostRepository.findByUserId(user.getId(), PageRequest.of(0, 1)).getTotalElements());
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", userPage.getTotalElements());
        result.put("pages", userPage.getTotalPages());
        return result;
    }

    /**
     * 获取用户详细信息（管理员）- 含文章/帖子记录
     */
    public UserDetailDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        UserDetailDTO dto = new UserDetailDTO();
        BeanUtils.copyProperties(user, dto);

        // 查询用户发布的文章（最近20条）
        Pageable articlePageable = PageRequest.of(0, 20);
        Page<Article> articlePage = articleRepository.findByUser_IdOrderByCreatedAtDesc(userId, articlePageable);
        dto.setArticleCount(articlePage.getTotalElements());
        dto.setArticles(articlePage.getContent().stream().map(article -> {
            UserDetailDTO.ArticleSummary summary = new UserDetailDTO.ArticleSummary();
            summary.setId(article.getId());
            summary.setTitle(article.getTitle());
            summary.setStatus(article.getStatus());
            summary.setViewCount(article.getViewCount() != null ? article.getViewCount().intValue() : 0);
            summary.setLikeCount(article.getLikeCount() != null ? article.getLikeCount().intValue() : 0);
            summary.setCreatedAt(article.getCreatedAt());
            return summary;
        }).collect(Collectors.toList()));

        // 查询用户发布的帖子（最近20条）
        Pageable postPageable = PageRequest.of(0, 20);
        Page<ForumPost> postPage = forumPostRepository.findByUserId(userId, postPageable);
        dto.setPostCount(postPage.getTotalElements());
        dto.setPosts(postPage.getContent().stream().map(post -> {
            UserDetailDTO.PostSummary summary = new UserDetailDTO.PostSummary();
            summary.setId(post.getId());
            summary.setTitle(post.getTitle());
            summary.setStatus(post.getStatus());
            summary.setViewCount(post.getViewCount() != null ? post.getViewCount().intValue() : 0);
            summary.setLikeCount(post.getLikeCount() != null ? post.getLikeCount().intValue() : 0);
            summary.setCreatedAt(post.getCreatedAt());
            return summary;
        }).collect(Collectors.toList()));

        // 查询评论数量
        dto.setCommentCount(commentRepository.countByUserId(userId));

        return dto;
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", userRepository.count());
        stats.put("active", userRepository.countByStatus(1));
        stats.put("disabled", userRepository.countByStatus(0));
        stats.put("admins", userRepository.countByRole("ADMIN"));
        stats.put("users", userRepository.countByRole("USER"));
        return stats;
    }
}
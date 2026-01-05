package com.blogs.controller.front;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.ArticleVO;
import com.blogs.dto.CommentVO;
import com.blogs.dto.UserActionItemVO;
import com.blogs.dto.UserDTO;
import com.blogs.entity.Favorite;
import com.blogs.entity.ForumPost;
import com.blogs.entity.LikeRecord;
import com.blogs.entity.Notification;
import com.blogs.entity.User;
import com.blogs.repository.CommentRepository;
import com.blogs.repository.FavoriteRepository;
import com.blogs.repository.LikeRecordRepository;
import com.blogs.repository.UserRepository;
import com.blogs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 前台个人中心控制器 - 所有注册用户通用
 */
@RestController
@RequestMapping("/front/user")
public class UserCenterController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private LikeRecordRepository likeRecordRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        String username = getCurrentUsername();
        return Result.success(userService.getUserInfo(username));
    }

    /**
     * 获取我的文章
     */
    @GetMapping("/articles")
    public Result<PageResult<ArticleVO>> getMyArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        return Result.success(articleService.getUserArticleList(username, page, size));
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/posts")
    public Result<PageResult<ForumPost>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        return Result.success(forumService.getMyPosts(user.getId(), page, size));
    }

    /**
     * 获取我的消息
     */
    @GetMapping("/notifications")
    public Result<PageResult<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        return Result.success(notificationService.getNotifications(user.getId(), page, size));
    }

    /**
     * 我的收藏（文章 + 帖子）
     */
    @GetMapping("/favorites")
    public Result<PageResult<UserActionItemVO>> getMyFavorites(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        var p = (type == null || type.isBlank())
                ? favoriteRepository.findByUserId(user.getId(), pageable)
                : favoriteRepository.findByUserIdAndType(user.getId(), type.trim().toUpperCase(), pageable);

        List<UserActionItemVO> list = new ArrayList<>();
        for (Favorite f : p.getContent()) {
            UserActionItemVO vo = new UserActionItemVO();
            vo.setTargetId(f.getTargetId());
            vo.setCreatedAt(f.getCreatedAt());
            if ("ARTICLE".equalsIgnoreCase(f.getType())) {
                vo.setContentType("ARTICLE");
                ArticleVO a = articleService.getArticle(f.getTargetId());
                vo.setTitle(a.getTitle());
            } else {
                vo.setContentType("FORUM_POST");
                ForumPost post = forumService.getPostWithoutIncrement(f.getTargetId());
                vo.setTitle(post.getTitle());
            }
            list.add(vo);
        }
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));
    }

    /**
     * 我的点赞（文章 + 帖子）
     */
    @GetMapping("/likes")
    public Result<PageResult<UserActionItemVO>> getMyLikes(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        var p = likeRecordRepository.findByUserId(user.getId(), pageable);

        List<UserActionItemVO> list = new ArrayList<>();
        for (LikeRecord r : p.getContent()) {
            if (type != null && !type.isBlank() && !r.getType().equalsIgnoreCase(type.trim())) {
                continue;
            }
            UserActionItemVO vo = new UserActionItemVO();
            vo.setTargetId(r.getTargetId());
            vo.setCreatedAt(r.getCreatedAt());
            if ("ARTICLE".equalsIgnoreCase(r.getType())) {
                vo.setContentType("ARTICLE");
                vo.setTitle(articleService.getArticle(r.getTargetId()).getTitle());
            } else {
                vo.setContentType("FORUM_POST");
                vo.setTitle(forumService.getPostWithoutIncrement(r.getTargetId()).getTitle());
            }
            list.add(vo);
        }
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));
    }

    /**
     * 我的评论/回帖（统一 comment 表）
     */
    @GetMapping("/comments")
    public Result<PageResult<CommentVO>> getMyComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        var p = commentRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable);
        List<CommentVO> list = p.getContent().stream().map(CommentVO::fromEntity).toList();
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody com.blogs.dto.UserUpdateRequest request) {
        String username = getCurrentUsername();
        userService.updateUserInfo(username, request);
        return Result.success();
    }

    // ==================== 公开用户主页相关 ====================

    /**
     * 获取公开用户信息（用于查看其他用户主页）
     */
    @GetMapping("/public/{userId}")
    public Result<com.blogs.dto.PublicUserVO> getPublicUserInfo(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return Result.success(com.blogs.dto.PublicUserVO.fromEntity(user));
    }

    /**
     * 获取用户的公开文章列表
     */
    @GetMapping("/public/{userId}/articles")
    public Result<PageResult<ArticleVO>> getUserPublicArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return Result.success(articleService.getUserArticleList(user.getUsername(), page, size));
    }

    /**
     * 获取用户的点赞列表（根据隐私设置）
     */
    @GetMapping("/public/{userId}/likes")
    public Result<PageResult<UserActionItemVO>> getUserPublicLikes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查隐私设置
        if (user.getLikesPublic() == null || !user.getLikesPublic()) {
            // 检查是否是本人
            String currentUsername = getCurrentUsername();
            UserDTO currentUser = userService.getUserInfo(currentUsername);
            if (!currentUser.getId().equals(userId)) {
                throw new RuntimeException("该用户的点赞列表不公开");
            }
        }

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        var p = likeRecordRepository.findByUserId(userId, pageable);

        List<UserActionItemVO> list = new ArrayList<>();
        for (LikeRecord r : p.getContent()) {
            UserActionItemVO vo = new UserActionItemVO();
            vo.setTargetId(r.getTargetId());
            vo.setCreatedAt(r.getCreatedAt());
            if ("ARTICLE".equalsIgnoreCase(r.getType())) {
                vo.setContentType("ARTICLE");
                vo.setTitle(articleService.getArticle(r.getTargetId()).getTitle());
            } else {
                vo.setContentType("FORUM_POST");
                vo.setTitle(forumService.getPostWithoutIncrement(r.getTargetId()).getTitle());
            }
            list.add(vo);
        }
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));
    }

    /**
     * 获取用户的收藏列表（根据隐私设置）
     */
    @GetMapping("/public/{userId}/favorites")
    public Result<PageResult<UserActionItemVO>> getUserPublicFavorites(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查隐私设置
        if (user.getFavoritesPublic() == null || !user.getFavoritesPublic()) {
            // 检查是否是本人
            String currentUsername = getCurrentUsername();
            UserDTO currentUser = userService.getUserInfo(currentUsername);
            if (!currentUser.getId().equals(userId)) {
                throw new RuntimeException("该用户的收藏列表不公开");
            }
        }

        var pageable = org.springframework.data.domain.PageRequest.of(page - 1, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        var p = favoriteRepository.findByUserId(userId, pageable);

        List<UserActionItemVO> list = new ArrayList<>();
        for (Favorite f : p.getContent()) {
            UserActionItemVO vo = new UserActionItemVO();
            vo.setTargetId(f.getTargetId());
            vo.setCreatedAt(f.getCreatedAt());
            if ("ARTICLE".equalsIgnoreCase(f.getType())) {
                vo.setContentType("ARTICLE");
                ArticleVO a = articleService.getArticle(f.getTargetId());
                vo.setTitle(a.getTitle());
            } else {
                vo.setContentType("FORUM_POST");
                ForumPost post = forumService.getPostWithoutIncrement(f.getTargetId());
                vo.setTitle(post.getTitle());
            }
            list.add(vo);
        }
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

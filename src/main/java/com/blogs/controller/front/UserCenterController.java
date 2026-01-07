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
import com.blogs.entity.Media;
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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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

    @Autowired
    private MediaService mediaService;

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
            try {
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
            } catch (Exception e) {
                // 忽略已删除的内容
            }
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
            try {
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
            } catch (Exception e) {
                // 忽略已删除的内容
            }
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
            try {
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
            } catch (Exception e) {
                // 忽略已删除的内容
            }
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
            try {
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
            } catch (Exception e) {
                // 忽略已删除的内容
            }
        }
        return Result.success(PageResult.of(list, p.getTotalElements(), page, size));

    }

    // ==================== 图片上传 ====================

    /**
     * 用户上传图片（用于文章封面、内容图片等）
     */
    @PostMapping("/upload")
    public Result<Media> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        Media media = mediaService.uploadFile(file, "user-upload");
        return Result.success(media);
    }

    // ==================== 用户统计信息 ====================

    /**
     * 获取用户统计数据（文章总数、阅读量、点赞数等）
     */
    @GetMapping("/statistics")
    public Result<java.util.Map<String, Object>> getUserStatistics() {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);

        // 获取用户的所有文章
        var articlePage = articleService.getUserArticleList(username, 1, 1000);

        long totalArticles = articlePage.getTotal();
        long publishedArticles = 0;
        long draftArticles = 0;
        long totalViews = 0;
        long totalLikes = 0;
        long totalCollects = 0;
        long totalComments = 0;

        for (ArticleVO article : articlePage.getList()) {
            if (article.getStatus() == 1)
                publishedArticles++;
            else if (article.getStatus() == 0)
                draftArticles++;
            totalViews += article.getViewCount() != null ? article.getViewCount() : 0;
            totalLikes += article.getLikeCount() != null ? article.getLikeCount() : 0;
            totalCollects += article.getCollectCount() != null ? article.getCollectCount() : 0;
            totalComments += article.getCommentCount() != null ? article.getCommentCount() : 0;
        }

        // 获取帖子统计
        var postPage = forumService.getMyPosts(user.getId(), 1, 1000);
        long totalPosts = postPage.getTotal();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalArticles", totalArticles);
        stats.put("publishedArticles", publishedArticles);
        stats.put("draftArticles", draftArticles);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        stats.put("totalCollects", totalCollects);
        stats.put("totalComments", totalComments);
        stats.put("totalPosts", totalPosts);

        return Result.success(stats);
    }

    // ==================== 按状态筛选文章 ====================

    /**
     * 获取我的文章（带状态筛选）
     */
    @GetMapping("/articles/filter")
    public Result<PageResult<ArticleVO>> getMyArticlesFiltered(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String username = getCurrentUsername();
        return Result.success(articleService.getUserArticleListWithStatus(username, status, page, size));
    }

    // ==================== 批量操作 ====================

    /**
     * 批量下架文章（将状态改为私密）
     */
    @PostMapping("/articles/batch-unpublish")
    public Result<Void> batchUnpublishArticles(@RequestBody java.util.List<Long> ids) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        articleService.batchUpdateStatus(ids, 2, user.getId());
        return Result.success();
    }

    /**
     * 批量删除文章（移到回收站）
     */
    @PostMapping("/articles/batch-delete")
    public Result<Void> batchDeleteArticles(@RequestBody java.util.List<Long> ids) {
        String username = getCurrentUsername();
        UserDTO user = userService.getUserInfo(username);
        articleService.batchUpdateStatus(ids, 3, user.getId());
        return Result.success();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

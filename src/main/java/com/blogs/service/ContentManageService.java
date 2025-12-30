package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.dto.ContentAdminVO;
import com.blogs.entity.Article;
import com.blogs.entity.ForumPost;
import com.blogs.exception.BusinessException;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.ForumPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台全站内容管理统一服务（低侵入：不替换原 Article 管理，只新增统一入口）
 */
@Service
@Transactional
public class ContentManageService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    public PageResult<ContentAdminVO> getContentList(String contentType, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        String type = contentType != null ? contentType.trim().toUpperCase() : null;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (type == null || type.isBlank() || "ARTICLE".equals(type)) {
            Page<Article> p = status == null
                    ? articleRepository.findAllByOrderByCreatedAtDesc(pageable)
                    : articleRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            List<ContentAdminVO> list = p.getContent().stream().map(this::toAdminVO).toList();
            return PageResult.of(list, p.getTotalElements(), page, size);
        }

        if ("FORUM_POST".equals(type)) {
            Page<ForumPost> p = status == null
                    ? forumPostRepository.findAll(pageable)
                    : forumPostRepository.findByStatus(status, pageable);
            List<ContentAdminVO> list = p.getContent().stream().map(this::toAdminVO).toList();
            return PageResult.of(list, p.getTotalElements(), page, size);
        }

        throw new BusinessException("contentType 不合法");
    }

    public void updateStatus(String contentType, Long id, Integer status) {
        String type = contentType != null ? contentType.trim().toUpperCase() : null;
        if (id == null) throw new BusinessException("内容ID不能为空");
        if (status == null) throw new BusinessException("状态不能为空");

        if ("ARTICLE".equals(type)) {
            Article a = articleRepository.findById(id).orElseThrow(() -> new BusinessException("文章不存在"));
            a.setStatus(status);
            articleRepository.save(a);
            return;
        }
        if ("FORUM_POST".equals(type)) {
            ForumPost p = forumPostRepository.findById(id).orElseThrow(() -> new BusinessException("帖子不存在"));
            p.setStatus(status);
            forumPostRepository.save(p);
            return;
        }
        throw new BusinessException("contentType 不合法");
    }

    public void updateTop(String contentType, Long id, Boolean isTop) {
        String type = contentType != null ? contentType.trim().toUpperCase() : null;
        if (id == null) throw new BusinessException("内容ID不能为空");
        if (isTop == null) throw new BusinessException("isTop 不能为空");

        if ("ARTICLE".equals(type)) {
            Article a = articleRepository.findById(id).orElseThrow(() -> new BusinessException("文章不存在"));
            a.setIsTop(isTop);
            articleRepository.save(a);
            return;
        }
        if ("FORUM_POST".equals(type)) {
            ForumPost p = forumPostRepository.findById(id).orElseThrow(() -> new BusinessException("帖子不存在"));
            p.setIsTop(isTop);
            forumPostRepository.save(p);
            return;
        }
        throw new BusinessException("contentType 不合法");
    }

    public void updateEssence(Long id, Boolean isEssence) {
        if (id == null) throw new BusinessException("帖子ID不能为空");
        if (isEssence == null) throw new BusinessException("isEssence 不能为空");
        ForumPost p = forumPostRepository.findById(id).orElseThrow(() -> new BusinessException("帖子不存在"));
        p.setIsEssence(isEssence);
        forumPostRepository.save(p);
    }

    private ContentAdminVO toAdminVO(Article a) {
        ContentAdminVO vo = new ContentAdminVO();
        vo.setContentType("ARTICLE");
        vo.setId(a.getId());
        vo.setUserId(a.getUserId());
        vo.setTitle(a.getTitle());
        vo.setStatus(a.getStatus());
        vo.setIsTop(a.getIsTop());
        vo.setViewCount(a.getViewCount());
        vo.setLikeCount(a.getLikeCount());
        vo.setCollectCount(a.getCollectCount());
        vo.setCommentCount(a.getCommentCount());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        vo.setCategoryId(a.getCategoryId());
        return vo;
    }

    private ContentAdminVO toAdminVO(ForumPost p) {
        ContentAdminVO vo = new ContentAdminVO();
        vo.setContentType("FORUM_POST");
        vo.setId(p.getId());
        vo.setUserId(p.getUserId());
        vo.setTitle(p.getTitle());
        vo.setStatus(p.getStatus());
        vo.setIsTop(p.getIsTop());
        vo.setIsEssence(p.getIsEssence());
        vo.setViewCount(p.getViewCount());
        vo.setLikeCount(p.getLikeCount());
        vo.setCollectCount(p.getCollectCount());
        vo.setCommentCount(p.getCommentCount());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());
        vo.setSectionId(p.getSectionId());
        return vo;
    }
}



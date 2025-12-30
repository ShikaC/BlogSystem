package com.blogs.service;

import com.blogs.common.PageResult;
import com.blogs.dto.SearchResultVO;
import com.blogs.entity.Article;
import com.blogs.entity.ForumPost;
import com.blogs.entity.User;
import com.blogs.repository.ArticleRepository;
import com.blogs.repository.ForumPostRepository;
import com.blogs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 全站统一搜索服务（文章 + 帖子）
 */
@Service
@Transactional(readOnly = true)
public class SearchService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private UserRepository userRepository;

    public PageResult<SearchResultVO> search(String keyword, String contentType, Integer page, Integer size) {
        if (keyword == null || keyword.isBlank()) {
            return PageResult.of(List.of(), 0L, page != null ? page : 1, size != null ? size : 10);
        }
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        String type = contentType != null ? contentType.trim().toUpperCase(Locale.ROOT) : null;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if ("ARTICLE".equals(type)) {
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<Article> p = articleRepository.searchByKeyword(keyword, pageable);
            List<SearchResultVO> list = p.getContent().stream().map(this::toResult).toList();
            return PageResult.of(list, p.getTotalElements(), page, size);
        }

        if ("FORUM_POST".equals(type)) {
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<ForumPost> p = forumPostRepository.searchByKeyword(keyword, pageable);
            List<SearchResultVO> list = toPostResults(p.getContent());
            return PageResult.of(list, p.getTotalElements(), page, size);
        }

        // ALL：用“取前 page*size 再切片”的方式保证混排分页稳定（低侵入实现）
        int fetchSize = page * size;
        Pageable pageable0 = PageRequest.of(0, fetchSize, sort);
        Page<Article> aPage = articleRepository.searchByKeyword(keyword, pageable0);
        Page<ForumPost> pPage = forumPostRepository.searchByKeyword(keyword, pageable0);

        List<SearchResultVO> merged = new ArrayList<>(aPage.getContent().size() + pPage.getContent().size());
        merged.addAll(aPage.getContent().stream().map(this::toResult).toList());
        merged.addAll(toPostResults(pPage.getContent()));

        merged.sort(Comparator.comparing(SearchResultVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(merged.size(), page * size);
        List<SearchResultVO> slice = from >= to ? List.of() : merged.subList(from, to);
        long total = aPage.getTotalElements() + pPage.getTotalElements();
        return PageResult.of(slice, total, page, size);
    }

    private SearchResultVO toResult(Article a) {
        SearchResultVO vo = new SearchResultVO();
        vo.setContentType("ARTICLE");
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setExcerpt(buildExcerpt(a.getSummary(), a.getContent()));
        vo.setCreatedAt(a.getCreatedAt());
        vo.setViewCount(a.getViewCount());
        if (a.getUser() != null) {
            vo.setUsername(a.getUser().getUsername());
            vo.setNickname(a.getUser().getNickname());
        }
        return vo;
    }

    private List<SearchResultVO> toPostResults(List<ForumPost> posts) {
        Set<Long> userIds = posts.stream().map(ForumPost::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return posts.stream().map(p -> {
            SearchResultVO vo = new SearchResultVO();
            vo.setContentType("FORUM_POST");
            vo.setId(p.getId());
            vo.setTitle(p.getTitle());
            vo.setExcerpt(buildExcerpt(null, p.getContent()));
            vo.setCreatedAt(p.getCreatedAt());
            vo.setViewCount(p.getViewCount());
            User u = userMap.get(p.getUserId());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setNickname(u.getNickname());
            }
            return vo;
        }).toList();
    }

    private String buildExcerpt(String summary, String content) {
        String base = (summary != null && !summary.isBlank()) ? summary : content;
        if (base == null) return "";
        String text = base.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        return text.length() > 160 ? text.substring(0, 160) + "..." : text;
    }
}



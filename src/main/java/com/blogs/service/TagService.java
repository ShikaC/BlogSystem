package com.blogs.service;

import com.blogs.entity.Tag;
import com.blogs.exception.BusinessException;
import com.blogs.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签服务
 */
@Service
@Transactional
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * 创建标签
     */
    public Tag createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new BusinessException("标签已存在");
        }

        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    /**
     * 更新标签
     */
    public Tag updateTag(Long id, String name) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("标签不存在"));

        if (!tag.getName().equals(name) && tagRepository.existsByName(name)) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setName(name);
        return tagRepository.save(tag);
    }

    /**
     * 删除标签
     */
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("标签不存在"));

        if (tag.getArticleCount() > 0) {
            throw new BusinessException("该标签下还有文章，无法删除");
        }

        tagRepository.delete(tag);
    }

    /**
     * 获取所有标签（带实际文章数）
     */
    public List<Tag> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        // 计算每个标签的实际已发布文章数
        for (Tag tag : tags) {
            Long count = tagRepository.countPublishedArticlesByTagId(tag.getId());
            tag.setArticleCount(count != null ? count.intValue() : 0);
        }
        return tags;
    }

    /**
     * 获取标签（按文章数排序）
     */
    public List<Tag> getTagsByArticleCount() {
        return tagRepository.findAllByOrderByArticleCountDesc();
    }

    /**
     * 获取标签详情
     */
    public Tag getTag(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("标签不存在"));
    }
}

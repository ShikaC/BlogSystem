package com.blogs.service;

import com.blogs.entity.Category;
import com.blogs.exception.BusinessException;
import com.blogs.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务
 */
@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * 创建分类
     */
    public Category createCategory(String name, String description, Long parentId) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException("分类名称已存在");
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setParentId(parentId);
        // 统一分类表：默认创建博客分类（CATEGORY）
        if (category.getType() == null || category.getType().isBlank()) {
            category.setType("CATEGORY");
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * 更新分类
     */
    public Category updateCategory(Long id, String name, String description, Integer sortOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        
        category.setName(name);
        category.setDescription(description);
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }
        
        return categoryRepository.save(category);
    }
    
    /**
     * 删除分类
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        
        // 检查是否有子分类
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException("请先删除子分类");
        }
        
        // 检查是否有文章
        if (category.getArticleCount() > 0) {
            throw new BusinessException("该分类下还有文章，无法删除");
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * 获取一级分类
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
    }
    
    /**
     * 获取子分类
     */
    public List<Category> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }
    
    /**
     * 获取分类详情
     */
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
    }
}

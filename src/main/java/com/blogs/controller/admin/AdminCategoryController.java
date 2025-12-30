package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.entity.Category;
import com.blogs.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台分类管理控制器
 */
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有分类
     */
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return Result.success(category);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    public Result<Category> createCategory(@RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) Long parentId) {
        Category category = categoryService.createCategory(name, description, parentId);
        return Result.success(category);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) Integer sortOrder) {
        Category category = categoryService.updateCategory(id, name, description, sortOrder);
        return Result.success(category);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}

package com.blogs.controller.front;

import com.blogs.common.Result;
import com.blogs.dto.UnifiedCategoryVO;
import com.blogs.service.UnifiedCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 全站统一分类/版块控制器（导航/筛选统一入口）
 *
 * 说明：不替换旧接口，仅新增统一入口，低侵入增量上线。
 */
@RestController
@RequestMapping("/front/unified/categories")
public class UnifiedCategoryController {

    @Autowired
    private UnifiedCategoryService unifiedCategoryService;

    @GetMapping
    public Result<List<UnifiedCategoryVO>> getAll(@RequestParam(required = false) String type) {
        return Result.success(unifiedCategoryService.getAll(type));
    }
}



package com.blogs.controller.front;

import com.blogs.common.PageResult;
import com.blogs.common.Result;
import com.blogs.dto.SearchResultVO;
import com.blogs.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全站统一搜索控制器
 */
@RestController
@RequestMapping("/front/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 全站搜索（默认 ALL，可选 ARTICLE / FORUM_POST）
     */
    @GetMapping
    public Result<PageResult<SearchResultVO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.search(keyword, contentType, page, size));
    }
}



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
     * @param keyword 搜索关键词
     * @param contentType 内容类型：ALL(全部), ARTICLE(仅文章), FORUM_POST(仅帖子)
     * @param sortBy 排序方式：relevance(相关度), time(发布时间), views(阅读量)
     * @param page 页码
     * @param size 每页数量
     */
    @GetMapping
    public Result<PageResult<SearchResultVO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.search(keyword, contentType, sortBy, page, size));
    }
}



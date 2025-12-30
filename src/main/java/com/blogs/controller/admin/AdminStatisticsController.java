package com.blogs.controller.admin;

import com.blogs.common.Result;
import com.blogs.dto.ArticleVO;
import com.blogs.dto.StatisticsDTO;
import com.blogs.service.ArticleService;
import com.blogs.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台统计控制器
 */
@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 获取统计数据
     */
    @GetMapping
    public Result<StatisticsDTO> getStatistics() {
        StatisticsDTO dto = statisticsService.getStatistics();
        return Result.success(dto);
    }
    
    /**
     * 获取热门文章
     */
    @GetMapping("/hot-articles")
    public Result<List<ArticleVO>> getHotArticles(@RequestParam(defaultValue = "10") Integer limit) {
        List<ArticleVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }
}

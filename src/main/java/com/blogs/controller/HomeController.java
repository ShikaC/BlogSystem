package com.blogs.controller;

import com.blogs.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Result<String> index() {
        return Result.success("Blogs API is running!");
    }
}


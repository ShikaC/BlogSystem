package com.blogs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前端路由控制器 - 处理Vue Router History模式
 */
@Controller
public class HomeController {

    /**
     * 处理前端路由，将所有非API请求转发到index.html
     */
    @RequestMapping(value = {"/", "/forum/**", "/article/**", "/category/**", "/tag/**", "/archives/**", "/search/**", "/about/**", "/links/**", "/user/**", "/admin/**"})
    public String index() {
        return "forward:/index.html";
    }
}